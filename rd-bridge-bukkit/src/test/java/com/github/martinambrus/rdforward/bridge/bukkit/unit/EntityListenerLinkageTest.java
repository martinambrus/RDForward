package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.event.Event$Type;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.plugin.SimplePluginManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for OpenWarp v1.1's {@code OWEntityListener},
 * which extends the pre-Bukkit-1.x abstract {@link EntityListener}
 * base class and overrides {@code onEntityDeath}. RDForward keeps
 * {@code EntityListener} as a load-only stub: the legacy
 * {@code registerEvent(Event.Type, Listener, Event.Priority, Plugin)}
 * overload remains a logged no-op (events aren't routed to listeners
 * subscribed via the legacy enum form), but the linkage must resolve
 * cleanly so {@code OpenWarp.onEnable} doesn't
 * {@code NoClassDefFoundError} when its listener subclass is loaded.
 */
class EntityListenerLinkageTest {

    @Test
    void entityListenerIsConcreteAndImplementsListener() {
        EntityListener el = new EntityListener();
        assertTrue(el instanceof Listener,
                "EntityListener must implement Listener so the bridge can register it");
    }

    @Test
    void onEntityDeathDefaultsToNoOp() {
        EntityListener el = new EntityListener();
        assertDoesNotThrow(() -> el.onEntityDeath(null),
                "default callback must be a no-op so unset overrides do nothing");
    }

    @Test
    void subclassOverridesSurfaceThroughVirtualDispatch() {
        // Plugin authors override only the methods they care about.
        // Real dispatch is gated by RDForward's bridge; the legacy
        // registerEvent(Event.Type, ...) path is a no-op so the
        // override never fires through the bridge -- but a direct
        // virtual call still resolves correctly.
        final boolean[] fired = { false };
        EntityListener el = new EntityListener() {
            @Override
            public void onEntityDeath(EntityDeathEvent event) { fired[0] = true; }
        };
        el.onEntityDeath(null);
        assertTrue(fired[0]);
    }

    @Test
    void legacyRegisterEventAcceptsEntityType() {
        // OpenWarp v1.1 calls
        //   pm.registerEvent(Event.Type.ENTITY_DEATH, listener, Event.Priority.Normal, plugin);
        // RDForward's PluginManager exposes this as a default no-op so
        // the call site links cleanly. The Event$Type.ENTITY_DEATH
        // constant must exist in the expanded legacy enum.
        assertNotNull(Event$Type.ENTITY_DEATH);
        SimplePluginManager pm = new SimplePluginManager();
        EntityListener listener = new EntityListener();
        assertDoesNotThrow(() -> pm.registerEvent(
                Event$Type.ENTITY_DEATH,
                listener,
                org.bukkit.event.Event$Priority.Normal,
                null));
    }

    @Test
    void expandedEventTypeEnumExposesOpenWarpRequirements() {
        // OpenWarp registers via PLAYER_JOIN, PLAYER_TELEPORT,
        // PLAYER_RESPAWN, ENTITY_DEATH. All four must exist as enum
        // constants so the bytecode getstatic resolves.
        assertNotNull(Event$Type.PLAYER_JOIN);
        assertNotNull(Event$Type.PLAYER_TELEPORT);
        assertNotNull(Event$Type.PLAYER_RESPAWN);
        assertNotNull(Event$Type.ENTITY_DEATH);
    }
}
