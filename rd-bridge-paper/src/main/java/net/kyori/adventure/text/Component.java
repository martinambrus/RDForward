package net.kyori.adventure.text;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Minimal Adventure Component. The bridge only carries the subset Paper
 * plugins use for simple chat messages — plain text + optional colour. The
 * two {@code text()} factories mirror Adventure's most common constructors
 * so existing plugin source builds against the stubs.
 */
public interface Component {

    String content();

    NamedTextColor color();

    static TextComponent text(String content) {
        return new TextComponent(content == null ? "" : content, null);
    }

    static TextComponent text(String content, NamedTextColor color) {
        return new TextComponent(content == null ? "" : content, color);
    }
}
