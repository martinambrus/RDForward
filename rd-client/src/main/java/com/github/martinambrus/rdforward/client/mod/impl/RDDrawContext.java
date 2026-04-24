package com.github.martinambrus.rdforward.client.mod.impl;

import com.github.martinambrus.rdforward.api.client.DrawContext;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Rasterizes text to GL textures through Java2D (same pipeline used by
 * {@link com.github.martinambrus.rdforward.client.HudRenderer} and
 * {@link com.github.martinambrus.rdforward.client.ChatRenderer}) and draws
 * it as screen-space quads. Rect and texture primitives go straight through
 * immediate-mode GL.
 *
 * <p>One instance is created per frame by the mixin render hook, bound to
 * the current screen dimensions, and handed to every {@code RENDER_HUD}
 * listener. The static {@code textCache} is shared across frames so
 * unchanging text (e.g. a coordinate overlay) does not re-rasterize.
 *
 * <p>This context assumes the caller has already set up an orthographic
 * projection for the HUD pass (as does the existing HUD render path).
 */
public final class RDDrawContext implements DrawContext {

    private static final Font FONT = new Font("SansSerif", Font.BOLD, 14);
    private static final int CACHE_LIMIT = 128;

    /** Text cache keyed by (text, color, shadow). LinkedHashMap gives LRU via access-order. */
    private static final Map<String, CachedText> textCache =
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CachedText> eldest) {
                    if (size() > CACHE_LIMIT) {
                        GL11.glDeleteTextures(eldest.getValue().textureId);
                        return true;
                    }
                    return false;
                }
            };

    private final int screenWidth;
    private final int screenHeight;

    public RDDrawContext(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    @Override public int getScreenWidth() { return screenWidth; }
    @Override public int getScreenHeight() { return screenHeight; }

    @Override
    public void drawText(String text, int x, int y, int color) {
        drawTextImpl(text, x, y, color, false);
    }

    @Override
    public void drawTextWithShadow(String text, int x, int y, int color) {
        drawTextImpl(text, x, y, color, true);
    }

    @Override
    public int getTextWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setFont(FONT);
        int w = g.getFontMetrics().stringWidth(text);
        g.dispose();
        return w;
    }

    @Override
    public int getTextHeight() {
        BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tmp.createGraphics();
        g.setFont(FONT);
        int h = g.getFontMetrics().getHeight();
        g.dispose();
        return h;
    }

    @Override
    public void fillRect(int x, int y, int width, int height, int color) {
        float a = ((color >> 24) & 0xFF) / 255f;
        float r = ((color >> 16) & 0xFF) / 255f;
        float gcol = ((color >> 8) & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(r, gcol, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);
        GL11.glEnd();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    @Override
    public void drawTexture(int textureId, int x, int y, int width, int height) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + width, y);
        GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + width, y + height);
        GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y + height);
        GL11.glEnd();
    }

    private void drawTextImpl(String text, int x, int y, int color, boolean shadow) {
        if (text == null || text.isEmpty()) return;
        CachedText ct = cached(text, color, shadow);
        drawTexture(ct.textureId, x, y, ct.width, ct.height);
    }

    private static synchronized CachedText cached(String text, int color, boolean shadow) {
        String key = (shadow ? "s|" : "p|") + Integer.toHexString(color) + "|" + text;
        CachedText existing = textCache.get(key);
        if (existing != null) return existing;
        CachedText ct = rasterize(text, color, shadow);
        textCache.put(key, ct);
        return ct;
    }

    private static CachedText rasterize(String text, int color, boolean shadow) {
        BufferedImage measure = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D mg = measure.createGraphics();
        mg.setFont(FONT);
        FontMetrics fm = mg.getFontMetrics();
        int w = fm.stringWidth(text) + (shadow ? 2 : 0);
        int h = fm.getHeight() + (shadow ? 2 : 0);
        int ascent = fm.getAscent();
        mg.dispose();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(FONT);
        if (shadow) {
            g.setColor(new Color(0, 0, 0, 200));
            g.drawString(text, 2, ascent + 2);
        }
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int gc = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        g.setColor(new Color(r, gc, b, a == 0 ? 255 : a));
        g.drawString(text, shadow ? 1 : 0, ascent);
        g.dispose();

        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);
        ByteBuffer buf = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder());
        for (int px : pixels) {
            buf.put((byte) ((px >> 16) & 0xFF));
            buf.put((byte) ((px >> 8) & 0xFF));
            buf.put((byte) (px & 0xFF));
            buf.put((byte) ((px >> 24) & 0xFF));
        }
        buf.flip();

        int tex = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        return new CachedText(tex, w, h);
    }

    private record CachedText(int textureId, int width, int height) {}
}
