package com.github.martinambrus.rdforward.client;

import org.lwjgl.opengl.GL11;

import java.util.Collection;

/**
 * Renders remote players as colored cubes in the 3D world.
 *
 * Each player is drawn as a 0.6 x 1.8 x 0.6 block cube (roughly
 * matching Minecraft's player hitbox). Different player IDs get
 * different colors so you can tell players apart.
 *
 * Must be called from the render thread, after the camera is set up
 * but before the buffer swap.
 */
public class RemotePlayerRenderer {

    // Player colors (cycle through for different player IDs)
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

    /**
     * Render all remote players as colored cubes.
     *
     * @param partialTick interpolation factor (0.0 = previous tick, 1.0 = current tick)
     */
    public static void renderAll(float partialTick) {
        MultiplayerState state = MultiplayerState.getInstance();
        Collection<RemotePlayer> players = state.getRemotePlayers();
        if (players.isEmpty()) return;

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        for (RemotePlayer player : players) {
            renderPlayer(player, partialTick);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private static void renderPlayer(RemotePlayer player, float partialTick) {
        float x = player.getInterpolatedX(partialTick);
        float y = player.getInterpolatedY(partialTick);
        float z = player.getInterpolatedZ(partialTick);

        // Player dimensions (roughly matching MC player hitbox)
        float halfWidth = 0.3f;
        float height = 1.8f;

        // Color based on player ID
        float[] color = COLORS[(player.getPlayerId() & 0xFF) % COLORS.length];

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);

        // Draw a solid colored cube centered on Y (player Y = center of bounding box)
        float halfHeight = height / 2;
        GL11.glColor4f(color[0], color[1], color[2], 0.8f);
        drawBox(-halfWidth, -halfHeight, -halfWidth, halfWidth, halfHeight, halfWidth);

        // Draw a darker wireframe outline
        GL11.glColor4f(color[0] * 0.5f, color[1] * 0.5f, color[2] * 0.5f, 1.0f);
        GL11.glLineWidth(2.0f);
        drawBoxOutline(-halfWidth, -halfHeight, -halfWidth, halfWidth, halfHeight, halfWidth);

        GL11.glPopMatrix();

        // Reset color
        GL11.glColor4f(1, 1, 1, 1);
    }

    private static void drawBox(float x0, float y0, float z0, float x1, float y1, float z1) {
        GL11.glBegin(GL11.GL_QUADS);

        // Bottom
        GL11.glVertex3f(x0, y0, z0);
        GL11.glVertex3f(x1, y0, z0);
        GL11.glVertex3f(x1, y0, z1);
        GL11.glVertex3f(x0, y0, z1);

        // Top
        GL11.glVertex3f(x0, y1, z0);
        GL11.glVertex3f(x0, y1, z1);
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x1, y1, z0);

        // Front (-Z)
        GL11.glVertex3f(x0, y0, z0);
        GL11.glVertex3f(x0, y1, z0);
        GL11.glVertex3f(x1, y1, z0);
        GL11.glVertex3f(x1, y0, z0);

        // Back (+Z)
        GL11.glVertex3f(x0, y0, z1);
        GL11.glVertex3f(x1, y0, z1);
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x0, y1, z1);

        // Left (-X)
        GL11.glVertex3f(x0, y0, z0);
        GL11.glVertex3f(x0, y0, z1);
        GL11.glVertex3f(x0, y1, z1);
        GL11.glVertex3f(x0, y1, z0);

        // Right (+X)
        GL11.glVertex3f(x1, y0, z0);
        GL11.glVertex3f(x1, y1, z0);
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x1, y0, z1);

        GL11.glEnd();
    }

    private static void drawBoxOutline(float x0, float y0, float z0, float x1, float y1, float z1) {
        GL11.glBegin(GL11.GL_LINES);

        // Bottom edges
        GL11.glVertex3f(x0, y0, z0); GL11.glVertex3f(x1, y0, z0);
        GL11.glVertex3f(x1, y0, z0); GL11.glVertex3f(x1, y0, z1);
        GL11.glVertex3f(x1, y0, z1); GL11.glVertex3f(x0, y0, z1);
        GL11.glVertex3f(x0, y0, z1); GL11.glVertex3f(x0, y0, z0);

        // Top edges
        GL11.glVertex3f(x0, y1, z0); GL11.glVertex3f(x1, y1, z0);
        GL11.glVertex3f(x1, y1, z0); GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x1, y1, z1); GL11.glVertex3f(x0, y1, z1);
        GL11.glVertex3f(x0, y1, z1); GL11.glVertex3f(x0, y1, z0);

        // Vertical edges
        GL11.glVertex3f(x0, y0, z0); GL11.glVertex3f(x0, y1, z0);
        GL11.glVertex3f(x1, y0, z0); GL11.glVertex3f(x1, y1, z0);
        GL11.glVertex3f(x1, y0, z1); GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x0, y0, z1); GL11.glVertex3f(x0, y1, z1);

        GL11.glEnd();
    }
}
