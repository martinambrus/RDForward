package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.mod.ModDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DescriptorParserTest {

    @Test
    void parsesMinimalDescriptor() throws DescriptorParser.ModDescriptorException {
        String json = """
                {
                  "id": "alpha",
                  "version": "1.0"
                }
                """;
        ModDescriptor d = DescriptorParser.parseJson(json);
        assertEquals("alpha", d.id());
        assertEquals("1.0", d.version());
        assertEquals("alpha", d.name(), "name defaults to id when missing");
        assertTrue(d.description().isEmpty());
        assertTrue(d.authors().isEmpty());
        assertTrue(d.dependencies().isEmpty());
        assertTrue(d.entrypoints().isEmpty());
        assertFalse(d.reloadable());
    }

    @Test
    void parsesFullDescriptor() throws DescriptorParser.ModDescriptorException {
        String json = """
                {
                  "id": "gamma",
                  "name": "Gamma Mod",
                  "version": "2.1.0",
                  "description": "A test",
                  "api_version": ">=0.2",
                  "authors": ["Alice", "Bob"],
                  "entrypoints": {"server": "com.ex.Server", "client": "com.ex.Client"},
                  "dependencies": {"core": ">=1.0", "utils": "*"},
                  "soft_dependencies": {"opt": "1.0"},
                  "permissions": ["rdforward.test.use"],
                  "reloadable": true,
                  "min_protocol": "BETA_1_7_3",
                  "max_protocol": "LATEST"
                }
                """;
        ModDescriptor d = DescriptorParser.parseJson(json);
        assertEquals("gamma", d.id());
        assertEquals("Gamma Mod", d.name());
        assertEquals("2.1.0", d.version());
        assertEquals(2, d.authors().size());
        assertEquals("com.ex.Server", d.serverEntrypoint());
        assertEquals("com.ex.Client", d.clientEntrypoint());
        assertEquals(">=1.0", d.dependencies().get("core"));
        assertEquals("1.0", d.softDependencies().get("opt"));
        assertEquals(1, d.permissions().size());
        assertTrue(d.reloadable());
        assertEquals("BETA_1_7_3", d.minProtocol());
        assertEquals("LATEST", d.maxProtocol());
    }

    @Test
    void missingRequiredIdThrows() {
        String json = """
                { "version": "1.0" }
                """;
        DescriptorParser.ModDescriptorException ex = assertThrows(
                DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseJson(json));
        assertTrue(ex.getMessage().contains("id"));
    }

    @Test
    void missingRequiredVersionThrows() {
        String json = """
                { "id": "x" }
                """;
        assertThrows(DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseJson(json));
    }

    @Test
    void malformedJsonThrows() {
        assertThrows(DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseJson("{ this is not json"));
    }

    @Test
    void rootArrayRejected() {
        assertThrows(DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseJson("[\"not\", \"an\", \"object\"]"));
    }

    @Test
    void nullFieldsTreatedAsAbsent() throws DescriptorParser.ModDescriptorException {
        String json = """
                {
                  "id": "x",
                  "version": "1.0",
                  "description": null,
                  "authors": null,
                  "dependencies": null
                }
                """;
        ModDescriptor d = DescriptorParser.parseJson(json);
        assertEquals("", d.description());
        assertTrue(d.authors().isEmpty());
        assertTrue(d.dependencies().isEmpty());
    }

    @Test
    void missingNameDefaultsToId() throws DescriptorParser.ModDescriptorException {
        String json = """
                { "id": "alpha", "version": "1.0" }
                """;
        ModDescriptor d = DescriptorParser.parseJson(json);
        assertEquals("alpha", d.name());
    }

    @Test
    void reloadableDefaultsToFalse() throws DescriptorParser.ModDescriptorException {
        ModDescriptor d = DescriptorParser.parseJson("""
                { "id": "x", "version": "1.0" }
                """);
        assertFalse(d.reloadable());
    }

    // -------- YAML --------

    @Test
    void parsesMinimalYaml() throws DescriptorParser.ModDescriptorException {
        String yaml = """
                id: alpha
                version: "1.0"
                """;
        ModDescriptor d = DescriptorParser.parseYaml(yaml);
        assertEquals("alpha", d.id());
        assertEquals("1.0", d.version());
        assertEquals("alpha", d.name());
        assertFalse(d.reloadable());
    }

    @Test
    void parsesFullYaml() throws DescriptorParser.ModDescriptorException {
        String yaml = """
                id: gamma
                name: Gamma Mod
                version: "2.1.0"
                description: A test
                api_version: ">=0.2"
                authors:
                  - Alice
                  - Bob
                entrypoints:
                  server: com.ex.Server
                  client: com.ex.Client
                dependencies:
                  core: ">=1.0"
                  utils: "*"
                soft_dependencies:
                  opt: "1.0"
                permissions:
                  - rdforward.test.use
                reloadable: true
                min_protocol: BETA_1_7_3
                max_protocol: LATEST
                """;
        ModDescriptor d = DescriptorParser.parseYaml(yaml);
        assertEquals("gamma", d.id());
        assertEquals("Gamma Mod", d.name());
        assertEquals("2.1.0", d.version());
        assertEquals(2, d.authors().size());
        assertEquals("com.ex.Server", d.serverEntrypoint());
        assertEquals("com.ex.Client", d.clientEntrypoint());
        assertEquals(">=1.0", d.dependencies().get("core"));
        assertEquals("1.0", d.softDependencies().get("opt"));
        assertTrue(d.reloadable());
        assertEquals("BETA_1_7_3", d.minProtocol());
    }

    @Test
    void yamlMissingRequiredThrows() {
        DescriptorParser.ModDescriptorException ex = assertThrows(
                DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseYaml("version: \"1.0\"\n"));
        assertTrue(ex.getMessage().contains("id"));
    }

    @Test
    void malformedYamlThrows() {
        assertThrows(DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseYaml("id: [unterminated\n"));
    }

    @Test
    void yamlRootScalarRejected() {
        assertThrows(DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseYaml("just-a-string"));
    }

    // -------- TOML --------

    @Test
    void parsesMinimalToml() throws DescriptorParser.ModDescriptorException {
        String toml = """
                id = "alpha"
                version = "1.0"
                """;
        ModDescriptor d = DescriptorParser.parseToml(toml);
        assertEquals("alpha", d.id());
        assertEquals("1.0", d.version());
        assertEquals("alpha", d.name());
        assertFalse(d.reloadable());
    }

    @Test
    void parsesFullToml() throws DescriptorParser.ModDescriptorException {
        String toml = """
                id = "gamma"
                name = "Gamma Mod"
                version = "2.1.0"
                description = "A test"
                api_version = ">=0.2"
                authors = ["Alice", "Bob"]
                permissions = ["rdforward.test.use"]
                reloadable = true
                min_protocol = "BETA_1_7_3"
                max_protocol = "LATEST"
                [entrypoints]
                server = "com.ex.Server"
                client = "com.ex.Client"
                [dependencies]
                core = ">=1.0"
                utils = "*"
                [soft_dependencies]
                opt = "1.0"
                """;
        ModDescriptor d = DescriptorParser.parseToml(toml);
        assertEquals("gamma", d.id());
        assertEquals("Gamma Mod", d.name());
        assertEquals("2.1.0", d.version());
        assertEquals(2, d.authors().size());
        assertEquals("com.ex.Server", d.serverEntrypoint());
        assertEquals("com.ex.Client", d.clientEntrypoint());
        assertEquals(">=1.0", d.dependencies().get("core"));
        assertEquals("1.0", d.softDependencies().get("opt"));
        assertTrue(d.reloadable());
        assertEquals("BETA_1_7_3", d.minProtocol());
    }

    @Test
    void tomlMissingRequiredThrows() {
        DescriptorParser.ModDescriptorException ex = assertThrows(
                DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseToml("version = \"1.0\"\n"));
        assertTrue(ex.getMessage().contains("id"));
    }

    @Test
    void malformedTomlThrows() {
        assertThrows(DescriptorParser.ModDescriptorException.class,
                () -> DescriptorParser.parseToml("id = \"x\n"));
    }
}
