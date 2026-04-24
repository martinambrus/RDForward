package com.github.martinambrus.rdforward.api.client;

/**
 * 2D rendering helper handed to {@code RENDER_HUD} callbacks. Wraps the
 * client's text and quad pipeline so mods can draw overlays without making
 * raw GL calls. Colors are ARGB ({@code 0xAARRGGBB}). Coordinates are in
 * screen pixels with origin at the top-left.
 */
public interface DrawContext {

    void drawText(String text, int x, int y, int color);

    void drawTextWithShadow(String text, int x, int y, int color);

    int getTextWidth(String text);

    int getTextHeight();

    void fillRect(int x, int y, int width, int height, int color);

    /** Draw a quad textured with an existing GL texture id. */
    void drawTexture(int textureId, int x, int y, int width, int height);

    int getScreenWidth();

    int getScreenHeight();
}
