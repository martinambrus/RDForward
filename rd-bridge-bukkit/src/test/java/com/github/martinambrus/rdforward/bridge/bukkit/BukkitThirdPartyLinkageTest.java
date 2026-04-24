package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke test that loads a real third-party plugin jar and runs its
 * {@code onEnable}. Used as a "canary" for generated stub coverage:
 * any {@link NoClassDefFoundError} / {@link NoSuchMethodError} surfaced
 * during enable tells us a specific upstream class or member is
 * missing from the generated bridge.
 *
 * <p>Tagged {@code thirdparty} so it is excluded from the default
 * {@code :rd-bridge-bukkit:test} run — the dedicated
 * {@code :rd-bridge-bukkit:testThirdParty} Gradle task opts into it.
 * The plugin jar lives under
 * {@code src/test/resources/third-party-fixtures/}; add more plugins
 * by dropping their jars there and adding a matching test method.
 */
@Tag("thirdparty")
class BukkitThirdPartyLinkageTest {

    private static final String LUCKPERMS_FIXTURE =
            "third-party-fixtures/LuckPerms-Bukkit-5.5.42.jar";

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitEventAdapter.resetWarnedPlugins();
        BukkitBridge.uninstall();
    }

    @Test
    void luckPermsBukkitLoaderEnablesWithoutLinkageError(@TempDir Path dir) throws Exception {
        Path jar = dir.resolve("LuckPerms-Bukkit.jar");
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(LUCKPERMS_FIXTURE)) {
            assertNotNull(in,
                    "fixture jar not on test classpath: " + LUCKPERMS_FIXTURE);
            Files.copy(in, jar, StandardCopyOption.REPLACE_EXISTING);
        }

        StubRdServer rd = new StubRdServer();
        BukkitBridge.install(rd);

        BukkitPluginLoader.LoadedPlugin loaded =
                BukkitPluginLoader.load(jar, getClass().getClassLoader());
        try {
            try {
                loaded.serverMod().onEnable(rd);
            } catch (Throwable t) {
                throw new AssertionError(
                        "LuckPerms-Bukkit onEnable surfaced a linkage/runtime failure: "
                                + summarize(t), t);
            }

            try {
                loaded.serverMod().onDisable();
            } catch (Throwable ignored) {
                // Best-effort; disable errors are not the smoke-test signal.
            }
        } finally {
            loaded.classLoader().close();
        }
    }

    private static String summarize(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root) root = root.getCause();
        return t.getClass().getName()
                + (t.getMessage() == null ? "" : ": " + t.getMessage())
                + " (root=" + root.getClass().getName()
                + (root.getMessage() == null ? "" : ": " + root.getMessage()) + ")";
    }
}
