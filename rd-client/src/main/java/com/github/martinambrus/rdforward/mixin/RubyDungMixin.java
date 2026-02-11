package com.github.martinambrus.rdforward.mixin;

import com.github.martinambrus.rdforward.client.MultiplayerState;
import com.github.martinambrus.rdforward.client.RDClient;
import com.github.martinambrus.rdforward.client.RemotePlayerRenderer;
import com.mojang.rubydung.Player;
import com.mojang.rubydung.RubyDung;
import com.mojang.rubydung.level.Level;
import com.mojang.rubydung.level.LevelListener;
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
 *   1. run() HEAD — print banner and auto-connect to server
 *   2. render(float) HEAD — send position updates, apply server block changes
 *   3. render(float) before glfwSwapBuffers — render remote players
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

    @Inject(method = "run", at = @At("HEAD"))
    private void onGameStart(CallbackInfo ci) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(" RDForward " + getVersion());
        System.out.println(" Fabric Loader initialized");
        System.out.println(" Multiplayer enabled");
        System.out.println("========================================");
        System.out.println();

        // Auto-connect to server if rdforward.server system property is set.
        // Usage: -Drdforward.server=localhost:25565 -Drdforward.username=Player1
        String serverHost = System.getProperty("rdforward.server", "");
        if (!serverHost.isEmpty()) {
            String host = serverHost;
            int port = 25565;
            if (serverHost.contains(":")) {
                String[] parts = serverHost.split(":", 2);
                host = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port in rdforward.server: " + parts[1]);
                }
            }
            // Empty username → server assigns "Player<ID>" automatically
            String username = System.getProperty("rdforward.username", "");
            System.out.println("Connecting to " + host + ":" + port
                + (username.isEmpty() ? " (server will assign name)..." : " as " + username + "..."));
            RDClient.getInstance().connect(host, port, username);
        }
    }

    /**
     * Called at the start of each render frame.
     * Applies server world data, sends position updates, and applies block changes.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(float partialTick, CallbackInfo ci) {
        MultiplayerState state = MultiplayerState.getInstance();
        RDClient client = RDClient.getInstance();

        // Apply server world data once when it arrives
        if (!rdforward$worldApplied && state.isWorldReady() && level != null) {
            rdforward$applyServerWorld(state);
        }

        if (!client.isConnected()) return;

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
     * Replaces the local Level's block data with the server's world.
     * Maps server block types to RubyDung's binary solid/air system.
     */
    private void rdforward$applyServerWorld(MultiplayerState state) {
        LevelAccessor la = (LevelAccessor) level;
        byte[] localBlocks = la.getBlocks();
        byte[] serverBlocks = state.getWorldBlocks();

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
    }

    /**
     * Called just before glfwSwapBuffers — render remote players.
     * Remote players appear as colored cubes after the world is rendered
     * but before the frame is presented.
     */
    @Inject(method = "render", at = @At(value = "INVOKE",
            target = "Lorg/lwjgl/glfw/GLFW;glfwSwapBuffers(J)V"))
    private void onRenderBeforeSwap(float partialTick, CallbackInfo ci) {
        if (!RDClient.getInstance().isConnected()) return;
        RemotePlayerRenderer.renderAll(partialTick);
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
