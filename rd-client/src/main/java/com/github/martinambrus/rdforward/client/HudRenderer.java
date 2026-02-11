package com.github.martinambrus.rdforward.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Renders a text overlay in the top-left corner of the game window.
 *
 * Uses Java2D to rasterize text into an OpenGL texture, then draws
 * it as a screen-space quad with alpha blending. The texture is only
 * regenerated when the text changes.
 */
public class HudRenderer {

    private static int textureId = -1;
    private static int texWidth, texHeight;
    private static String lastText = "";

    /**
     * Draws text in the top-left corner with a semi-transparent background.
     *
     * @param text         the text to display
     * @param screenWidth  current window width in pixels
     * @param screenHeight current window height in pixels
     */
    public static void drawText(String text, int screenWidth, int screenHeight) {
        if (text == null || text.isEmpty()) return;

        try {
            drawTextInternal(text, screenWidth, screenHeight);
        } catch (Throwable t) {
            // Don't let HUD errors crash the game
            if (!errorReported) {
                System.err.println("[HUD] Rendering error: " + t.getMessage());
                t.printStackTrace();
                errorReported = true;
            }
        }
    }

    private static boolean errorReported = false;

    private static void drawTextInternal(String text, int screenWidth, int screenHeight) {
        // Regenerate texture if text changed
        if (!text.equals(lastText) || textureId == -1) {
            updateTexture(text);
            lastText = text;
        }

        if (textureId <= 0) return;

        // Save GL state
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, screenWidth, screenHeight, 0, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int padding = 4;
        int x = padding;
        int y = padding;
        int drawWidth = texWidth;
        int drawHeight = texHeight;

        // Draw semi-transparent background
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.4f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x - 2, y - 2);
        GL11.glVertex2f(x + drawWidth + 2, y - 2);
        GL11.glVertex2f(x + drawWidth + 2, y + drawHeight + 2);
        GL11.glVertex2f(x - 2, y + drawHeight + 2);
        GL11.glEnd();

        // Draw text texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + drawWidth, y);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + drawWidth, y + drawHeight);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + drawHeight);
        GL11.glEnd();

        // Restore GL state
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void updateTexture(String text) {
        // Measure text dimensions using a temporary Graphics2D
        Font font = new Font("SansSerif", Font.BOLD, 14);
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gTmp = tmp.createGraphics();
        gTmp.setFont(font);
        FontMetrics fm = gTmp.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        gTmp.dispose();

        // Round up to reasonable size
        texWidth = textWidth + 4;
        texHeight = textHeight + 4;

        // Render text to image
        BufferedImage image = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(font);

        // Shadow for readability
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(text, 3, fm.getAscent() + 3);
        // Main text
        g.setColor(Color.WHITE);
        g.drawString(text, 2, fm.getAscent() + 2);
        g.dispose();

        // Convert image pixels to RGBA byte buffer
        int[] pixels = new int[texWidth * texHeight];
        image.getRGB(0, 0, texWidth, texHeight, pixels, 0, texWidth);

        ByteBuffer buffer = ByteBuffer.allocateDirect(texWidth * texHeight * 4)
                .order(ByteOrder.nativeOrder());
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
            buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
            buffer.put((byte) (pixel & 0xFF));          // B
            buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
        }
        buffer.flip();

        // Upload to OpenGL
        if (textureId == -1) {
            textureId = GL11.glGenTextures();
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, texWidth, texHeight, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
    }

    /**
     * Releases the GL texture. Call on shutdown.
     */
    public static void cleanup() {
        if (textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
            lastText = "";
        }
    }
}
