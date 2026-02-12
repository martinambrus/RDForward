package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.github.martinambrus.rdforward.android.game.*;
import com.github.martinambrus.rdforward.android.multiplayer.MultiplayerState;
import com.github.martinambrus.rdforward.android.multiplayer.RDClient;
import com.github.martinambrus.rdforward.render.BlendFactor;
import com.github.martinambrus.rdforward.render.DepthFunc;
import com.github.martinambrus.rdforward.render.TextureFilter;
import com.github.martinambrus.rdforward.render.libgdx.LibGDXGraphics;

/**
 * libGDX ApplicationAdapter that runs the full RubyDung game on Android.
 * Provides a welcome screen with Single Player / Multiplayer options,
 * full multiplayer support, and a HUD banner.
 */
public class RDForwardGameAdapter extends ApplicationAdapter {

    private final AndroidLauncher launcher;
    private LibGDXGraphics graphics;
    private TouchInputAdapter touchInput;

    // Game state machine
    private enum GameState { MENU, PLAYING }
    private GameState gameState = GameState.MENU;

    // Game objects (created when entering PLAYING state)
    private Level level;
    private LevelRenderer levelRenderer;
    private Player player;
    private Timer timer;
    private HitResult hitResult;
    private int textureId;

    // 2D rendering for menu and HUD
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private GlyphLayout glyphLayout;
    private Texture whitePixel;

    // Multiplayer state
    private boolean multiplayerMode = false;
    private boolean worldApplied = false;
    private byte[] savedLocalBlocks = null;
    private int positionSyncCounter = 0;
    private long serverUnavailableUntil = 0;

    // Fog color (sky color)
    private static final float FOG_R = 0.5f;
    private static final float FOG_G = 0.8f;
    private static final float FOG_B = 1.0f;

    // Menu button layout (computed each frame for responsive sizing)
    private float btnX, btnWidth, btnHeight;
    private float singlePlayerBtnY, multiplayerBtnY;

    // HUD banner bounds (screen coords, libGDX y-up) for tap-to-switch detection
    private float hudBannerRight, hudBannerBottom;

    public RDForwardGameAdapter(AndroidLauncher launcher) {
        this.launcher = launcher;
    }

    @Override
    public void create() {
        graphics = new LibGDXGraphics();
        touchInput = new TouchInputAdapter();
        Gdx.input.setInputProcessor(touchInput);

        // 2D rendering setup
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        glyphLayout = new GlyphLayout();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        // Load terrain texture (needed for game rendering)
        textureId = graphics.loadTexture("/terrain.png", TextureFilter.NEAREST);
    }

    @Override
    public void render() {
        switch (gameState) {
            case MENU: renderMenu(); break;
            case PLAYING: renderGame(); break;
        }
    }

    // ── Welcome Screen ──────────────────────────────────────────────

    private void renderMenu() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float scale = Math.max(1f, h / 480f);

        Gdx.gl.glClearColor(0.12f, 0.12f, 0.22f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        spriteBatch.begin();

        // Title
        font.getData().setScale(scale * 3f);
        font.setColor(1f, 1f, 1f, 1f);
        glyphLayout.setText(font, "RDForward");
        font.draw(spriteBatch, glyphLayout, (w - glyphLayout.width) / 2, h * 0.75f);

        // Subtitle
        font.getData().setScale(scale * 1.2f);
        font.setColor(0.6f, 0.6f, 0.7f, 1f);
        glyphLayout.setText(font, "rd-132211");
        font.draw(spriteBatch, glyphLayout, (w - glyphLayout.width) / 2, h * 0.75f - scale * 55f);

        // Buttons
        font.getData().setScale(scale * 2f);
        btnWidth = Math.min(w * 0.65f, 600 * scale);
        btnHeight = scale * 60f;
        btnX = (w - btnWidth) / 2;

        singlePlayerBtnY = h * 0.40f;
        drawButton("Single Player", btnX, singlePlayerBtnY, btnWidth, btnHeight,
                0.18f, 0.45f, 0.18f);

        multiplayerBtnY = singlePlayerBtnY - btnHeight - scale * 24f;
        drawButton("Multiplayer", btnX, multiplayerBtnY, btnWidth, btnHeight,
                0.18f, 0.18f, 0.45f);

        // Reset font state
        font.getData().setScale(1f);
        font.setColor(1f, 1f, 1f, 1f);
        spriteBatch.end();

        // Handle button touches
        if (Gdx.input.justTouched()) {
            float tx = Gdx.input.getX();
            float ty = h - Gdx.input.getY(); // flip Y for libGDX bottom-up coords
            if (isInButton(tx, ty, btnX, singlePlayerBtnY, btnWidth, btnHeight)) {
                startSinglePlayer();
            } else if (isInButton(tx, ty, btnX, multiplayerBtnY, btnWidth, btnHeight)) {
                startMultiplayer();
            }
        }
    }

    private void drawButton(String text, float x, float y, float w, float h,
                            float r, float g, float b) {
        spriteBatch.setColor(r, g, b, 0.85f);
        spriteBatch.draw(whitePixel, x, y, w, h);

        font.setColor(1f, 1f, 1f, 1f);
        glyphLayout.setText(font, text);
        float textX = x + (w - glyphLayout.width) / 2;
        float textY = y + (h + glyphLayout.height) / 2;
        font.draw(spriteBatch, glyphLayout, textX, textY);

        spriteBatch.setColor(1, 1, 1, 1);
    }

    private boolean isInButton(float tx, float ty, float bx, float by, float bw, float bh) {
        return tx >= bx && tx <= bx + bw && ty >= by && ty <= by + bh;
    }

    private void startSinglePlayer() {
        multiplayerMode = false;
        initGame();
        gameState = GameState.PLAYING;
    }

    private void startMultiplayer() {
        ServerConnectDialog.show((host, port, username) -> {
            Gdx.app.postRunnable(() -> {
                multiplayerMode = true;
                worldApplied = false;
                savedLocalBlocks = null;
                initGame();
                RDClient.getInstance().connect(host, port, username);
                gameState = GameState.PLAYING;
            });
        });
    }

    private void initGame() {
        if (level != null) return; // already initialized
        level = new Level(256, 256, 64);
        levelRenderer = new LevelRenderer(level, graphics);
        player = new Player(level, touchInput);
        timer = new Timer(60.0f);
    }

    // ── Game Rendering ──────────────────────────────────────────────

    private void renderGame() {
        if (level == null) return;

        // Advance timer and tick game logic
        timer.advanceTime();
        for (int i = 0; i < timer.ticks; i++) {
            player.tick();
        }

        // Process touch input
        touchInput.update();

        // F6 toggle (physical keyboard) or tap on HUD banner
        boolean toggleMP = touchInput.consumeF6();
        if (!toggleMP && Gdx.input.justTouched()) {
            float tx = Gdx.input.getX();
            float ty = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip to y-up
            if (tx <= hudBannerRight && ty >= hudBannerBottom) {
                toggleMP = true;
            }
        }
        if (toggleMP) {
            if (multiplayerMode) {
                disconnectFromServer();
            } else {
                ServerConnectDialog.show((host, port, username) -> {
                    Gdx.app.postRunnable(() -> {
                        multiplayerMode = true;
                        worldApplied = false;
                        savedLocalBlocks = null;
                        RDClient.getInstance().connect(host, port, username);
                    });
                });
            }
        }

        // Multiplayer sync
        if (multiplayerMode) {
            syncMultiplayer();
        }

        // Camera look from touch input
        float xo = touchInput.consumeMouseDX();
        float yo = touchInput.consumeMouseDY();
        player.turn(xo, yo);

        // Block picking via ray-march
        pick(timer.a);

        // Block interaction
        handleInput();

        // Clear screen to sky color
        graphics.setClearColor(FOG_R, FOG_G, FOG_B, 1.0f);
        graphics.clear();

        // Setup 3D camera
        setupCamera(timer.a);
        graphics.enableCullFace();
        graphics.enableDepthTest();
        graphics.depthFunc(DepthFunc.LEQUAL);

        // Bind terrain texture for all chunk rendering
        graphics.enableTexture2D();
        graphics.bindTexture(textureId);

        // Layer 0: bright faces (no fog)
        graphics.disableFog();
        levelRenderer.render(0);

        // Layer 1: dark/shadowed faces (with fog)
        graphics.enableFog(0.2f, FOG_R, FOG_G, FOG_B, 1.0f);
        levelRenderer.render(1);

        // Hit highlight
        graphics.disableTexture2D();
        if (hitResult != null) {
            levelRenderer.renderHit(hitResult);
        }
        graphics.disableFog();

        // Crosshair overlay
        renderCrosshair();

        // HUD banner (top-left)
        renderHud();

        // Reset color for next frame
        graphics.setColor(1, 1, 1, 1);
    }

    // ── Multiplayer Sync ────────────────────────────────────────────

    private void syncMultiplayer() {
        MultiplayerState state = MultiplayerState.getInstance();
        RDClient client = RDClient.getInstance();

        // Apply server world once when ready
        if (!worldApplied && state.isWorldReady() && level != null) {
            applyServerWorld(state);
        }

        if (!client.isConnected()) {
            if (worldApplied) {
                disconnectFromServer();
            }
            if (!worldApplied && client.hasConnectionFailed()) {
                disconnectFromServer();
                serverUnavailableUntil = System.currentTimeMillis() + 5000;
            }
            return;
        }

        // Self-teleport (server spawn / correction)
        short[] selfTeleport = state.pollSelfTeleport();
        if (selfTeleport != null && player != null) {
            float tx = selfTeleport[0] / 32.0f;
            float ty = selfTeleport[1] / 32.0f + (1.0f / 32.0f);
            float tz = selfTeleport[2] / 32.0f;
            player.x = tx; player.y = ty; player.z = tz;
            player.xo = tx; player.yo = ty; player.zo = tz;
            float w = 0.3f;
            float feetY = ty - 1.62f;
            player.bb = new AABB(tx - w, feetY, tz - w, tx + w, feetY + 1.8f, tz + w);
        }

        // Position sync (every 3 ticks ≈ 20 TPS at 60 FPS)
        if (timer.ticks > 0 && player != null) {
            positionSyncCounter += timer.ticks;
            if (positionSyncCounter >= 3) {
                positionSyncCounter = 0;
                short x = (short) (player.x * 32);
                short y = (short) Math.round(player.y * 32);
                short z = (short) (player.z * 32);
                int yaw = (int) (player.yRot * 256.0f / 360.0f) & 0xFF;
                int pitch = (int) (player.xRot * 256.0f / 360.0f) & 0xFF;
                client.sendPosition(x, y, z, yaw, pitch);
            }
        }

        // Apply server block changes
        MultiplayerState.BlockChange change;
        while ((change = state.pollBlockChange()) != null) {
            if (level != null) {
                level.setTile(change.x, change.y, change.z, change.blockType != 0 ? 1 : 0);
            }
        }

        // Revert timed-out predictions
        for (MultiplayerState.PendingPrediction pred : state.pollTimedOutPredictions()) {
            if (level != null) {
                level.setTile(pred.x, pred.y, pred.z, pred.originalBlockType != 0 ? 1 : 0);
            }
        }
    }

    private void applyServerWorld(MultiplayerState state) {
        byte[] localBlocks = level.getBlocks();
        byte[] serverBlocks = state.getWorldBlocks();

        if (savedLocalBlocks == null) {
            savedLocalBlocks = new byte[localBlocks.length];
            System.arraycopy(localBlocks, 0, savedLocalBlocks, 0, localBlocks.length);
        }

        if (serverBlocks == null || localBlocks.length != serverBlocks.length) {
            worldApplied = true;
            return;
        }

        for (int i = 0; i < localBlocks.length; i++) {
            localBlocks[i] = serverBlocks[i] != 0 ? (byte) 1 : (byte) 0;
        }

        level.calcLightDepths(0, 0, level.width, level.height);
        level.notifyAllChanged();
        worldApplied = true;
    }

    private void disconnectFromServer() {
        RDClient.getInstance().disconnect();
        multiplayerMode = false;

        if (savedLocalBlocks != null && level != null) {
            byte[] localBlocks = level.getBlocks();
            System.arraycopy(savedLocalBlocks, 0, localBlocks, 0, localBlocks.length);
            savedLocalBlocks = null;
            level.calcLightDepths(0, 0, level.width, level.height);
            level.notifyAllChanged();
        }
    }

    // ── HUD Banner ──────────────────────────────────────────────────

    private void renderHud() {
        String text;
        if (serverUnavailableUntil > System.currentTimeMillis()) {
            text = "Server Unavailable";
        } else if (multiplayerMode && RDClient.getInstance().isConnected()) {
            String name = MultiplayerState.getInstance().getServerName();
            text = "rd-132211 multiplayer"
                    + (name.isEmpty() ? "" : " - " + name)
                    + " [tap for single player]";
        } else {
            text = "rd-132211 single player [tap for multiplayer]";
        }

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float scale = Math.max(1f, h / 480f);

        // Transition from 3D (LibGDXGraphics shader) to 2D (SpriteBatch shader):
        // SpriteBatch doesn't disable depth test or cull face, so quads can get
        // rejected by leftover 3D state. Set a clean 2D baseline explicitly.
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
        spriteBatch.begin();

        font.getData().setScale(scale * 0.8f);

        // Measure text
        glyphLayout.setText(font, text);
        float pad = 4 * scale;
        float bgHeight = glyphLayout.height + pad * 2;
        float bgWidth = glyphLayout.width + pad * 4;

        // Background bar flush with top of screen
        spriteBatch.setColor(0, 0, 0, 0.4f);
        spriteBatch.draw(whitePixel, 0, h - bgHeight, bgWidth, bgHeight);
        spriteBatch.setColor(1, 1, 1, 1);

        // Text centered vertically in the background bar
        float textY = h - pad;

        // Shadow
        font.setColor(0, 0, 0, 0.8f);
        font.draw(spriteBatch, text, pad * 2 + 1, textY - 1);

        // Text
        font.setColor(1, 1, 1, 1);
        font.draw(spriteBatch, text, pad * 2, textY);

        font.getData().setScale(1f);
        spriteBatch.end();

        // Store banner bounds for tap detection (libGDX y-up coords)
        hudBannerRight = bgWidth;
        hudBannerBottom = h - bgHeight;

        // Restore 3D state for next frame
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    }

    // ── Camera / Picking / Input (unchanged) ────────────────────────

    private void setupCamera(float a) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        graphics.matrixModeProjection();
        graphics.loadIdentity();
        graphics.perspective(70.0f, (float) w / (float) h, 0.05f, 1000.0f);

        graphics.matrixModeModelView();
        graphics.loadIdentity();
        graphics.translate(0.0f, 0.0f, -0.3f);
        graphics.rotate(player.xRot, 1.0f, 0.0f, 0.0f);
        graphics.rotate(player.yRot, 0.0f, 1.0f, 0.0f);

        float x = player.xo + (player.x - player.xo) * a;
        float y = player.yo + (player.y - player.yo) * a;
        float z = player.zo + (player.z - player.zo) * a;
        graphics.translate(-x, -y, -z);
    }

    private void pick(float a) {
        float eyeX = player.xo + (player.x - player.xo) * a;
        float eyeY = player.yo + (player.y - player.yo) * a;
        float eyeZ = player.zo + (player.z - player.zo) * a;

        float xRotRad = (float) Math.toRadians(player.xRot);
        float yRotRad = (float) Math.toRadians(player.yRot);
        float dirX = (float) (Math.sin(yRotRad) * Math.cos(xRotRad));
        float dirY = (float) (-Math.sin(xRotRad));
        float dirZ = (float) (-Math.cos(yRotRad) * Math.cos(xRotRad));

        float reach = 5.0f;
        float step = 0.05f;
        int prevBx = Integer.MIN_VALUE, prevBy = Integer.MIN_VALUE, prevBz = Integer.MIN_VALUE;
        hitResult = null;

        for (float t = 0; t < reach; t += step) {
            float px = eyeX + dirX * t;
            float py = eyeY + dirY * t;
            float pz = eyeZ + dirZ * t;
            int bx = (int) Math.floor(px);
            int by = (int) Math.floor(py);
            int bz = (int) Math.floor(pz);

            if (bx == prevBx && by == prevBy && bz == prevBz) continue;

            if (level.isSolidTile(bx, by, bz)) {
                int face = 1;
                if (prevBx != Integer.MIN_VALUE) {
                    int dx = bx - prevBx, dy = by - prevBy, dz = bz - prevBz;
                    if (dy < 0) face = 1;
                    else if (dy > 0) face = 0;
                    else if (dz < 0) face = 3;
                    else if (dz > 0) face = 2;
                    else if (dx < 0) face = 5;
                    else if (dx > 0) face = 4;
                }
                hitResult = new HitResult(bx, by, bz, 0, face);
                return;
            }
            prevBx = bx; prevBy = by; prevBz = bz;
        }
    }

    private void handleInput() {
        // Tap = left click = place block adjacent to hit face
        boolean tap = touchInput.isMouseButtonDown(0);
        if (tap && hitResult != null) {
            int bx = hitResult.x, by = hitResult.y, bz = hitResult.z;
            switch (hitResult.f) {
                case 0: by--; break;
                case 1: by++; break;
                case 2: bz--; break;
                case 3: bz++; break;
                case 4: bx--; break;
                case 5: bx++; break;
            }

            if (multiplayerMode && RDClient.getInstance().isConnected()) {
                byte orig = level.isTile(bx, by, bz) ? (byte) 1 : (byte) 0;
                MultiplayerState.getInstance().addPrediction(bx, by, bz, orig);
                RDClient.getInstance().sendBlockChange(bx, by, bz, 1, 1);
            }
            level.setTile(bx, by, bz, 1);
        }

        // Long press = right click = destroy block at hit point
        boolean hold = touchInput.isMouseButtonDown(1);
        if (hold && hitResult != null) {
            if (multiplayerMode && RDClient.getInstance().isConnected()) {
                byte orig = level.isTile(hitResult.x, hitResult.y, hitResult.z) ? (byte) 1 : (byte) 0;
                MultiplayerState.getInstance().addPrediction(hitResult.x, hitResult.y, hitResult.z, orig);
                RDClient.getInstance().sendBlockChange(hitResult.x, hitResult.y, hitResult.z, 0, 0);
            }
            level.setTile(hitResult.x, hitResult.y, hitResult.z, 0);
        }
    }

    private void renderCrosshair() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        graphics.matrixModeProjection();
        graphics.loadIdentity();
        graphics.ortho(0, w, h, 0, -1, 1);
        graphics.matrixModeModelView();
        graphics.loadIdentity();

        graphics.disableDepthTest();
        graphics.disableTexture2D();
        graphics.enableBlend();
        graphics.blendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);

        float cx = w / 2.0f;
        float cy = h / 2.0f;
        float sz = Math.max(8, w * 0.01f);

        graphics.setColor(1.0f, 1.0f, 1.0f, 0.7f);
        graphics.beginLines();
        graphics.vertex2f(cx - sz, cy);
        graphics.vertex2f(cx + sz, cy);
        graphics.vertex2f(cx, cy - sz);
        graphics.vertex2f(cx, cy + sz);
        graphics.endDraw();

        graphics.disableBlend();
        graphics.enableDepthTest();
    }

    // ── Lifecycle ───────────────────────────────────────────────────

    @Override
    public void resize(int width, int height) {
        // Viewport is updated automatically by libGDX
    }

    @Override
    public void pause() {
        if (level != null) level.save();
    }

    @Override
    public void resume() {
        // Restore state if needed
    }

    @Override
    public void dispose() {
        if (multiplayerMode) disconnectFromServer();
        if (level != null) level.save();
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
        if (whitePixel != null) whitePixel.dispose();
        if (graphics != null) graphics.dispose();
    }

    public LibGDXGraphics getGraphics() { return graphics; }
    public TouchInputAdapter getTouchInput() { return touchInput; }
}
