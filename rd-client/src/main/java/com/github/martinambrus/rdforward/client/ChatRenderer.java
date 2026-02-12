package com.github.martinambrus.rdforward.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Renders chat messages in the bottom-left corner of the game window.
 *
 * Messages are added via {@link #addMessage(String)} and automatically
 * fade out after {@link #DISPLAY_DURATION_MS} milliseconds. While the
 * chat input is open, all recent messages are shown without fading.
 *
 * Uses Java2D for text rasterization (same approach as HudRenderer
 * and NameTagRenderer) with per-message texture caching. Textures
 * are regenerated only when the visible message set changes.
 */
public class ChatRenderer {

    /** How long a message stays visible before fading (milliseconds). */
    private static final long DISPLAY_DURATION_MS = 10_000;

    /** Duration of the fade-out animation (milliseconds). */
    private static final long FADE_DURATION_MS = 2_000;

    /** Maximum messages shown at once. */
    private static final int MAX_VISIBLE = 10;

    /** Font for chat text. */
    private static final Font FONT = new Font("SansSerif", Font.BOLD, 14);

    /** All recent messages with timestamps. */
    private static final List<ChatEntry> messages = new ArrayList<>();

    /** Whether the chat input box is currently open (suppresses fade). */
    private static boolean inputOpen = false;

    /** Cached GL texture, regenerated when visible text changes. */
    private static int textureId = -1;
    private static int texWidth, texHeight;
    private static String lastRenderedKey = "";

    /**
     * Add a new chat message to the display.
     */
    public static void addMessage(String message) {
        messages.add(new ChatEntry(message, System.currentTimeMillis()));
        // Trim old messages to prevent unbounded growth
        while (messages.size() > 100) {
            messages.remove(0);
        }
    }

    /**
     * Whether there are any messages to display (including fading ones).
     */
    public static boolean isEmpty() {
        return messages.isEmpty();
    }

    /**
     * Set whether the chat input box is open (suppresses message fade-out).
     */
    public static void setInputOpen(boolean open) {
        inputOpen = open;
    }

    /**
     * Render visible chat messages at the bottom-left of the screen.
     *
     * @param screenWidth  current window width in pixels
     * @param screenHeight current window height in pixels
     */
    public static void render(int screenWidth, int screenHeight) {
        List<VisibleMessage> visible = getVisibleMessages();
        if (visible.isEmpty()) return;

        try {
            renderInternal(visible, screenWidth, screenHeight);
        } catch (Throwable t) {
            // Don't crash the game for rendering errors
            System.err.println("[Chat] Rendering error: " + t.getMessage());
        }
    }

    private static void renderInternal(List<VisibleMessage> visible, int screenWidth, int screenHeight) {
        // Build a key for cache invalidation
        StringBuilder keyBuilder = new StringBuilder();
        for (VisibleMessage vm : visible) {
            keyBuilder.append(vm.text).append('\n');
        }
        String key = keyBuilder.toString();

        // Regenerate texture if content changed
        if (!key.equals(lastRenderedKey) || textureId == -1) {
            updateTexture(visible);
            lastRenderedKey = key;
        }

        if (textureId <= 0 || texWidth <= 0 || texHeight <= 0) return;

        // Calculate minimum alpha across all visible messages (for global fade)
        float minAlpha = 1.0f;
        for (VisibleMessage vm : visible) {
            if (vm.alpha < minAlpha) minAlpha = vm.alpha;
        }

        // Position: bottom-left, above the chat input area
        int padding = 4;
        int x = padding;
        int inputBoxHeight = inputOpen ? 22 : 0;
        int y = screenHeight - texHeight - padding - inputBoxHeight;

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

        // Draw semi-transparent background
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f * minAlpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x - 2, y - 2);
        GL11.glVertex2f(x + texWidth + 2, y - 2);
        GL11.glVertex2f(x + texWidth + 2, y + texHeight + 2);
        GL11.glVertex2f(x - 2, y + texHeight + 2);
        GL11.glEnd();

        // Draw chat text texture
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, minAlpha);
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

    /**
     * Build the list of messages that should be visible right now.
     */
    private static List<VisibleMessage> getVisibleMessages() {
        long now = System.currentTimeMillis();
        List<VisibleMessage> visible = new ArrayList<>();

        // Iterate from newest to oldest, collecting up to MAX_VISIBLE
        for (int i = messages.size() - 1; i >= 0 && visible.size() < MAX_VISIBLE; i--) {
            ChatEntry entry = messages.get(i);
            long age = now - entry.timestamp;

            if (inputOpen) {
                // When chat input is open, show all recent messages at full opacity
                visible.add(0, new VisibleMessage(entry.message, 1.0f));
            } else if (age < DISPLAY_DURATION_MS) {
                // Message is fresh — full opacity
                visible.add(0, new VisibleMessage(entry.message, 1.0f));
            } else if (age < DISPLAY_DURATION_MS + FADE_DURATION_MS) {
                // Message is fading out
                float alpha = 1.0f - (float) (age - DISPLAY_DURATION_MS) / FADE_DURATION_MS;
                visible.add(0, new VisibleMessage(entry.message, alpha));
            }
            // Else: fully faded, skip
        }

        return visible;
    }

    private static void updateTexture(List<VisibleMessage> visible) {
        if (visible.isEmpty()) {
            texWidth = 0;
            texHeight = 0;
            return;
        }

        // Measure text dimensions
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gTmp = tmp.createGraphics();
        gTmp.setFont(FONT);
        FontMetrics fm = gTmp.getFontMetrics();

        int lineHeight = fm.getHeight();
        int maxWidth = 0;
        for (VisibleMessage vm : visible) {
            int w = fm.stringWidth(vm.text);
            if (w > maxWidth) maxWidth = w;
        }
        gTmp.dispose();

        texWidth = maxWidth + 4;
        texHeight = lineHeight * visible.size() + 4;

        // Render all lines to image
        BufferedImage image = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(FONT);

        for (int i = 0; i < visible.size(); i++) {
            int textY = fm.getAscent() + 2 + i * lineHeight;
            // Shadow
            g.setColor(new Color(0, 0, 0, 180));
            g.drawString(visible.get(i).text, 3, textY + 1);
            // Main text
            g.setColor(Color.WHITE);
            g.drawString(visible.get(i).text, 2, textY);
        }
        g.dispose();

        // Convert to RGBA byte buffer
        int[] pixels = new int[texWidth * texHeight];
        image.getRGB(0, 0, texWidth, texHeight, pixels, 0, texWidth);

        ByteBuffer buffer = ByteBuffer.allocateDirect(texWidth * texHeight * 4)
                .order(ByteOrder.nativeOrder());
        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
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
     * Release GL resources. Call on shutdown.
     */
    public static void cleanup() {
        if (textureId != -1) {
            GL11.glDeleteTextures(textureId);
            textureId = -1;
        }
        messages.clear();
        lastRenderedKey = "";
    }

    /** A chat message with its arrival timestamp. */
    private static class ChatEntry {
        final String message;
        final long timestamp;

        ChatEntry(String message, long timestamp) {
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    /** A message visible in the current frame with its computed alpha. */
    private static class VisibleMessage {
        final String text;
        final float alpha;

        VisibleMessage(String text, float alpha) {
            this.text = text;
            this.alpha = alpha;
        }
    }
}
