package com.github.martinambrus.rdforward.bridge.paper.unit;

import com.github.martinambrus.rdforward.bridge.paper.PaperPluginDescriptor;
import com.github.martinambrus.rdforward.bridge.paper.PaperPluginParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaperPluginParserTest {

    private static PaperPluginDescriptor parse(String yml) {
        return PaperPluginParser.parse(new ByteArrayInputStream(yml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesMandatoryFields() {
        PaperPluginDescriptor d = parse("""
                name: Demo
                version: '1.0.0'
                main: com.example.Demo
                """);
        assertEquals("Demo", d.name());
        assertEquals("1.0.0", d.version());
        assertEquals("com.example.Demo", d.main());
        assertNull(d.bootstrapper(), "bootstrapper is optional");
        assertNull(d.loader(), "loader is optional");
        assertEquals("", d.description());
        assertEquals("", d.apiVersion());
        assertTrue(d.authors().isEmpty());
        assertTrue(d.bootstrapDeps().isEmpty());
        assertTrue(d.serverDeps().isEmpty());
        assertTrue(d.commands().isEmpty());
    }

    @Test
    void rejectsMissingName() {
        String yml = "version: '1'\nmain: com.example.Demo\n";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parse(yml));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void rejectsMissingVersion() {
        assertThrows(IllegalArgumentException.class, () -> parse("name: X\nmain: Y\n"));
    }

    @Test
    void rejectsMissingMain() {
        assertThrows(IllegalArgumentException.class, () -> parse("name: X\nversion: '1'\n"));
    }

    @Test
    void rejectsEmptyYaml() {
        assertThrows(IllegalArgumentException.class, () -> parse(""));
    }

    @Test
    void parsesBootstrapperAndLoader() {
        PaperPluginDescriptor d = parse("""
                name: Demo
                version: '1'
                main: com.example.Demo
                bootstrapper: com.example.Boot
                loader: com.example.Loader
                description: Example plugin
                api-version: '1.21'
                """);
        assertEquals("com.example.Boot", d.bootstrapper());
        assertEquals("com.example.Loader", d.loader());
        assertEquals("Example plugin", d.description());
        assertEquals("1.21", d.apiVersion());
    }

    @Test
    void parsesAuthorsList() {
        PaperPluginDescriptor d = parse("""
                name: D
                version: '1'
                main: X
                authors: [Alice, Bob]
                """);
        assertEquals(2, d.authors().size());
        assertEquals("Alice", d.authors().get(0));
    }

    @Test
    void fallsBackToSingleAuthorKeyWhenAuthorsAbsent() {
        PaperPluginDescriptor d = parse("""
                name: D
                version: '1'
                main: X
                author: Solo
                """);
        assertEquals(1, d.authors().size());
        assertEquals("Solo", d.authors().get(0));
    }

    @Test
    void parsesNestedDependenciesSection() {
        PaperPluginDescriptor d = parse("""
                name: D
                version: '1'
                main: X
                dependencies:
                  bootstrap:
                    ProtocolLib:
                      load: BEFORE
                      required: true
                  server:
                    Vault:
                      required: false
                """);
        assertEquals(1, d.bootstrapDeps().size());
        assertTrue(d.bootstrapDeps().contains("ProtocolLib"));
        assertEquals(1, d.serverDeps().size());
        assertTrue(d.serverDeps().contains("Vault"));
    }

    @Test
    void parsesFlatDependListWhenDependenciesAbsent() {
        PaperPluginDescriptor d = parse("""
                name: D
                version: '1'
                main: X
                depend: [A, B]
                """);
        assertTrue(d.bootstrapDeps().isEmpty());
        assertEquals(2, d.serverDeps().size());
        assertTrue(d.serverDeps().contains("A"));
    }

    @Test
    void parsesDependencySectionAsFlatList() {
        PaperPluginDescriptor d = parse("""
                name: D
                version: '1'
                main: X
                dependencies:
                  server: [A, B]
                """);
        assertEquals(2, d.serverDeps().size());
    }

    @Test
    void parsesCommandsBlock() {
        PaperPluginDescriptor d = parse("""
                name: D
                version: '1'
                main: X
                commands:
                  hi:
                    description: Say hello
                    usage: /hi
                    permission: d.hi
                    aliases: [hello]
                """);
        assertNotNull(d.commands().get("hi"));
        assertEquals("Say hello", d.commands().get("hi").description());
        assertEquals(1, d.commands().get("hi").aliases().size());
    }
}
