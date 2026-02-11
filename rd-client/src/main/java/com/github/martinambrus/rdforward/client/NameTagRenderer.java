package com.github.martinambrus.rdforward.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders player name tags as billboarded text labels above players.
 *
 * Uses Java2D to rasterize each name into an OpenGL texture, then
 * draws a billboard quad at the player's world position. Textures
 * are cached per-name and only regenerated when a new name appears.
 */
public class NameTagRenderer {

    private static final Font FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final Map<String, NameTagTexture> textureCache = new HashMap<>();

    /**
     * Render a name tag above a position in world space.
     * Must be called while the 3D camera is active (modelview + projection set).
     *
     * @param name the player name to render
     * @param x    world X coordinate
     * @param y    world Y coordinate (bottom of player — tag will be placed above head)
     * @param z    world Z coordinate
     */
    public static void renderNameTag(String name, float x, float y, float z) {
        if (name == null || name.isEmpty()) return;

        NameTagTexture tag = textureCache.get(name);
        if (tag == null) {
            tag = createTexture(name);
            textureCache.put(name, tag);
        }
        if (tag.textureId <= 0) return;

        // Position above the player's head (1.8 blocks tall + 0.3 gap above head, minus eye height)
        float tagY = y - 1.62f + 1.8f + 0.3f;

        // Scale: 1 pixel = 1/64 blocks (small, readable at close range)
        float scale = 1.0f / 64.0f;
        float halfWidth = tag.width * scale * 0.5f;
        float halfHeight = tag.height * scale * 0.5f;

        GL11.glPushMatrix();
        GL11.glTranslatef(x, tagY, z);

        // Billboard: extract the modelview matrix and cancel the rotation part
        // so the quad always faces the camera.
        float[] modelview = new float[16];
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, modelview);

        // Reset the 3x3 rotation portion to identity (columns 0-2, rows 0-2)
        // Column 0
        modelview[0] = 1; modelview[1] = 0; modelview[2] = 0;
        // Column 1
        modelview[4] = 0; modelview[5] = 1; modelview[6] = 0;
        // Column 2
        modelview[8] = 0; modelview[9] = 0; modelview[10] = 1;

        // We need to undo the current modelview and apply the billboard version.
        // Instead, we can use a simpler approach: push identity rotation at this point.
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef(x, tagY, z);

        // Get camera-facing vectors from the inverse modelview
        // Simpler approach: scale the quad in clip space using the current matrix
        // Actually, the cleanest fixed-function billboard:
        // 1. Get the current modelview
        // 2. Zero out the rotation columns
        // 3. Load it
        float[] mv = new float[16];
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, mv);
        // Reset rotation (keep translation in column 3)
        float tx = mv[12], ty = mv[13], tz = mv[14];
        // Load identity-ish matrix with only translation
        GL11.glLoadIdentity();
        GL11.glTranslatef(tx, ty, tz);

        // Draw background quad
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-halfWidth - 0.01f, -halfHeight - 0.01f, 0);
        GL11.glVertex3f(halfWidth + 0.01f, -halfHeight - 0.01f, 0);
        GL11.glVertex3f(halfWidth + 0.01f, halfHeight + 0.01f, 0);
        GL11.glVertex3f(-halfWidth - 0.01f, halfHeight + 0.01f, 0);
        GL11.glEnd();

        // Draw text quad
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tag.textureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-halfWidth, -halfHeight, 0);
        GL11.glTexCoord2f(1, 0); GL11.glVertex3f(halfWidth, -halfHeight, 0);
        GL11.glTexCoord2f(1, 1); GL11.glVertex3f(halfWidth, halfHeight, 0);
        GL11.glTexCoord2f(0, 1); GL11.glVertex3f(-halfWidth, halfHeight, 0);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glPopMatrix();
    }

    private static NameTagTexture createTexture(String name) {
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gTmp = tmp.createGraphics();
        gTmp.setFont(FONT);
        FontMetrics fm = gTmp.getFontMetrics();
        int textWidth = fm.stringWidth(name);
        int textHeight = fm.getHeight();
        gTmp.dispose();

        int texW = textWidth + 4;
        int texH = textHeight + 4;

        BufferedImage image = new BufferedImage(texW, texH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(FONT);
        // Shadow
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(name, 3, fm.getAscent() + 3);
        // Main text
        g.setColor(Color.WHITE);
        g.drawString(name, 2, fm.getAscent() + 2);
        g.dispose();

        int[] pixels = new int[texW * texH];
        image.getRGB(0, 0, texW, texH, pixels, 0, texW);

        ByteBuffer buffer = ByteBuffer.allocateDirect(texW * texH * 4).order(ByteOrder.nativeOrder());
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();

        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texW, texH, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        return new NameTagTexture(textureId, texW, texH);
    }

    /**
     * Release all cached textures. Call on shutdown.
     */
    public static void cleanup() {
        for (NameTagTexture tag : textureCache.values()) {
            if (tag.textureId > 0) {
                GL11.glDeleteTextures(tag.textureId);
            }
        }
        textureCache.clear();
    }

    private static class NameTagTexture {
        final int textureId;
        final int width;
        final int height;

        NameTagTexture(int textureId, int width, int height) {
            this.textureId = textureId;
            this.width = width;
            this.height = height;
        }
    }
}
