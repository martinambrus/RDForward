package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.event.Event$Priority;
import org.bukkit.event.Event$Type;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.SimplePluginManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pinning regression test for LogBlock 1.41 + LogBlockQuestioner 0.02,
 * which compile against the pre-Bukkit-1.x event API:
 * {@code Event.Type} / {@code Event.Priority} inner enums (encoded as
 * {@code Event$Type}/{@code Event$Priority} in the constant pool) and
 * the abstract {@link PlayerListener} base class. Modern Bukkit
 * replaced these with {@code @EventHandler}-annotated methods on plain
 * {@link Listener} implementors.
 *
 * <p>RDForward's bridge keeps the legacy types as load-only stubs:
 * {@link org.bukkit.plugin.PluginManager#registerEvent(Event$Type,
 * Listener, Event$Priority, org.bukkit.plugin.Plugin)} is a logged
 * no-op (events aren't routed) but the call site links cleanly so the
 * plugin's {@code onEnable} doesn't {@code NoSuchMethodError}.
 */
class LegacyEventApiTest {

    @Test
    void eventTypeEnumExposesPlayerCommandPreprocess() {
        Event$Type t = Event$Type.PLAYER_COMMAND_PREPROCESS;
        assertNotNull(t);
        assertEquals("PLAYER_COMMAND_PREPROCESS", t.name());
    }

    @Test
    void eventPriorityEnumKeepsMixedCaseSpellings() {
        // pre-Bukkit-1.x bytecode embeds the original mixed-case
        // spellings in its constant pool — uppercase aliases would
        // NoSuchFieldError when the plugin GETSTATICs Event$Priority.Normal.
        for (String name : new String[]{"Lowest", "Low", "Normal", "High", "Highest", "Monitor"}) {
            assertNotNull(Event$Priority.valueOf(name),
                    "Event$Priority must expose mixed-case constant: " + name);
        }
    }

    @Test
    void playerListenerIsConcreteAndImplementsListener() {
        // Plugins extend PlayerListener and override only the events
        // they care about. A new instance of the bare class must
        // construct cleanly so subclasses can call super() implicitly.
        PlayerListener pl = new PlayerListener();
        assertTrue(pl instanceof Listener,
                "PlayerListener must implement Listener so the bridge can register it");
    }

    @Test
    void playerListenerOnPlayerCommandPreprocessIsNoOp() {
        PlayerListener pl = new PlayerListener();
        assertDoesNotThrow(() -> pl.onPlayerCommandPreprocess(null),
                "default callback must be a no-op so unset overrides do nothing");
    }

    @Test
    void subclassOverridesSurfaceThroughVirtualDispatch() {
        // The pre-1.x dispatch contract: the abstract base declares
        // every onPlayerXxx as a no-op; a subclass overrides only the
        // ones it needs, and the dispatcher invokes by virtual call.
        // RDForward's bridge does NOT route the legacy enum form into
        // dispatch, but the subclass's override must still resolve at
        // runtime when invoked directly (used in ad-hoc shutdown code).
        final boolean[] invoked = { false };
        PlayerListener pl = new PlayerListener() {
            @Override
            public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
                invoked[0] = true;
            }
        };
        pl.onPlayerCommandPreprocess(null);
        assertTrue(invoked[0]);
    }

    @Test
    void legacyRegisterEventOverloadIsCallable() {
        // LogBlock 1.41 calls
        // pluginManager.registerEvent(Event.Type.BLOCK_BREAK, listener,
        //     Event.Priority.Normal, this);
        // RDForward's PluginManager exposes this as a default no-op so
        // the linkage resolves at runtime; events aren't routed but the
        // call site no longer NoSuchMethodErrors.
        SimplePluginManager pm = new SimplePluginManager();
        PlayerListener listener = new PlayerListener();
        assertDoesNotThrow(() -> pm.registerEvent(
                Event$Type.PLAYER_COMMAND_PREPROCESS,
                listener,
                Event$Priority.Normal,
                null));
    }
}
