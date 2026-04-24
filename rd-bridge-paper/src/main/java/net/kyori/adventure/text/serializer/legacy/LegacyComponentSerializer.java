package net.kyori.adventure.text.serializer.legacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Converts between {@link Component} and the legacy {@code §}-prefixed
 * colour-code string form used by the pre-Adventure Bukkit chat pipeline.
 * The bridge uses this to round-trip messages between Paper plugins (which
 * operate on Components) and {@code ServerEvents.CHAT} (which carries
 * plain-text strings).
 */
public final class LegacyComponentSerializer {

    private static final LegacyComponentSerializer SECTION    = new LegacyComponentSerializer('§');
    private static final LegacyComponentSerializer AMPERSAND  = new LegacyComponentSerializer('&');

    private final char prefix;

    private LegacyComponentSerializer(char prefix) {
        this.prefix = prefix;
    }

    public static LegacyComponentSerializer legacySection() { return SECTION; }

    public static LegacyComponentSerializer legacyAmpersand() { return AMPERSAND; }

    public String serialize(Component component) {
        if (component == null) return "";
        StringBuilder sb = new StringBuilder();
        appendComponent(component, sb);
        return sb.toString();
    }

    public Component deserialize(String input) {
        return Component.text(input == null ? "" : input);
    }

    private void appendComponent(Component component, StringBuilder sb) {
        if (component instanceof TextComponent t) {
            NamedTextColor color = t.color();
            if (color != null) sb.append(prefix).append(color.code());
            sb.append(t.content());
            return;
        }
        String content = component.content();
        if (content != null) sb.append(content);
    }
}
