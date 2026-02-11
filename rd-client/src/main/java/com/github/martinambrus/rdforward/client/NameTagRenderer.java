package com.github.martinambrus.rdforward.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
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
     * @param y    world Y coordinate (eye level — tag placed above head)
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

        // Position above the player's head (1.8 blocks tall + 0.3 gap, minus 1.62 eye height)
        float tagY = y - 1.62f + 1.8f + 0.3f;

        // Scale: 1 pixel = 1/64 blocks (small, readable at close range)
        float scale = 1.0f / 64.0f;
        float halfWidth = tag.width * scale * 0.5f;
        float halfHeight = tag.height * scale * 0.5f;

        // Billboard technique: translate to world position, then extract the
        // eye-space translation from the modelview matrix and replace the
        // full matrix with identity + that translation. This removes all
        // rotation so the quad always faces the camera.
        GL11.glPushMatrix();
        GL11.glTranslatef(x, tagY, z);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer mv = stack.mallocFloat(16);
            GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, mv);
            float tx = mv.get(12);
            float ty = mv.get(13);
            float tz = mv.get(14);

            // Replace modelview with identity + translation (kills rotation = billboard)
            GL11.glLoadIdentity();
            GL11.glTranslatef(tx, ty, tz);
        }

        // Draw semi-transparent background
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-halfWidth - 0.01f, -halfHeight - 0.01f, 0);
        GL11.glVertex3f(halfWidth + 0.01f, -halfHeight - 0.01f, 0);
        GL11.glVertex3f(halfWidth + 0.01f, halfHeight + 0.01f, 0);
        GL11.glVertex3f(-halfWidth - 0.01f, halfHeight + 0.01f, 0);
        GL11.glEnd();

        // Draw text texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tag.textureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_QUADS);
        // V coordinates flipped: Java2D pixel origin is top-left, OpenGL texture origin is bottom-left
        GL11.glTexCoord2f(0, 1); GL11.glVertex3f(-halfWidth, -halfHeight, 0);
        GL11.glTexCoord2f(1, 1); GL11.glVertex3f(halfWidth, -halfHeight, 0);
        GL11.glTexCoord2f(1, 0); GL11.glVertex3f(halfWidth, halfHeight, 0);
        GL11.glTexCoord2f(0, 0); GL11.glVertex3f(-halfWidth, halfHeight, 0);
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
