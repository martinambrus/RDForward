package com.github.martinambrus.rdforward.mixin;

import com.github.martinambrus.rdforward.client.ChatInput;
import com.github.martinambrus.rdforward.client.ChatRenderer;
import com.github.martinambrus.rdforward.client.HudRenderer;
import com.github.martinambrus.rdforward.client.MultiplayerState;
import com.github.martinambrus.rdforward.client.NameTagRenderer;
import com.github.martinambrus.rdforward.client.RDClient;
import com.github.martinambrus.rdforward.client.RemotePlayerRenderer;
import com.mojang.rubydung.Player;
import com.mojang.rubydung.RubyDung;
import com.mojang.rubydung.Timer;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.phys.AABB;
import com.mojang.rubydung.level.LevelListener;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Queue;

/**
 * Injects RDForward multiplayer functionality into the RubyDung game loop.
 *
 * Hooks:
 *   1. run() HEAD — print banner, auto-connect if --server was given
 *   2. render(float) HEAD — F6 toggle, position sync, block changes
 *   3. render(float) before glfwSwapBuffers — HUD text + remote players
 *   4. destroy() HEAD — disconnect Netty + cleanup HUD texture
 */
@Mixin(RubyDung.class)
public class RubyDungMixin {

    @Shadow
    private Player player;

    @Shadow
    private Timer timer;

    @Shadow
    private Level level;

    // -- Game input state (shadowed for chat input management) --
    @Shadow
    private boolean mouseGrabbed;

    @Shadow
    private double mouseDX;

    @Shadow
    private double mouseDY;

    @Shadow
    private boolean firstMouse;

    @Shadow
    @Final
    private Queue<int[]> mouseEvents;

    @Shadow
    @Final
    private Queue<int[]> keyEvents;

    /** Whether the server world has been applied to the local Level. */
    private boolean rdforward$worldApplied = false;

    /** Backup of local single-player blocks (saved before server world is applied). */
    private byte[] rdforward$savedLocalBlocks = null;

    /** Whether we're currently in multiplayer mode. */
    private boolean rdforward$multiplayerMode = false;

    /** Edge detection for F6 toggle (key was pressed previous frame). */
    private boolean rdforward$f6WasPressed = false;

    /** Edge detection for T key (chat open). */
    private boolean rdforward$tWasPressed = false;

    /** Whether chat was active on the previous frame (for detecting transitions). */
    private boolean rdforward$chatWasActive = false;

    /** Timestamp until which "Server Unavailable" is shown (0 = not showing). */
    private long rdforward$serverUnavailableUntil = 0;

    /** Server connection details (parsed once at startup). */
    private String rdforward$serverHost = "localhost";
    private int rdforward$serverPort = 25565;
    private String rdforward$username = "";

    @Inject(method = "run", at = @At("HEAD"))
    private void onGameStart(CallbackInfo ci) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" RDForward " + getVersion());
        System.out.println(" Fabric Loader initialized");
        System.out.println(" Single player (F6 for multiplayer)");
        System.out.println("========================================");
        System.out.println();

        // Parse server settings from system properties (set by CLI flags or -D)
        String serverProp = System.getProperty("rdforward.server", "");
        if (!serverProp.isEmpty()) {
            if (serverProp.contains(":")) {
                String[] parts = serverProp.split(":", 2);
                rdforward$serverHost = parts[0];
                try {
                    rdforward$serverPort = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port in rdforward.server: " + parts[1]);
                }
            } else {
                rdforward$serverHost = serverProp;
            }
            // Auto-connect when --server flag was explicitly provided
            rdforward$username = System.getProperty("rdforward.username", "");
            rdforward$connectToServer();
        }
        rdforward$username = System.getProperty("rdforward.username", "");
    }

    /**
     * Called at the start of each render frame.
     * Handles F6 toggle, applies server world, sends position, applies block changes.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(float partialTick, CallbackInfo ci) {
        // F6 toggle — poll key state directly (reliable on all platforms)
        boolean f6Pressed = GLFW.glfwGetKey(RubyDung.window, GLFW.GLFW_KEY_F6) == GLFW.GLFW_PRESS;
        if (f6Pressed && !rdforward$f6WasPressed) {
            System.out.println("[RDForward] F6 pressed — toggling multiplayer mode");
            if (rdforward$multiplayerMode) {
                rdforward$disconnectFromServer();
            } else {
                rdforward$connectToServer();
            }
        }
        rdforward$f6WasPressed = f6Pressed;

        // Ctrl+M fallback — flag set by key callback/polling in RubyDung.java
        if (RubyDung.multiplayerToggleRequested) {
            RubyDung.multiplayerToggleRequested = false;
            System.out.println("[RDForward] Ctrl+M pressed — toggling multiplayer mode");
            if (rdforward$multiplayerMode) {
                rdforward$disconnectFromServer();
            } else {
                rdforward$connectToServer();
            }
        }

        MultiplayerState state = MultiplayerState.getInstance();
        RDClient client = RDClient.getInstance();

        // Poll chat messages from server and feed to ChatRenderer
        String chatMsg;
        while ((chatMsg = state.pollChatMessage()) != null) {
            ChatRenderer.addMessage(chatMsg);
        }

        // T key opens chat input (only when connected and chat isn't already open)
        if (!ChatInput.isActive()) {
            boolean tPressed = GLFW.glfwGetKey(RubyDung.window, GLFW.GLFW_KEY_T) == GLFW.GLFW_PRESS;
            if (tPressed && !rdforward$tWasPressed && client.isConnected()) {
                ChatInput.open(RubyDung.window);
            }
            rdforward$tWasPressed = tPressed;
        }

        // Chat state transitions — manage the game's internal input state so that:
        //   1. Mouse look stops while chat is open (mouseGrabbed = false)
        //   2. Click-to-recapture is neutralized (mouseEvents cleared)
        //   3. Mouse tracking resets cleanly on close (firstMouse = true, no delta jump)
        boolean chatActive = ChatInput.isActive();
        if (chatActive && !rdforward$chatWasActive) {
            // Chat just opened — release cursor and stop mouse tracking
            mouseGrabbed = false;
            GLFW.glfwSetInputMode(RubyDung.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
        if (!chatActive && rdforward$chatWasActive) {
            // Chat just closed — re-grab cursor and reset mouse tracking so
            // the view doesn't jump to a new position
            GLFW.glfwSetInputMode(RubyDung.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            mouseGrabbed = true;
            firstMouse = true;
            mouseDX = 0;
            mouseDY = 0;
        }
        rdforward$chatWasActive = chatActive;

        if (chatActive) {
            // Neutralize ALL game input while chat is open:
            // - mouseGrabbed=false prevents camera rotation from mouse deltas
            // - Zero deltas so nothing accumulates for the next frame
            // - Clear event queues to prevent click-to-recapture and stale key events
            // The game's render() continues (so blocks/players still render) but
            // all input is suppressed. Keyboard movement (WASD) is suppressed by
            // the tick() cancellation inject below.
            mouseGrabbed = false;
            mouseDX = 0;
            mouseDY = 0;
            mouseEvents.clear();
            keyEvents.clear();
            return;
        }

        // Apply server world data once when it arrives
        if (!rdforward$worldApplied && state.isWorldReady() && level != null) {
            rdforward$applyServerWorld(state);
        }

        if (!client.isConnected()) {
            // Drain block change queue so it doesn't grow in single player
            RubyDung.blockChangeQueue.clear();

            // Auto-switch to single player if server disconnected while we were playing
            if (rdforward$multiplayerMode && rdforward$worldApplied) {
                System.out.println("[RDForward] Server connection lost — switching to single player");
                rdforward$disconnectFromServer();
            }

            // Connection attempt failed (server unavailable) — revert to single player
            if (rdforward$multiplayerMode && !rdforward$worldApplied && client.hasConnectionFailed()) {
                System.out.println("[RDForward] Server unavailable — returning to single player");
                rdforward$disconnectFromServer();
                rdforward$serverUnavailableUntil = System.currentTimeMillis() + 5000;
            }
            return;
        }

        // Apply self-teleport from server (initial spawn or position correction)
        short[] selfTeleport = state.pollSelfTeleport();
        if (selfTeleport != null && player != null) {
            float tx = selfTeleport[0] / 32.0f;
            // Add 1/32 block upward nudge to compensate for fixed-point truncation
            // that can place feet inside the ground block
            float ty = selfTeleport[1] / 32.0f + (1.0f / 32.0f);
            float tz = selfTeleport[2] / 32.0f;
            // Set position directly — y is eye level (bb.y0 + 1.62)
            player.x = tx;
            player.y = ty;
            player.z = tz;
            player.xo = tx;
            player.yo = ty;
            player.zo = tz;
            // Reconstruct bounding box: half-width 0.3, feet at eye-1.62, head at feet+1.8
            float w = 0.3f;
            float feetY = ty - 1.62f;
            player.bb = new AABB(tx - w, feetY, tz - w, tx + w, feetY + 1.8f, tz + w);
            System.out.println("[RDForward] Teleported to server position: ("
                + tx + ", " + ty + ", " + tz + ")");
        }

        // Send position updates once per game tick (20 TPS, frame-rate independent)
        if (((TimerAccessor) timer).getTicks() > 0 && player != null) {
            PlayerAccessor pa = (PlayerAccessor) player;
            // Convert float block coordinates to fixed-point (blocks * 32)
            // Use Math.round for Y to avoid downward truncation that buries the player
            short x = (short) (pa.getX() * 32);
            short y = (short) Math.round(pa.getY() * 32);
            short z = (short) (pa.getZ() * 32);
            // Convert degrees to byte rotation (0-255 maps to 0-360)
            int yaw = (int) (pa.getYRot() * 256.0f / 360.0f) & 0xFF;
            int pitch = (int) (pa.getXRot() * 256.0f / 360.0f) & 0xFF;
            client.sendPosition(x, y, z, yaw, pitch);
        }

        // Send local block changes to the server + record predictions
        int[] blockEvent;
        while ((blockEvent = RubyDung.blockChangeQueue.poll()) != null) {
            int bx = blockEvent[0], by = blockEvent[1], bz = blockEvent[2];
            // Record original block type for revert if server rejects
            if (level != null) {
                byte originalType = level.isTile(bx, by, bz) ? (byte) 1 : (byte) 0;
                state.addPrediction(bx, by, bz, originalType);
            }
            client.sendBlockChange(bx, by, bz, blockEvent[3], blockEvent[4]);
        }

        // Revert timed-out predictions (server didn't confirm the block change)
        java.util.List<MultiplayerState.PendingPrediction> timedOut = state.pollTimedOutPredictions();
        for (MultiplayerState.PendingPrediction pred : timedOut) {
            if (level != null) {
                level.setTile(pred.x, pred.y, pred.z, pred.originalBlockType != 0 ? 1 : 0);
            }
        }

        // Apply pending block changes from the server
        MultiplayerState.BlockChange change;
        while ((change = state.pollBlockChange()) != null) {
            if (level != null) {
                // RubyDung only knows block types 0 (air) and 1 (solid)
                level.setTile(change.x, change.y, change.z, change.blockType != 0 ? 1 : 0);
            }
        }
    }

    /**
     * Called just before glfwSwapBuffers — render HUD text and remote players.
     */
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"))
    private void onRenderBeforeSwap(float partialTick, CallbackInfo ci) {
        // Render remote players if in multiplayer
        if (RDClient.getInstance().isConnected()) {
            RemotePlayerRenderer.renderAll(partialTick);
        }

        // Always render HUD text
        int[] w = new int[1], h = new int[1];
        GLFW.glfwGetWindowSize(RubyDung.window, w, h);
        HudRenderer.drawText(rdforward$getHudText(), w[0], h[0]);

        // Render chat messages and input box (when connected)
        if (RDClient.getInstance().isConnected() || !ChatRenderer.isEmpty()) {
            ChatRenderer.render(w[0], h[0]);
            ChatInput.render(w[0], h[0]);
        }
    }

    /**
     * Called when the game is shutting down (window closed).
     * Disconnects the multiplayer client so Netty threads don't keep the JVM alive.
     */
    @Inject(method = "destroy", at = @At("HEAD"))
    private void onDestroy(CallbackInfo ci) {
        // Restore local blocks before the game saves level.dat,
        // otherwise the server world overwrites the single-player save
        if (rdforward$multiplayerMode && rdforward$savedLocalBlocks != null && level != null) {
            LevelAccessor la = (LevelAccessor) level;
            byte[] localBlocks = la.getBlocks();
            System.arraycopy(rdforward$savedLocalBlocks, 0, localBlocks, 0, localBlocks.length);
            rdforward$savedLocalBlocks = null;
            RubyDung.suppressLocalSave = false; // Allow saving the restored local world
            System.out.println("[RDForward] Restored local world before saving");
        }

        RDClient client = RDClient.getInstance();
        if (client.isConnected()) {
            System.out.println("Disconnecting from server...");
            client.disconnect();
        }
        HudRenderer.cleanup();
        ChatRenderer.cleanup();
        ChatInput.cleanup();
        NameTagRenderer.cleanup();
    }

    /**
     * Cancel player ticking while chat input is active.
     * Player.tick() uses GLFW.glfwGetKey() polling for WASD/Space/R,
     * which bypasses our key callback replacement. Cancelling tick()
     * prevents all movement while typing.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (ChatInput.isActive()) {
            ci.cancel();
        }
    }

    // -- Internal helpers --

    private void rdforward$connectToServer() {
        System.out.println("[RDForward] Connecting to " + rdforward$serverHost + ":" + rdforward$serverPort
            + (rdforward$username.isEmpty() ? " (server will assign name)..." : " as " + rdforward$username + "..."));
        RDClient.getInstance().connect(rdforward$serverHost, rdforward$serverPort, rdforward$username);
        rdforward$multiplayerMode = true;
        rdforward$worldApplied = false;
        RubyDung.suppressLocalSave = true;
        System.out.println("[RDForward] Switched to MULTIPLAYER mode");
    }

    private void rdforward$disconnectFromServer() {
        RDClient.getInstance().disconnect();
        rdforward$multiplayerMode = false;
        RubyDung.suppressLocalSave = false;

        // Restore the original single-player world
        if (rdforward$savedLocalBlocks != null && level != null) {
            LevelAccessor la = (LevelAccessor) level;
            byte[] localBlocks = la.getBlocks();
            System.arraycopy(rdforward$savedLocalBlocks, 0, localBlocks, 0, localBlocks.length);
            rdforward$savedLocalBlocks = null;

            // Recalculate lighting and trigger full chunk rebuild
            level.calcLightDepths(0, 0, level.width, level.height);
            ArrayList<LevelListener> listeners = la.getLevelListeners();
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).allChanged();
            }
            System.out.println("[RDForward] Restored local single-player world");
        }

        System.out.println("[RDForward] Switched to SINGLE PLAYER mode");
    }

    private String rdforward$getHudText() {
        // Show "Server Unavailable" for 5 seconds after a failed connection attempt
        if (rdforward$serverUnavailableUntil > System.currentTimeMillis()) {
            return "Multiplayer Server Unavailable (F6 to retry)";
        }

        if (rdforward$multiplayerMode) {
            String serverInfo = rdforward$serverHost + ":" + rdforward$serverPort;
            MultiplayerState state = MultiplayerState.getInstance();
            String serverName = state.getServerName();
            if (serverName != null && !serverName.isEmpty()) {
                serverInfo = serverName;
            }
            return "rd-132211 multiplayer - " + serverInfo + " (F6 for single player)";
        } else {
            return "rd-132211 single player (F6 for multiplayer)";
        }
    }

    /**
     * Replaces the local Level's block data with the server's world.
     * Maps server block types to RubyDung's binary solid/air system.
     */
    private void rdforward$applyServerWorld(MultiplayerState state) {
        LevelAccessor la = (LevelAccessor) level;
        byte[] localBlocks = la.getBlocks();
        byte[] serverBlocks = state.getWorldBlocks();

        // Save local blocks before overwriting (for restore on disconnect)
        if (rdforward$savedLocalBlocks == null) {
            rdforward$savedLocalBlocks = new byte[localBlocks.length];
            System.arraycopy(localBlocks, 0, rdforward$savedLocalBlocks, 0, localBlocks.length);
        }

        if (serverBlocks == null || localBlocks.length != serverBlocks.length) {
            System.err.println("World size mismatch: local=" + localBlocks.length
                + " server=" + (serverBlocks != null ? serverBlocks.length : "null"));
            rdforward$worldApplied = true;
            return;
        }

        // Map server blocks to RubyDung's block system (0=air, non-zero=solid)
        int solidCount = 0;
        for (int i = 0; i < localBlocks.length; i++) {
            localBlocks[i] = serverBlocks[i] != 0 ? (byte) 1 : (byte) 0;
            if (localBlocks[i] != 0) solidCount++;
        }
        System.out.println("[RDForward] Applied " + solidCount + " solid blocks out of " + localBlocks.length
            + " (" + (solidCount * 100 / localBlocks.length) + "% solid)");

        // Recalculate lighting for the entire world
        level.calcLightDepths(0, 0, level.width, level.height);

        // Notify all listeners (LevelRenderer) to rebuild all chunks
        ArrayList<LevelListener> listeners = la.getLevelListeners();
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).allChanged();
        }

        rdforward$worldApplied = true;
        System.out.println("Server world applied to local Level ("
            + level.width + "x" + level.depth + "x" + level.height + ")");
    }

    private static String getVersion() {
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer("rdforward")
                .map(mod -> "v" + mod.getMetadata().getVersion().getFriendlyString())
                .orElse("(dev)");
        } catch (Throwable e) {
            return "(dev)";
        }
    }
}
