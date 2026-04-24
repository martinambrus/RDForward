package com.github.martinambrus.rdforward.bridge.neoforge.unit;

import com.github.martinambrus.rdforward.bridge.neoforge.NeoForgeModDescriptor;
import com.github.martinambrus.rdforward.bridge.neoforge.NeoForgeModsTomlParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NeoForgeModsTomlParserTest {

    private static NeoForgeModDescriptor parse(String toml) {
        return NeoForgeModsTomlParser.parse(new ByteArrayInputStream(toml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesMinimalNeoForgeToml() {
        NeoForgeModDescriptor d = parse("""
                modLoader = "javafml"
                loaderVersion = "[4,)"
                license = "LGPL-3.0"

                [[mods]]
                modId = "neomod"
                version = "2.0.0"
                displayName = "NeoMod"
                """);
        assertEquals("javafml", d.modLoader());
        assertEquals("[4,)", d.loaderVersion());
        assertEquals("LGPL-3.0", d.license());
        assertEquals(1, d.mods().size());
        NeoForgeModDescriptor.Entry e = d.mods().get(0);
        assertEquals("neomod", e.modId());
        assertEquals("2.0.0", e.version());
        assertEquals("NeoMod", e.displayName());
    }

    @Test
    void mainClassCapturedWhenDeclared() {
        NeoForgeModDescriptor d = parse("""
                [[mods]]
                modId = "noanno"
                version = "1.0"
                mainClass = "com.example.NoAnnotationMod"
                """);
        assertEquals("com.example.NoAnnotationMod", d.mods().get(0).mainClass());
    }

    @Test
    void mainClassNullWhenAbsent() {
        NeoForgeModDescriptor d = parse("[[mods]]\nmodId = \"x\"\n");
        assertNull(d.mods().get(0).mainClass());
    }

    @Test
    void versionDefaultsTo1_0_0WhenOmitted() {
        NeoForgeModDescriptor d = parse("[[mods]]\nmodId = \"x\"\n");
        assertEquals("1.0.0", d.mods().get(0).version());
    }

    @Test
    void defaultsPopulateWhenRootFieldsAbsent() {
        NeoForgeModDescriptor d = parse("[[mods]]\nmodId = \"x\"\n");
        assertEquals("javafml", d.modLoader());
        assertEquals("", d.loaderVersion());
        assertEquals("", d.license());
    }

    @Test
    void multipleModEntriesPreservedInOrder() {
        NeoForgeModDescriptor d = parse("""
                [[mods]]
                modId = "first"
                [[mods]]
                modId = "second"
                [[mods]]
                modId = "third"
                """);
        assertEquals(3, d.mods().size());
        assertEquals("first", d.mods().get(0).modId());
        assertEquals("second", d.mods().get(1).modId());
        assertEquals("third", d.mods().get(2).modId());
    }

    @Test
    void primaryReturnsFirstEntry() {
        NeoForgeModDescriptor d = parse("""
                [[mods]]
                modId = "first"
                [[mods]]
                modId = "second"
                """);
        assertEquals("first", d.primary().modId());
    }

    @Test
    void primaryNullWhenNoModsTable() {
        NeoForgeModDescriptor d = parse("modLoader = \"javafml\"\n");
        assertNull(d.primary());
        assertTrue(d.mods().isEmpty());
    }

    @Test
    void dependenciesFilterOutNeoforgeAndMinecraft() {
        NeoForgeModDescriptor d = parse("""
                [[mods]]
                modId = "demo"
                version = "1.0"

                [[dependencies.demo]]
                modId = "neoforge"
                mandatory = true
                versionRange = "[20.4,)"

                [[dependencies.demo]]
                modId = "minecraft"
                mandatory = true
                versionRange = "[1.20.4,)"

                [[dependencies.demo]]
                modId = "curios"
                mandatory = true
                versionRange = "[7.0,)"
                """);
        assertEquals(1, d.dependencies().size(), "neoforge + minecraft must be filtered");
        assertEquals("[7.0,)", d.dependencies().get("curios"));
    }

    @Test
    void optionalDependenciesSkipped() {
        NeoForgeModDescriptor d = parse("""
                [[mods]]
                modId = "demo"

                [[dependencies.demo]]
                modId = "opt-mod"
                mandatory = false
                versionRange = "*"
                """);
        assertFalse(d.dependencies().containsKey("opt-mod"));
    }

    @Test
    void authorsStringPassesThrough() {
        NeoForgeModDescriptor d = parse("""
                [[mods]]
                modId = "x"
                authors = "Alice, Bob"
                """);
        assertEquals("Alice, Bob", d.mods().get(0).authors());
    }

    @Test
    void descriptionCapturedWhenPresent() {
        NeoForgeModDescriptor d = parse("""
                [[mods]]
                modId = "x"
                description = "a neoforge mod"
                """);
        assertEquals("a neoforge mod", d.mods().get(0).description());
    }
}
