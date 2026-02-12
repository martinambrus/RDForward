package com.github.martinambrus.rdforward.android;

import com.github.martinambrus.rdforward.android.multiplayer.MultiplayerState;
import com.github.martinambrus.rdforward.android.multiplayer.RemotePlayer;
import com.github.martinambrus.rdforward.render.BlendFactor;
import com.github.martinambrus.rdforward.render.libgdx.LibGDXGraphics;

import java.util.Collection;

/**
 * Renders remote players as colored cubes using LibGDXGraphics.
 * Must be called while the 3D camera is set up (after chunk rendering,
 * before switching to 2D overlays).
 */
public class RemotePlayerRenderer {

    private static final float[][] COLORS = {
        {1.0f, 0.2f, 0.2f},  // Red
        {0.2f, 0.6f, 1.0f},  // Blue
        {0.2f, 1.0f, 0.2f},  // Green
        {1.0f, 1.0f, 0.2f},  // Yellow
        {1.0f, 0.5f, 0.0f},  // Orange
        {0.8f, 0.2f, 1.0f},  // Purple
        {0.0f, 1.0f, 1.0f},  // Cyan
        {1.0f, 0.6f, 0.8f},  // Pink
    };

    public static void renderAll(LibGDXGraphics graphics, float partialTick) {
        Collection<RemotePlayer> players = MultiplayerState.getInstance().getRemotePlayers();
        if (players.isEmpty()) return;

        graphics.disableTexture2D();
        graphics.enableBlend();
        graphics.blendFunc(BlendFactor.SRC_ALPHA, BlendFactor.ONE_MINUS_SRC_ALPHA);

        for (RemotePlayer player : players) {
            renderPlayer(graphics, player);
        }

        graphics.disableBlend();
        graphics.enableTexture2D();
        graphics.setColor(1, 1, 1, 1);
    }

    private static void renderPlayer(LibGDXGraphics graphics, RemotePlayer player) {
        // Convert fixed-point (blocks * 32) to float block coordinates
        float x = player.getX() / 32.0f;
        float y = player.getY() / 32.0f;
        float z = player.getZ() / 32.0f;

        float halfWidth = 0.3f;
        float height = 1.8f;
        float eyeHeight = 1.62f;

        float[] color = COLORS[(player.getPlayerId() & 0xFF) % COLORS.length];

        graphics.pushMatrix();
        graphics.translate(x, y, z);

        float x0 = -halfWidth, y0 = -eyeHeight, z0 = -halfWidth;
        float x1 = halfWidth, y1 = -eyeHeight + height, z1 = halfWidth;

        // Solid cube
        graphics.setColor(color[0], color[1], color[2], 0.8f);
        graphics.beginQuads();

        // Bottom
        graphics.vertex3f(x0, y0, z0);
        graphics.vertex3f(x1, y0, z0);
        graphics.vertex3f(x1, y0, z1);
        graphics.vertex3f(x0, y0, z1);

        // Top
        graphics.vertex3f(x0, y1, z0);
        graphics.vertex3f(x0, y1, z1);
        graphics.vertex3f(x1, y1, z1);
        graphics.vertex3f(x1, y1, z0);

        // Front (-Z)
        graphics.vertex3f(x0, y0, z0);
        graphics.vertex3f(x0, y1, z0);
        graphics.vertex3f(x1, y1, z0);
        graphics.vertex3f(x1, y0, z0);

        // Back (+Z)
        graphics.vertex3f(x0, y0, z1);
        graphics.vertex3f(x1, y0, z1);
        graphics.vertex3f(x1, y1, z1);
        graphics.vertex3f(x0, y1, z1);

        // Left (-X)
        graphics.vertex3f(x0, y0, z0);
        graphics.vertex3f(x0, y0, z1);
        graphics.vertex3f(x0, y1, z1);
        graphics.vertex3f(x0, y1, z0);

        // Right (+X)
        graphics.vertex3f(x1, y0, z0);
        graphics.vertex3f(x1, y1, z0);
        graphics.vertex3f(x1, y1, z1);
        graphics.vertex3f(x1, y0, z1);

        graphics.endDraw();

        // Wireframe outline
        graphics.setColor(color[0] * 0.5f, color[1] * 0.5f, color[2] * 0.5f, 1.0f);
        graphics.beginLines();

        // Bottom edges
        graphics.vertex3f(x0, y0, z0); graphics.vertex3f(x1, y0, z0);
        graphics.vertex3f(x1, y0, z0); graphics.vertex3f(x1, y0, z1);
        graphics.vertex3f(x1, y0, z1); graphics.vertex3f(x0, y0, z1);
        graphics.vertex3f(x0, y0, z1); graphics.vertex3f(x0, y0, z0);

        // Top edges
        graphics.vertex3f(x0, y1, z0); graphics.vertex3f(x1, y1, z0);
        graphics.vertex3f(x1, y1, z0); graphics.vertex3f(x1, y1, z1);
        graphics.vertex3f(x1, y1, z1); graphics.vertex3f(x0, y1, z1);
        graphics.vertex3f(x0, y1, z1); graphics.vertex3f(x0, y1, z0);

        // Vertical edges
        graphics.vertex3f(x0, y0, z0); graphics.vertex3f(x0, y1, z0);
        graphics.vertex3f(x1, y0, z0); graphics.vertex3f(x1, y1, z0);
        graphics.vertex3f(x1, y0, z1); graphics.vertex3f(x1, y1, z1);
        graphics.vertex3f(x0, y0, z1); graphics.vertex3f(x0, y1, z1);

        graphics.endDraw();

        graphics.popMatrix();
    }
}
