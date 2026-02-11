package com.github.martinambrus.rdforward.client;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Handles chat text input and rendering of the input box.
 *
 * Activated by pressing T while connected to a server. When active:
 * - Mouse cursor is released (ungrabbed) so the player stops moving
 * - Character input is captured via a GLFW char callback
 * - Backspace deletes, Enter sends, Escape cancels
 * - An input box is rendered at the bottom of the screen
 *
 * The chat input integrates with GLFW by installing temporary callbacks
 * when opened and restoring the previous callbacks when closed.
 *
 * Max message length is 64 characters (MC Classic protocol limit).
 */
public class ChatInput {

    private static final int MAX_MESSAGE_LENGTH = 64;
    private static final Font FONT = new Font("SansSerif", Font.PLAIN, 12);

    private static boolean active = false;
    private static final StringBuilder inputBuffer = new StringBuilder();

    /** The GLFW window handle (set once on first open). */
    private static long windowHandle = 0;

    /** Previous GLFW callbacks (restored on close). */
    private static GLFWCharCallback previousCharCallback;
    private static GLFWKeyCallback previousKeyCallback;

    /** GL texture for the input box. */
    private static int textureId = -1;
    private static int texWidth, texHeight;
    private static String lastRenderedText = null;

    /**
     * Open the chat input box.
     * Installs GLFW callbacks for text input. Cursor management is handled
     * by the mixin which has access to the game's internal input state.
     *
     * @param window the GLFW window handle
     */
    public static void open(long window) {
        if (active) return;
        active = true;
        inputBuffer.setLength(0);
        windowHandle = window;

        // Save previous callbacks (may be null)
        previousCharCallback = GLFW.glfwSetCharCallback(window, (w, codepoint) -> {
            onCharTyped(codepoint);
        });
        previousKeyCallback = GLFW.glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
                onKeyPressed(key);
            }
        });

        // Cursor release is handled by the mixin (which manages the game's
        // internal mouseGrabbed/firstMouse state for clean transitions).

        ChatRenderer.setInputOpen(true);
    }

    /**
     * Close the chat input box without sending.
     * Restores previous GLFW callbacks. Cursor re-grab is handled by the mixin.
     */
    public static void close() {
        if (!active) return;
        active = false;

        if (windowHandle != 0) {
            // Restore previous callbacks
            GLFW.glfwSetCharCallback(windowHandle, previousCharCallback);
            GLFW.glfwSetKeyCallback(windowHandle, previousKeyCallback);
            previousCharCallback = null;
            previousKeyCallback = null;

            // Cursor re-grab is handled by the mixin on the next frame
            // (it detects the active→inactive transition and calls grabMouse).
        }

        ChatRenderer.setInputOpen(false);
    }

    /**
     * Whether the chat input is currently active.
     * When active, the game should suppress normal key input (movement, etc.).
     */
    public static boolean isActive() {
        return active;
    }

    /**
     * Get the current input text (for display in the input box).
     */
    public static String getText() {
        return inputBuffer.toString();
    }

    /**
     * Render the chat input box at the bottom of the screen.
     *
     * @param screenWidth  current window width
     * @param screenHeight current window height
     */
    public static void render(int screenWidth, int screenHeight) {
        if (!active) return;

        try {
            renderInternal(screenWidth, screenHeight);
        } catch (Throwable t) {
            System.err.println("[ChatInput] Rendering error: " + t.getMessage());
        }
    }

    private static void renderInternal(int screenWidth, int screenHeight) {
        String displayText = "> " + inputBuffer.toString() + "_";

        // Regenerate texture if text changed
        if (!displayText.equals(lastRenderedText) || textureId == -1) {
            updateTexture(displayText);
            lastRenderedText = displayText;
        }

        if (textureId <= 0) return;

        int padding = 4;
        int x = padding;
        int y = screenHeight - texHeight - padding;
        // Use the full screen width for the input box background
        int boxWidth = screenWidth - padding * 2;

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

        // Draw input box background
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0f, 0.0f, 0.0f, 0.5f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x - 2, y - 2);
        GL11.glVertex2f(x + boxWidth + 2, y - 2);
        GL11.glVertex2f(x + boxWidth + 2, y + texHeight + 2);
        GL11.glVertex2f(x - 2, y + texHeight + 2);
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

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    // -- GLFW callback handlers --

    private static void onCharTyped(int codepoint) {
        if (!active) return;
        if (inputBuffer.length() >= MAX_MESSAGE_LENGTH) return;
        // Only accept printable ASCII characters (MC Classic protocol constraint)
        if (codepoint >= 32 && codepoint < 127) {
            inputBuffer.append((char) codepoint);
        }
    }

    private static void onKeyPressed(int key) {
        if (!active) return;

        switch (key) {
            case GLFW.GLFW_KEY_ENTER:
                send();
                close();
                break;
            case GLFW.GLFW_KEY_ESCAPE:
                close();
                break;
            case GLFW.GLFW_KEY_BACKSPACE:
                if (inputBuffer.length() > 0) {
                    inputBuffer.deleteCharAt(inputBuffer.length() - 1);
                }
                break;
        }
    }

    private static void send() {
        String message = inputBuffer.toString().trim();
        if (!message.isEmpty()) {
            RDClient.getInstance().sendChat(message);
        }
    }

    // -- Texture rendering --

    private static void updateTexture(String text) {
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gTmp = tmp.createGraphics();
        gTmp.setFont(FONT);
        FontMetrics fm = gTmp.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        gTmp.dispose();

        texWidth = textWidth + 4;
        texHeight = textHeight + 4;

        BufferedImage image = new BufferedImage(texWidth, texHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(FONT);
        // Shadow
        g.setColor(new Color(0, 0, 0, 180));
        g.drawString(text, 3, fm.getAscent() + 3);
        // Main text
        g.setColor(Color.WHITE);
        g.drawString(text, 2, fm.getAscent() + 2);
        g.dispose();

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
        lastRenderedText = null;
    }
}
