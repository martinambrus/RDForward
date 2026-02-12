package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.github.martinambrus.rdforward.android.game.*;
import com.github.martinambrus.rdforward.render.BlendFactor;
import com.github.martinambrus.rdforward.render.DepthFunc;
import com.github.martinambrus.rdforward.render.TextureFilter;
import com.github.martinambrus.rdforward.render.libgdx.LibGDXGraphics;

/**
 * libGDX ApplicationAdapter that runs the full RubyDung game on Android.
 * Replicates the desktop game loop using the RDGraphics abstraction
 * (LibGDXGraphics) instead of direct GL11 calls.
 */
public class RDForwardGameAdapter extends ApplicationAdapter {

    private final AndroidLauncher launcher;
    private LibGDXGraphics graphics;
    private TouchInputAdapter touchInput;

    // Game state
    private Level level;
    private LevelRenderer levelRenderer;
    private Player player;
    private Timer timer;
    private HitResult hitResult;
    private int textureId;

    // Fog color (sky color)
    private static final float FOG_R = 0.5f;
    private static final float FOG_G = 0.8f;
    private static final float FOG_B = 1.0f;

    public RDForwardGameAdapter(AndroidLauncher launcher) {
        this.launcher = launcher;
    }

    @Override
    public void create() {
        graphics = new LibGDXGraphics();
        touchInput = new TouchInputAdapter();
        Gdx.input.setInputProcessor(touchInput);

        // Load texture
        textureId = graphics.loadTexture("/terrain.png", TextureFilter.NEAREST);

        // Create world and renderer
        level = new Level(256, 256, 64);
        levelRenderer = new LevelRenderer(level, graphics);
        player = new Player(level, touchInput);
        timer = new Timer(60.0f);
    }

    @Override
    public void render() {
        if (level == null) return;

        // Advance timer and tick game logic
        timer.advanceTime();
        for (int i = 0; i < timer.ticks; i++) {
            player.tick();
        }

        // Process touch input
        touchInput.update();

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

        // Reset color for next frame
        graphics.setColor(1, 1, 1, 1);
    }

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
                int face = 1; // default to top
                if (prevBx != Integer.MIN_VALUE) {
                    int dx = bx - prevBx, dy = by - prevBy, dz = bz - prevBz;
                    if (dy < 0) face = 0;      // bottom
                    else if (dy > 0) face = 1;  // top
                    else if (dz < 0) face = 2;  // front (-z)
                    else if (dz > 0) face = 3;  // back (+z)
                    else if (dx < 0) face = 4;  // left (-x)
                    else if (dx > 0) face = 5;  // right (+x)
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
        if (tap) {
            if (hitResult != null) {
                int bx = hitResult.x, by = hitResult.y, bz = hitResult.z;
                Gdx.app.log("GameInput", "TAP place: hit=(" + bx + "," + by + "," + bz
                        + ") face=" + hitResult.f);
                switch (hitResult.f) {
                    case 0: by--; break;
                    case 1: by++; break;
                    case 2: bz--; break;
                    case 3: bz++; break;
                    case 4: bx--; break;
                    case 5: bx++; break;
                }
                Gdx.app.log("GameInput", "  → placing at (" + bx + "," + by + "," + bz + ")");
                level.setTile(bx, by, bz, 1);
            } else {
                Gdx.app.log("GameInput", "TAP but hitResult is NULL — no block in crosshair");
            }
        }

        // Long press = right click = destroy block at hit point
        boolean hold = touchInput.isMouseButtonDown(1);
        if (hold && hitResult != null) {
            Gdx.app.log("GameInput", "HOLD destroy: (" + hitResult.x + ","
                    + hitResult.y + "," + hitResult.z + ")");
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
        float sz = Math.max(8, w * 0.01f); // scale with screen size

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

    @Override
    public void resize(int width, int height) {
        // Viewport is updated automatically by libGDX
    }

    @Override
    public void pause() {
        // Save state if needed
    }

    @Override
    public void resume() {
        // Restore state if needed
    }

    @Override
    public void dispose() {
        if (graphics != null) graphics.dispose();
    }

    public LibGDXGraphics getGraphics() { return graphics; }
    public TouchInputAdapter getTouchInput() { return touchInput; }
}
