package net.kyori.adventure.text;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Flat leaf text component. Stub: no children, no styling beyond the
 * optional {@link NamedTextColor}.
 */
public final class TextComponent implements Component {

    private final String content;
    private final NamedTextColor color;

    public TextComponent(String content, NamedTextColor color) {
        this.content = content == null ? "" : content;
        this.color = color;
    }

    @Override public String content() { return content; }

    @Override public NamedTextColor color() { return color; }
}
