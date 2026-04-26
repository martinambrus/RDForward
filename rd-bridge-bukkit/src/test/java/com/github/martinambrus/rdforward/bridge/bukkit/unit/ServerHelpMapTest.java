package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.help.HelpMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for WorldEdit's {@code WEPIF} bootstrap, which
 * calls {@code Bukkit.getServer().getHelpMap()}. The {@link HelpMap}
 * default is a process-wide stub singleton — it accepts every
 * registration silently because RDForward does not surface Bukkit help
 * topics, but it must not be {@code null}.
 */
class ServerHelpMapTest {

    @BeforeEach @AfterEach
    void clean() { BukkitBridge.uninstall(); }

    @Test
    void helpMapIsNonNullAndStable() {
        BukkitBridge.install(new StubRdServer());
        HelpMap first = Bukkit.getServer().getHelpMap();
        assertNotNull(first, "Server.getHelpMap() must not return null");
        HelpMap second = Bukkit.getServer().getHelpMap();
        assertSame(first, second,
                "successive calls must return the same singleton — plugins cache help registrations");
    }

    @Test
    void helpMapAcceptsRegistrationsSilently() {
        BukkitBridge.install(new StubRdServer());
        HelpMap map = Bukkit.getServer().getHelpMap();
        // Real plugin code calls these and expects no exception. The stub
        // discards everything, but the calls themselves must succeed.
        map.clear();
        map.addTopic(null);
        map.registerHelpTopicFactory(Object.class, null);
        assertTrue(map.getHelpTopics().isEmpty());
        assertTrue(map.getIgnoredPlugins().isEmpty());
    }
}
