package com.github.martinambrus.rdforward.bridge.pocketmine.unit;

import com.github.martinambrus.rdforward.bridge.pocketmine.PocketMinePluginParser;
import org.junit.jupiter.api.Test;
import pocketmine.plugin.PluginDescription;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PocketMinePluginParserTest {

    private static PluginDescription parse(String yml) {
        return PocketMinePluginParser.parse(new ByteArrayInputStream(yml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesMandatoryFieldsOnly() {
        PluginDescription d = parse("""
                name: Demo
                version: '1.0'
                main: com.example.Demo
                """);
        assertEquals("Demo", d.name());
        assertEquals("1.0", d.version());
        assertEquals("com.example.Demo", d.main());
        assertEquals("", d.api());
        assertEquals("", d.description());
        assertTrue(d.depend().isEmpty());
        assertTrue(d.softDepend().isEmpty());
        assertTrue(d.loadBefore().isEmpty());
        assertTrue(d.authors().isEmpty());
        assertTrue(d.commands().isEmpty());
    }

    @Test
    void rejectsMissingName() {
        assertThrows(IllegalArgumentException.class, () -> parse("version: '1'\nmain: X\n"));
    }

    @Test
    void rejectsMissingVersion() {
        assertThrows(IllegalArgumentException.class, () -> parse("name: D\nmain: X\n"));
    }

    @Test
    void rejectsMissingMain() {
        assertThrows(IllegalArgumentException.class, () -> parse("name: D\nversion: '1'\n"));
    }

    @Test
    void rejectsEmptyYaml() {
        assertThrows(IllegalArgumentException.class, () -> parse(""));
    }

    @Test
    void rejectsBlankRequiredField() {
        assertThrows(IllegalArgumentException.class, () -> parse("""
                name: ''
                version: '1'
                main: X
                """));
    }

    @Test
    void parsesApiAndDescription() {
        PluginDescription d = parse("""
                name: D
                version: '1'
                main: X
                api: '5.0.0'
                description: a pocketmine plugin
                """);
        assertEquals("5.0.0", d.api());
        assertEquals("a pocketmine plugin", d.description());
    }

    @Test
    void authorsListParsedWhenList() {
        PluginDescription d = parse("""
                name: D
                version: '1'
                main: X
                authors: [Alice, Bob]
                """);
        assertEquals(2, d.authors().size());
        assertEquals("Alice", d.authors().get(0));
        assertEquals("Bob", d.authors().get(1));
    }

    @Test
    void fallsBackToSingleAuthorWhenAuthorsAbsent() {
        PluginDescription d = parse("""
                name: D
                version: '1'
                main: X
                author: Solo
                """);
        assertEquals(1, d.authors().size());
        assertEquals("Solo", d.authors().get(0));
    }

    @Test
    void parsesDependLists() {
        PluginDescription d = parse("""
                name: D
                version: '1'
                main: X
                depend: [A, B]
                softdepend: [C]
                loadbefore: [E, F]
                """);
        assertEquals(2, d.depend().size());
        assertTrue(d.depend().contains("A"));
        assertEquals(1, d.softDepend().size());
        assertTrue(d.softDepend().contains("C"));
        assertEquals(2, d.loadBefore().size());
    }

    @Test
    void parsesCommandsBlock() {
        PluginDescription d = parse("""
                name: D
                version: '1'
                main: X
                commands:
                  hi:
                    description: Say hello
                    usage: /hi
                    permission: d.hi
                """);
        assertNotNull(d.commands().get("hi"));
        assertEquals("Say hello", d.commands().get("hi").get("description"));
        assertEquals("/hi", d.commands().get("hi").get("usage"));
    }

    @Test
    void dependStringCoercedToSingletonList() {
        PluginDescription d = parse("""
                name: D
                version: '1'
                main: X
                depend: DepA
                """);
        assertEquals(1, d.depend().size());
        assertEquals("DepA", d.depend().get(0));
    }
}
