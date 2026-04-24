package com.github.martinambrus.rdforward.bridge.fabric.unit;

import com.github.martinambrus.rdforward.bridge.fabric.FabricModDescriptor;
import com.github.martinambrus.rdforward.bridge.fabric.FabricModJsonParser;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricModJsonParserTest {

    private static FabricModDescriptor parse(String json) {
        return FabricModJsonParser.parse(new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesMinimalValidJson() {
        FabricModDescriptor d = parse("""
                {"id":"demo","version":"1.0.0"}
                """);
        assertEquals("demo", d.id());
        assertEquals("1.0.0", d.version());
        assertEquals("demo", d.name(), "name defaults to id when omitted");
        assertEquals("", d.description());
        assertEquals("*", d.environment(), "environment defaults to '*'");
        assertTrue(d.authors().isEmpty());
        assertTrue(d.mainEntrypoints().isEmpty());
        assertTrue(d.serverEntrypoints().isEmpty());
        assertTrue(d.clientEntrypoints().isEmpty());
        assertTrue(d.dependencies().isEmpty());
    }

    @Test
    void rejectsMissingId() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> parse("{\"version\":\"1.0\"}"));
        assertTrue(ex.getMessage().contains("id"));
    }

    @Test
    void rejectsMissingVersion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> parse("{\"id\":\"demo\"}"));
        assertTrue(ex.getMessage().contains("version"));
    }

    @Test
    void rejectsNonObjectRoot() {
        assertThrows(IllegalArgumentException.class, () -> parse("[]"));
        assertThrows(IllegalArgumentException.class, () -> parse("\"demo\""));
    }

    @Test
    void rejectsBlankId() {
        assertThrows(IllegalArgumentException.class,
                () -> parse("{\"id\":\"\",\"version\":\"1.0\"}"));
    }

    @Test
    void parsesEnvironmentField() {
        assertEquals("client", parse("{\"id\":\"x\",\"version\":\"1\",\"environment\":\"client\"}").environment());
        assertEquals("server", parse("{\"id\":\"x\",\"version\":\"1\",\"environment\":\"server\"}").environment());
    }

    @Test
    void parsesEntrypointsStringForm() {
        FabricModDescriptor d = parse("""
                {"id":"x","version":"1","entrypoints":{
                    "main":["com.example.Main"],
                    "server":["com.example.Server"],
                    "client":["com.example.Client"]
                }}
                """);
        assertEquals(1, d.mainEntrypoints().size());
        assertEquals("com.example.Main", d.mainEntrypoints().get(0));
        assertEquals("com.example.Server", d.serverEntrypoints().get(0));
        assertEquals("com.example.Client", d.clientEntrypoints().get(0));
    }

    @Test
    void parsesEntrypointsObjectForm() {
        FabricModDescriptor d = parse("""
                {"id":"x","version":"1","entrypoints":{
                    "main":[{"value":"com.example.Main","adapter":"kotlin"}]
                }}
                """);
        assertEquals(1, d.mainEntrypoints().size());
        assertEquals("com.example.Main", d.mainEntrypoints().get(0),
                "object-form entrypoints must extract the 'value' field");
    }

    @Test
    void parsesAuthorsAsStringList() {
        FabricModDescriptor d = parse("""
                {"id":"x","version":"1","authors":["Alice","Bob"]}
                """);
        assertEquals(2, d.authors().size());
        assertEquals("Alice", d.authors().get(0));
    }

    @Test
    void parsesAuthorsAsObjectList() {
        FabricModDescriptor d = parse("""
                {"id":"x","version":"1","authors":[{"name":"Alice","contact":{}},{"name":"Bob"}]}
                """);
        assertEquals(2, d.authors().size());
        assertEquals("Alice", d.authors().get(0));
    }

    @Test
    void parsesDepends() {
        FabricModDescriptor d = parse("""
                {"id":"x","version":"1","depends":{"fabricloader":">=0.15","minecraft":"1.21.x"}}
                """);
        assertEquals(2, d.dependencies().size());
        assertEquals(">=0.15", d.dependencies().get("fabricloader"));
        assertEquals("1.21.x", d.dependencies().get("minecraft"));
    }

    @Test
    void nameFallsBackToIdWhenBlank() {
        FabricModDescriptor d = parse("{\"id\":\"hello\",\"version\":\"1\"}");
        assertEquals("hello", d.name());
    }

    @Test
    void descriptionOptional() {
        FabricModDescriptor d = parse("{\"id\":\"x\",\"version\":\"1\",\"description\":\"test mod\"}");
        assertEquals("test mod", d.description());
    }
}
