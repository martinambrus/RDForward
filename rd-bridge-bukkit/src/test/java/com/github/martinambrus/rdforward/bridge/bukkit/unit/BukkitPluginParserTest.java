package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginDescriptor;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPluginParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BukkitPluginParserTest {

    private static BukkitPluginDescriptor parse(String yml) {
        return BukkitPluginParser.parse(new ByteArrayInputStream(yml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesMandatoryFields() {
        String yml = """
                name: Demo
                version: 1.2.3
                main: com.example.Demo
                """;
        BukkitPluginDescriptor d = parse(yml);
        assertEquals("Demo", d.name());
        assertEquals("1.2.3", d.version());
        assertEquals("com.example.Demo", d.main());
        assertTrue(d.depend().isEmpty(), "depend defaults to empty list");
        assertTrue(d.commands().isEmpty(), "commands defaults to empty map");
    }

    @Test
    void rejectsMissingName() {
        String yml = "version: '1.0'\nmain: com.example.Demo\n";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parse(yml));
        assertTrue(ex.getMessage().contains("name"));
    }

    @Test
    void rejectsMissingVersion() {
        String yml = "name: Demo\nmain: com.example.Demo\n";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parse(yml));
        assertTrue(ex.getMessage().contains("version"));
    }

    @Test
    void rejectsMissingMain() {
        String yml = "name: Demo\nversion: '1.0'\n";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parse(yml));
        assertTrue(ex.getMessage().contains("main"));
    }

    @Test
    void rejectsBlankName() {
        String yml = "name: ''\nversion: '1.0'\nmain: com.example.Demo\n";
        assertThrows(IllegalArgumentException.class, () -> parse(yml));
    }

    @Test
    void rejectsEmptyYaml() {
        assertThrows(IllegalArgumentException.class, () -> parse(""));
    }

    @Test
    void ignoresUnknownTopLevelFields() {
        String yml = """
                name: Demo
                version: '1.0'
                main: com.example.Demo
                softdepend: [SomeOtherPlugin]
                loadbefore: [Y]
                website: https://example.com/
                """;
        BukkitPluginDescriptor d = parse(yml);
        assertEquals("Demo", d.name());
    }

    @Test
    void parsesDependList() {
        String yml = """
                name: Demo
                version: '1.0'
                main: com.example.Demo
                depend: [ProtocolLib, Vault]
                """;
        BukkitPluginDescriptor d = parse(yml);
        assertEquals(2, d.depend().size());
        assertTrue(d.depend().contains("ProtocolLib"));
        assertTrue(d.depend().contains("Vault"));
    }

    @Test
    void parsesCommandsBlock() {
        String yml = """
                name: Demo
                version: '1.0'
                main: com.example.Demo
                commands:
                  hello:
                    description: Say hi
                    usage: /hello
                    permission: demo.hello
                    aliases: [hi, hey]
                """;
        BukkitPluginDescriptor d = parse(yml);
        assertEquals(1, d.commands().size());
        BukkitPluginDescriptor.CommandSpec hello = d.commands().get("hello");
        assertNotNull(hello);
        assertEquals("hello", hello.name());
        assertEquals("Say hi", hello.description());
        assertEquals("/hello", hello.usage());
        assertEquals("demo.hello", hello.permission());
        assertEquals(2, hello.aliases().size());
        assertEquals("hi", hello.aliases().get(0));
    }

    @Test
    void commandAliasesAcceptSingleString() {
        String yml = """
                name: Demo
                version: '1.0'
                main: com.example.Demo
                commands:
                  tp:
                    aliases: teleport
                """;
        BukkitPluginDescriptor d = parse(yml);
        BukkitPluginDescriptor.CommandSpec tp = d.commands().get("tp");
        assertEquals(1, tp.aliases().size());
        assertEquals("teleport", tp.aliases().get(0));
    }

    @Test
    void commandEntryWithoutBodyHasDefaults() {
        String yml = """
                name: Demo
                version: '1.0'
                main: com.example.Demo
                commands:
                  ping:
                """;
        BukkitPluginDescriptor d = parse(yml);
        BukkitPluginDescriptor.CommandSpec ping = d.commands().get("ping");
        assertEquals("", ping.description());
        assertEquals("", ping.usage());
        assertNull(ping.permission());
        assertTrue(ping.aliases().isEmpty());
    }
}
