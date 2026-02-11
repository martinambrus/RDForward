package com.github.martinambrus.rdforward.mixin;

import com.github.martinambrus.rdforward.client.MultiplayerState;
import com.github.martinambrus.rdforward.client.RDClient;
import com.github.martinambrus.rdforward.client.RemotePlayerRenderer;
import com.mojang.rubydung.Player;
import com.mojang.rubydung.RubyDung;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    /** Frame counter for throttling position updates. */
    private int rdforward$tickCounter = 0;

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
     * Sends position updates and applies pending block changes from the server.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderHead(float partialTick, CallbackInfo ci) {
        RDClient client = RDClient.getInstance();
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
        MultiplayerState state = MultiplayerState.getInstance();
        MultiplayerState.BlockChange change;
        while ((change = state.pollBlockChange()) != null) {
            // Block changes from the server are tracked in MultiplayerState.
            // Full level integration will be added when Level Mixin is implemented.
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
