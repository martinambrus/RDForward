package com.github.martinambrus.rdforward.bridge.neoforge;

import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.neoforge.fixtures.NeoForgeTestServer;
import com.github.martinambrus.rdforward.bridge.neoforge.fixtures.TestNeoForgeMod;
import com.github.martinambrus.rdforward.bridge.neoforge.fixtures.TestNeoForgeNoAnnotation;
import com.github.martinambrus.rdforward.bridge.forge.ForgeModLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the NeoForge bridge. Exercises deltas from the
 * Forge bridge: separate {@code neoforge.mods.toml} manifest, constructor
 * injection of {@code IEventBus} + {@code ModContainer}, and the
 * {@code mainClass} fallback for mods lacking an {@code @Mod} annotation.
 */
class NeoForgeBridgeIntegrationTest {

    private static final String[] PROPS = {
            TestNeoForgeMod.PROP_CTOR,
            TestNeoForgeMod.PROP_BUS_CLASS,
            TestNeoForgeMod.PROP_MOD_ID,
            TestNeoForgeMod.PROP_COMMON_SETUP,
            TestNeoForgeMod.PROP_BREAK,
            TestNeoForgeNoAnnotation.PROP_CTOR
    };

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        NeoForgeBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        NeoForgeBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @Test
    void neoforgeModsTomlParsesSeparateFile(@TempDir Path dir) throws Exception {
        Path jar = writeNeoForgeJar(dir.resolve("neoforge.jar"), "testmod",
                TestNeoForgeMod.class.getName(), null,
                TestNeoForgeMod.class.getName());

        IOException thrown = assertThrows(IOException.class,
                () -> ForgeModLoader.load(jar, getClass().getClassLoader()),
                "Forge loader must reject jars without META-INF/mods.toml");
        assertTrue(thrown.getMessage().contains("mods.toml"),
                "error must identify the missing manifest");

        NeoForgeModLoader.LoadedNeoForgeMod loaded =
                NeoForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            assertEquals("testmod", loaded.descriptor().id());
            assertTrue(loaded.mods().containsKey("testmod"));
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void constructorInjectsIEventBusAndModContainer(@TempDir Path dir) throws Exception {
        Path jar = writeNeoForgeJar(dir.resolve("ctor.jar"), "testmod",
                TestNeoForgeMod.class.getName(), null,
                TestNeoForgeMod.class.getName());

        NeoForgeModLoader.LoadedNeoForgeMod loaded =
                NeoForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            assertEquals("true", System.getProperty(TestNeoForgeMod.PROP_CTOR),
                    "mod constructor must have run");
            assertNotNull(loaded.primary().instance(), "ctor must have produced an instance");
            assertEquals("testmod", System.getProperty(TestNeoForgeMod.PROP_MOD_ID),
                    "injected ModContainer must carry the mod id");
            String busClass = System.getProperty(TestNeoForgeMod.PROP_BUS_CLASS);
            assertNotNull(busClass, "injected IEventBus reference must have been non-null");
            assertTrue(busClass.contains("NeoForgeEventBus"),
                    "injected bus must be a NeoForge-typed IEventBus impl, was " + busClass);
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void neoforgeEventBusForwardsToRdApi(@TempDir Path dir) throws Exception {
        Path jar = writeNeoForgeJar(dir.resolve("forward.jar"), "testmod",
                TestNeoForgeMod.class.getName(), null,
                TestNeoForgeMod.class.getName());

        NeoForgeTestServer rd = new NeoForgeTestServer();
        NeoForgeBridge.install(rd);
        assertTrue(NeoForgeBridge.isInstalled());

        NeoForgeModLoader.LoadedNeoForgeMod loaded =
                NeoForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);

            assertEquals("true", System.getProperty(TestNeoForgeMod.PROP_COMMON_SETUP),
                    "FMLCommonSetupEvent must fire on enable");

            EventResult result = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 4, 63, 8, 1);
            assertEquals(EventResult.PASS, result, "uncancelled break must pass rd-api");
            assertEquals("alice@4,63,8", System.getProperty(TestNeoForgeMod.PROP_BREAK),
                    "NeoForge mod listener on shared event bus must see the break");
        } finally {
            loaded.classLoader().close();
            NeoForgeBridge.uninstall();
        }
    }

    @Test
    void modWithoutAtModAnnotationLoadsFromMainClassField(@TempDir Path dir) throws Exception {
        String fqcn = TestNeoForgeNoAnnotation.class.getName();
        Path jar = writeNeoForgeJar(dir.resolve("noann.jar"), "noannmod", fqcn, fqcn,
                TestNeoForgeNoAnnotation.class.getName());

        NeoForgeModLoader.LoadedNeoForgeMod loaded =
                NeoForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            assertEquals("true", System.getProperty(TestNeoForgeNoAnnotation.PROP_CTOR),
                    "loader must instantiate the mainClass when no @Mod annotation is present");
            assertTrue(loaded.mods().containsKey("noannmod"));
        } finally {
            loaded.classLoader().close();
        }
    }

    private Path writeNeoForgeJar(Path target, String modId, String displayName,
                                  String mainClass, String... extraClasses) throws IOException {
        StringBuilder toml = new StringBuilder()
                .append("modLoader=\"javafml\"\n")
                .append("loaderVersion=\"[1,)\"\n")
                .append("license=\"MIT\"\n")
                .append("\n")
                .append("[[mods]]\n")
                .append("modId=\"").append(modId).append("\"\n")
                .append("version=\"1.0.0\"\n")
                .append("displayName=\"").append(displayName).append("\"\n")
                .append("description=\"fixture\"\n");
        if (mainClass != null) {
            toml.append("mainClass=\"").append(mainClass).append("\"\n");
        }
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("META-INF/neoforge.mods.toml"));
            jar.write(toml.toString().getBytes());
            jar.closeEntry();
            for (String fqcn : extraClasses) copyClassBytes(jar, fqcn);
        }
        return target;
    }

    private void copyClassBytes(JarOutputStream jar, String fqcn) throws IOException {
        String entryName = fqcn.replace('.', '/') + ".class";
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(entryName)) {
            if (in == null) fail("class resource not found: " + entryName);
            jar.putNextEntry(new JarEntry(entryName));
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) != -1) jar.write(buf, 0, n);
            jar.closeEntry();
        }
    }
}
