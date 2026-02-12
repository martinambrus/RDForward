package com.github.martinambrus.rdforward.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Renders a centered welcome/title menu overlay on the desktop client.
 * Uses Java2D to rasterize text into an OpenGL texture, same approach
 * as {@link HudRenderer}.
 */
public class MenuRenderer {

    private static int textureId = -1;
    private static int texWidth, texHeight;

    /**
     * Renders the menu overlay centered on screen with a dark background.
     */
    public static void render(int screenWidth, int screenHeight) {
        if (textureId == -1) {
            createTexture();
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

        // Dark overlay covering the whole screen
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.6f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(screenWidth, 0);
        GL11.glVertex2f(screenWidth, screenHeight);
        GL11.glVertex2f(0, screenHeight);
        GL11.glEnd();

        // Center the menu card
        float x = (screenWidth - texWidth) / 2.0f;
        float y = (screenHeight - texHeight) / 2.0f;

        // Semi-transparent background for the menu card
        float pad = 24;
        GL11.glColor4f(0.1f, 0.1f, 0.1f, 0.85f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x - pad, y - pad);
        GL11.glVertex2f(x + texWidth + pad, y - pad);
        GL11.glVertex2f(x + texWidth + pad, y + texHeight + pad);
        GL11.glVertex2f(x - pad, y + texHeight + pad);
        GL11.glEnd();

        // Draw text texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + texWidth, y);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + texWidth, y + texHeight);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + texHeight);
        GL11.glEnd();

        // Restore GL state
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    private static void createTexture() {
        Font titleFont = new Font("SansSerif", Font.BOLD, 28);
        Font optionFont = new Font("SansSerif", Font.PLAIN, 20);
        Font hintFont = new Font("SansSerif", Font.ITALIC, 14);

        String title = "RDForward";
        String opt1 = "[1] Single Player";
        String opt2 = "[2] Multiplayer";
        String hint = "Press 1 or 2 to select";

        // Measure text dimensions
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gTmp = tmp.createGraphics();
        FontMetrics fmTitle = gTmp.getFontMetrics(titleFont);
        FontMetrics fmOpt = gTmp.getFontMetrics(optionFont);
        FontMetrics fmHint = gTmp.getFontMetrics(hintFont);

        int maxWidth = Math.max(fmTitle.stringWidth(title),
                Math.max(fmOpt.stringWidth(opt1),
                        Math.max(fmOpt.stringWidth(opt2), fmHint.stringWidth(hint))));

        int lineSpacing = 12;
        int sectionGap = 28;
        int totalHeight = fmTitle.getHeight() + sectionGap
                + fmOpt.getHeight() + lineSpacing + fmOpt.getHeight()
                + sectionGap + fmHint.getHeight();
        gTmp.dispose();

        texWidth = maxWidth + 20;
        texHeight = totalHeight + 20;

        // Render text to image
        BufferedImage image = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int y = 10;

        // Title (yellow, centered, with shadow)
        g.setFont(titleFont);
        int titleX = (texWidth - fmTitle.stringWidth(title)) / 2;
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(title, titleX + 2, y + fmTitle.getAscent() + 2);
        g.setColor(new Color(255, 255, 100));
        g.drawString(title, titleX, y + fmTitle.getAscent());
        y += fmTitle.getHeight() + sectionGap;

        // Option 1 (white, centered, with shadow)
        g.setFont(optionFont);
        int opt1X = (texWidth - fmOpt.stringWidth(opt1)) / 2;
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(opt1, opt1X + 1, y + fmOpt.getAscent() + 1);
        g.setColor(Color.WHITE);
        g.drawString(opt1, opt1X, y + fmOpt.getAscent());
        y += fmOpt.getHeight() + lineSpacing;

        // Option 2 (white, centered, with shadow)
        int opt2X = (texWidth - fmOpt.stringWidth(opt2)) / 2;
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(opt2, opt2X + 1, y + fmOpt.getAscent() + 1);
        g.setColor(Color.WHITE);
        g.drawString(opt2, opt2X, y + fmOpt.getAscent());
        y += fmOpt.getHeight() + sectionGap;

        // Hint (gray, centered)
        g.setFont(hintFont);
        int hintX = (texWidth - fmHint.stringWidth(hint)) / 2;
        g.setColor(new Color(180, 180, 180));
        g.drawString(hint, hintX, y + fmHint.getAscent());

        g.dispose();

        // Convert image to RGBA byte buffer
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
        textureId = GL11.glGenTextures();
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
        }
    }
}
