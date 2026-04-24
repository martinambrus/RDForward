package com.github.martinambrus.rdforward.bridge.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Converts between rd-api's plain-text chat messages and Adventure
 * {@link Component} instances. The paper-api 26.1.2 stubs return
 * {@code null} from {@link LegacyComponentSerializer#legacySection()},
 * so this class degrades gracefully: plain text conversion goes through
 * the real serializer when one is installed by the host server, and
 * falls back to {@link Component#text(String)} round-tripping otherwise.
 */
public final class AdventureTranslator {

    private AdventureTranslator() {}

    public static String toPlainText(Component component) {
        if (component == null) return "";
        LegacyComponentSerializer s = LegacyComponentSerializer.legacySection();
        if (s != null) {
            String out = s.serialize(component);
            return out == null ? "" : out;
        }
        return "";
    }

    public static Component toComponent(String plain) {
        String safe = plain == null ? "" : plain;
        LegacyComponentSerializer s = LegacyComponentSerializer.legacySection();
        if (s != null) {
            Component c = s.deserialize(safe);
            if (c != null) return c;
        }
        return Component.text(safe);
    }
}
