package com.github.martinambrus.rdforward.bridge.paper.unit;

import com.github.martinambrus.rdforward.bridge.paper.AdventureTranslator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AdventureTranslatorTest {

    @Test
    void plainComponentSerializesToContent() {
        Component c = Component.text("hello");
        assertEquals("hello", AdventureTranslator.toPlainText(c));
    }

    @Test
    void coloredComponentEmitsSectionPrefix() {
        Component c = Component.text("red", NamedTextColor.RED);
        assertEquals("§c" + "red", AdventureTranslator.toPlainText(c));
    }

    @Test
    void emptyComponentSerializesToEmptyString() {
        assertEquals("", AdventureTranslator.toPlainText(Component.text("")));
    }

    @Test
    void nullComponentReturnsEmptyString() {
        assertEquals("", AdventureTranslator.toPlainText(null));
    }

    @Test
    void deserializeWrapsPlainStringInComponent() {
        Component c = AdventureTranslator.toComponent("hello world");
        assertNotNull(c);
        assertEquals("hello world", c.content());
    }

    @Test
    void deserializeNullProducesEmptyComponent() {
        Component c = AdventureTranslator.toComponent(null);
        assertEquals("", c.content());
    }

    @Test
    void roundTripForPlainText() {
        String in = "round trip text";
        assertEquals(in, AdventureTranslator.toPlainText(AdventureTranslator.toComponent(in)));
    }
}
