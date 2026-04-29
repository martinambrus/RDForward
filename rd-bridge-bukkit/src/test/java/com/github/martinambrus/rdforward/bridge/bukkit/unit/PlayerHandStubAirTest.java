package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pins the {@code Player.getItemInHand} / {@code getItemInMainHand}
 * routing introduced for Essentials's {@code /book} (and the rest of
 * the held-item commands: {@code /skull}, {@code /more}, {@code /lore}).
 * Pre-fix, the dispatch fell through to the default-value branch and
 * returned null; Essentials's commands call {@code item.getType()}
 * immediately and NPE'd before the WRITTEN_BOOK material check could
 * run.
 *
 * <p>Real Bukkit's contract: an empty held slot surfaces as an
 * AIR-typed {@link ItemStack}, never null. The bridge mirrors that.
 */
class PlayerHandStubAirTest {

    @Test
    void getItemInHandReturnsAirStackWhenEmpty() {
        Player p = BukkitPlayer.create("hand-stub", null, null);
        ItemStack held = p.getItemInHand();
        assertNotNull(held, "getItemInHand must never return null");
        assertEquals(Material.AIR, held.getType());
    }

    @Test
    void getItemInHandRoundTripsAcrossCalls() {
        // Pre-fix the dispatch fell through to defaultValue and returned
        // null on every call. The fix routes through the inventory
        // (which is itself a no-op stub when there's no rd-api backing),
        // and the wrapping AIR fallback kicks in. Either way the stack
        // is non-null on every call — pin both.
        Player p = BukkitPlayer.create("hand-stub-2", null, null);
        assertNotNull(p.getItemInHand());
        assertNotNull(p.getItemInHand());
    }
}
