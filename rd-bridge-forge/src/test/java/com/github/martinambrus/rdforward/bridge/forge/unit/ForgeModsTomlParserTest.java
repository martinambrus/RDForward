package com.github.martinambrus.rdforward.bridge.forge.unit;

import com.github.martinambrus.rdforward.bridge.forge.ForgeModDescriptor;
import com.github.martinambrus.rdforward.bridge.forge.ForgeModsTomlParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgeModsTomlParserTest {

    private static ForgeModDescriptor parse(String toml) {
        return ForgeModsTomlParser.parse(new ByteArrayInputStream(toml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesMinimalModsToml() {
        ForgeModDescriptor d = parse("""
                modLoader = "javafml"
                loaderVersion = "[47,)"
                license = "MIT"

                [[mods]]
                modId = "testmod"
                version = "1.0.0"
                """);
        assertEquals("javafml", d.modLoader());
        assertEquals("[47,)", d.loaderVersion());
        assertEquals("MIT", d.license());
        assertEquals(1, d.mods().size());
        ForgeModDescriptor.Entry e = d.mods().get(0);
        assertEquals("testmod", e.modId());
        assertEquals("1.0.0", e.version());
    }

    @Test
    void modLoaderDefaultsToJavafml() {
        ForgeModDescriptor d = parse("[[mods]]\nmodId = \"x\"\n");
        assertEquals("javafml", d.modLoader());
    }

    @Test
    void licenseAndLoaderVersionDefaultBlank() {
        ForgeModDescriptor d = parse("[[mods]]\nmodId = \"x\"\n");
        assertEquals("", d.loaderVersion());
        assertEquals("", d.license());
    }

    @Test
    void displayNameDefaultsToModId() {
        ForgeModDescriptor d = parse("[[mods]]\nmodId = \"abc\"\n");
        assertEquals("abc", d.mods().get(0).displayName());
    }

    @Test
    void parsesMultipleModEntries() {
        ForgeModDescriptor d = parse("""
                [[mods]]
                modId = "alpha"
                version = "1.0"
                [[mods]]
                modId = "beta"
                version = "2.0"
                """);
        assertEquals(2, d.mods().size());
        assertEquals("alpha", d.mods().get(0).modId());
        assertEquals("beta", d.mods().get(1).modId());
    }

    @Test
    void primaryReturnsFirstEntry() {
        ForgeModDescriptor d = parse("[[mods]]\nmodId = \"first\"\n[[mods]]\nmodId = \"second\"\n");
        assertEquals("first", d.primary().modId());
    }

    @Test
    void primaryReturnsNullWhenNoModsTable() {
        ForgeModDescriptor d = parse("modLoader = \"javafml\"\nlicense = \"ARR\"\n");
        assertNull(d.primary());
        assertTrue(d.mods().isEmpty());
    }

    @Test
    void parsesDependenciesTableAndSkipsForgeMinecraft() {
        ForgeModDescriptor d = parse("""
                [[mods]]
                modId = "testmod"
                version = "1.0"

                [[dependencies.testmod]]
                modId = "forge"
                mandatory = true
                versionRange = "[47,)"

                [[dependencies.testmod]]
                modId = "minecraft"
                mandatory = true
                versionRange = "[1.20,)"

                [[dependencies.testmod]]
                modId = "jei"
                mandatory = true
                versionRange = "[10.0,)"
                """);
        assertEquals(1, d.dependencies().size(), "forge and minecraft must be filtered out");
        assertEquals("[10.0,)", d.dependencies().get("jei"));
    }

    @Test
    void skipsOptionalDependencies() {
        ForgeModDescriptor d = parse("""
                [[mods]]
                modId = "testmod"

                [[dependencies.testmod]]
                modId = "optional-dep"
                mandatory = false
                versionRange = "*"
                """);
        assertFalse(d.dependencies().containsKey("optional-dep"));
    }

    @Test
    void authorsPassThroughWhenDeclared() {
        ForgeModDescriptor d = parse("""
                [[mods]]
                modId = "x"
                authors = "Alice, Bob"
                """);
        assertEquals("Alice, Bob", d.mods().get(0).authors());
    }

    @Test
    void authorsNullWhenAbsent() {
        ForgeModDescriptor d = parse("[[mods]]\nmodId = \"x\"\n");
        assertNull(d.mods().get(0).authors());
    }
}
