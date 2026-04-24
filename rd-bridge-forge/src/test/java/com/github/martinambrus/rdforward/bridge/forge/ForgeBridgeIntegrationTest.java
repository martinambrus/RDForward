package com.github.martinambrus.rdforward.bridge.forge;

import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.forge.fixtures.ForgeTestServer;
import com.github.martinambrus.rdforward.bridge.forge.fixtures.TestForgeEventHandler;
import com.github.martinambrus.rdforward.bridge.forge.fixtures.TestForgeMod;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for the Forge bridge. Mirrors the Bukkit / Paper bridge
 * suites: fixture jars are assembled at runtime, loaded through the real
 * loader, and exercised end-to-end against a stub rd-api {@code Server}.
 */
class ForgeBridgeIntegrationTest {

    private static final String[] PROPS = {
            TestForgeMod.PROP_CTOR,
            TestForgeMod.PROP_COMMON_SETUP,
            TestForgeMod.PROP_BREAK,
            TestForgeMod.PROP_TICK,
            TestForgeEventHandler.PROP_STATIC_CHAT
    };

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        ForgeBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        ForgeBridge.uninstall();
        for (String p : PROPS) System.clearProperty(p);
    }

    @Test
    void loadsModWithAtModAnnotation(@TempDir Path dir) throws Exception {
        Path jar = writeForgeJar(dir.resolve("forge.jar"), "testmod",
                TestForgeMod.class.getName(), TestForgeEventHandler.class.getName());

        ForgeModLoader.LoadedForgeMod loaded = ForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            assertEquals("testmod", loaded.descriptor().id());
            assertTrue(loaded.mods().containsKey("testmod"));
            assertNotNull(loaded.primary().instance(), "mod ctor must have produced an instance");
            assertEquals("true", System.getProperty(TestForgeMod.PROP_CTOR),
                    "mod constructor must have run");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void fmlCommonSetupEventFiresOnEnable(@TempDir Path dir) throws Exception {
        Path jar = writeForgeJar(dir.resolve("setup.jar"), "testmod",
                TestForgeMod.class.getName(), TestForgeEventHandler.class.getName());

        ForgeModLoader.LoadedForgeMod loaded = ForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(new ForgeTestServer());
            assertEquals("true", System.getProperty(TestForgeMod.PROP_COMMON_SETUP),
                    "FMLCommonSetupEvent must fire on enable");
        } finally {
            loaded.classLoader().close();
        }
    }

    @Test
    void blockBreakEventRoundTripsBothBuses(@TempDir Path dir) throws Exception {
        Path jar = writeForgeJar(dir.resolve("break.jar"), "testmod",
                TestForgeMod.class.getName(), TestForgeEventHandler.class.getName());

        ForgeTestServer rd = new ForgeTestServer();
        ForgeBridge.install(rd);
        assertTrue(ForgeBridge.isInstalled());

        ForgeModLoader.LoadedForgeMod loaded = ForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);

            EventResult allow = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("alice", 10, 64, -5, 3);
            assertEquals(EventResult.PASS, allow, "uncancelled break must pass rd-api");
            assertEquals("alice@10,64,-5", System.getProperty(TestForgeMod.PROP_BREAK),
                    "listener must have received the break with correct coords");

            EventResult deny = ServerEvents.BLOCK_BREAK.invoker().onBlockBreak("veto", 0, 0, 0, 1);
            assertEquals(EventResult.CANCEL, deny,
                    "setCanceled(true) on Forge event must bubble back to rd-api as CANCEL");
        } finally {
            loaded.classLoader().close();
            ForgeBridge.uninstall();
        }
    }

    @Test
    void subscribeEventClassPathRegistersStatic(@TempDir Path dir) throws Exception {
        Path jar = writeForgeJar(dir.resolve("static.jar"), "testmod",
                TestForgeMod.class.getName(), TestForgeEventHandler.class.getName());

        ForgeTestServer rd = new ForgeTestServer();
        ForgeBridge.install(rd);

        ForgeModLoader.LoadedForgeMod loaded = ForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);

            ServerEvents.CHAT.invoker().onChat("bob", "hi world");
            assertEquals("bob:hi world", System.getProperty(TestForgeEventHandler.PROP_STATIC_CHAT),
                    "@Mod.EventBusSubscriber static handler must receive chat");
        } finally {
            loaded.classLoader().close();
            ForgeBridge.uninstall();
        }
    }

    @Test
    void tickEventMapsToServerEventsTick(@TempDir Path dir) throws Exception {
        Path jar = writeForgeJar(dir.resolve("tick.jar"), "testmod",
                TestForgeMod.class.getName(), TestForgeEventHandler.class.getName());

        ForgeTestServer rd = new ForgeTestServer();
        ForgeBridge.install(rd);

        ForgeModLoader.LoadedForgeMod loaded = ForgeModLoader.load(jar, getClass().getClassLoader());
        try {
            loaded.serverMod().onEnable(rd);

            ServerEvents.SERVER_TICK.invoker().onServerTick(1);
            ServerEvents.SERVER_TICK.invoker().onServerTick(2);
            ServerEvents.SERVER_TICK.invoker().onServerTick(3);

            assertEquals("3", System.getProperty(TestForgeMod.PROP_TICK),
                    "ServerTickEvent must fire once per rd-api SERVER_TICK");
        } finally {
            loaded.classLoader().close();
            ForgeBridge.uninstall();
        }
    }

    private Path writeForgeJar(Path target, String modId, String... extraClasses) throws IOException {
        String toml = ""
                + "modLoader=\"javafml\"\n"
                + "loaderVersion=\"[47,)\"\n"
                + "license=\"MIT\"\n"
                + "\n"
                + "[[mods]]\n"
                + "modId=\"" + modId + "\"\n"
                + "version=\"1.0.0\"\n"
                + "displayName=\"Test Forge Mod\"\n"
                + "description=\"fixture\"\n";
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(target))) {
            jar.putNextEntry(new JarEntry("META-INF/mods.toml"));
            jar.write(toml.getBytes());
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
