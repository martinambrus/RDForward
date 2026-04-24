package net.kyori.adventure.text.format;

/**
 * Stub — Paper plugins occasionally reference {@code Style} via Adventure
 * static factories. The bridge exposes only an {@code EMPTY} singleton; no
 * decorations (bold / italic / strikethrough) are modelled.
 */
public final class Style {

    public static final Style EMPTY = new Style();

    private Style() {}
}
