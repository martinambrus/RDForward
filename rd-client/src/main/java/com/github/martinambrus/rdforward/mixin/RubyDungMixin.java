package com.github.martinambrus.rdforward.mixin;

import com.github.martinambrus.rdforward.client.HudRenderer;
import com.github.martinambrus.rdforward.client.MultiplayerState;
import com.github.martinambrus.rdforward.client.RDClient;
import com.github.martinambrus.rdforward.client.RemotePlayerRenderer;
import com.mojang.rubydung.Player;
import com.mojang.rubydung.RubyDung;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.level.LevelListener;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

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
    private Level level;

    /** Frame counter for throttling position updates. */
    private int rdforward$tickCounter = 0;

    /** Whether the server world has been applied to the local Level. */
    private boolean rdforward$worldApplied = false;

    /** Backup of local single-player blocks (saved before server world is applied). */
    private byte[] rdforward$savedLocalBlocks = null;

    /** Whether we're currently in multiplayer mode. */
    private boolean rdforward$multiplayerMode = false;

    /** Edge detection for F6 toggle (key was pressed previous frame). */
    private boolean rdforward$f6WasPressed = false;

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

        // Apply server world data once when it arrives
        if (!rdforward$worldApplied && state.isWorldReady() && level != null) {
            rdforward$applyServerWorld(state);
        }

        if (!client.isConnected()) {
            // Drain block change queue so it doesn't grow in single player
            RubyDung.blockChangeQueue.clear();
            return;
        }

        // Send position updates every 3 frames (~20/sec at 60 FPS)
        rdforward$tickCounter++;
        if (rdforward$tickCounter >= 3 && player != null) {
            rdforward$tickCounter = 0;
            PlayerAccessor pa = (PlayerAccessor) player;
            // Convert float block coordinates to fixed-point (blocks * 32)
            short x = (short) (pa.getX() * 32);
            short y = (short) (pa.getY() * 32);
            short z = (short) (pa.getZ() * 32);
            // Convert degrees to byte rotation (0-255 maps to 0-360)
            int yaw = (int) (pa.getYRot() * 256.0f / 360.0f) & 0xFF;
            int pitch = (int) (pa.getXRot() * 256.0f / 360.0f) & 0xFF;
            client.sendPosition(x, y, z, yaw, pitch);
        }

        // Send local block changes to the server
        int[] blockEvent;
        while ((blockEvent = RubyDung.blockChangeQueue.poll()) != null) {
            client.sendBlockChange(blockEvent[0], blockEvent[1], blockEvent[2], blockEvent[3], blockEvent[4]);
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
    }

    /**
     * Called when the game is shutting down (window closed).
     * Disconnects the multiplayer client so Netty threads don't keep the JVM alive.
     */
    @Inject(method = "destroy", at = @At("HEAD"))
    private void onDestroy(CallbackInfo ci) {
        RDClient client = RDClient.getInstance();
        if (client.isConnected()) {
            System.out.println("Disconnecting from server...");
            client.disconnect();
        }
        HudRenderer.cleanup();
    }

    // -- Internal helpers --

    private void rdforward$connectToServer() {
        System.out.println("[RDForward] Connecting to " + rdforward$serverHost + ":" + rdforward$serverPort
            + (rdforward$username.isEmpty() ? " (server will assign name)..." : " as " + rdforward$username + "..."));
        RDClient.getInstance().connect(rdforward$serverHost, rdforward$serverPort, rdforward$username);
        rdforward$multiplayerMode = true;
        rdforward$worldApplied = false;
        System.out.println("[RDForward] Switched to MULTIPLAYER mode");
    }

    private void rdforward$disconnectFromServer() {
        RDClient.getInstance().disconnect();
        rdforward$multiplayerMode = false;

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
        for (int i = 0; i < localBlocks.length; i++) {
            localBlocks[i] = serverBlocks[i] != 0 ? (byte) 1 : (byte) 0;
        }

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
