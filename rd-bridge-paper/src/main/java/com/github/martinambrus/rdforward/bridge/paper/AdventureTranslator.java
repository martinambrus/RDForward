package com.github.martinambrus.rdforward.bridge.paper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Converts between rd-api's plain-text chat messages and Adventure
 * {@link Component} instances. Every conversion goes through the
 * section-prefixed legacy serializer so colour codes survive the trip.
 */
public final class AdventureTranslator {

    private AdventureTranslator() {}

    public static String toPlainText(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    public static Component toComponent(String plain) {
        return LegacyComponentSerializer.legacySection().deserialize(plain);
    }
}
