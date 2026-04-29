package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.world.Location;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBridge;
import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the legacy {@code Damageable.damage(int)} default delegate.
 * Essentials's {@code Commandsuicide} calls {@code player.damage(1000)}
 * via the int overload after constructing {@code EntityDamageEvent};
 * without the default the call resolves to nothing on the proxy and
 * throws {@link NoSuchMethodError}.
 *
 * <p>The default delegates to {@code damage(double)} which the
 * BukkitPlayer ByteBuddy proxy intercepts as a no-op — the player is
 * not actually damaged (RDForward has no health system on this
 * early-version server).
 */
class DamageableLegacyIntDamageTest {

    @AfterEach
    void wipe() {
        BukkitPlayer.evict("ZathrusW");
        BukkitBridge.uninstall();
    }

    @Test
    void damageIntIsDeclaredOnDamageable() {
        Method m = Arrays.stream(Damageable.class.getDeclaredMethods())
                .filter(x -> x.getName().equals("damage")
                        && x.getParameterCount() == 1
                        && x.getParameterTypes()[0] == int.class)
                .findFirst().orElse(null);
        assertNotNull(m, "Damageable.damage(int) must exist for /suicide");
        assertTrue(m.isDefault(), "damage(int) must be a default method delegating to damage(double)");
    }

    @Test
    void playerDamageIntDoesNotThrow() {
        StubRdServer rd = new StubRdServer();
        rd.players.put("ZathrusW", new StubRdServer.StubRdPlayer(
                "ZathrusW", new Location("stub-world", 0, 64, 0, 0f, 0f)));
        BukkitBridge.install(rd);

        Player p = BukkitPlayer.create("ZathrusW");
        // Essentials shape — int damage. Must not throw.
        ((Damageable) p).damage(1000);
    }
}
