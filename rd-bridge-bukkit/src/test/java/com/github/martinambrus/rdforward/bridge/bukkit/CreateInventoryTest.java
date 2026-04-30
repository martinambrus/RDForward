package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EssentialsX 2.21.2's {@code /disposal} (Commanddisposal) calls
 * {@code server.createInventory(holder, 36, title)} and feeds the result
 * to {@code player.openInventory(...)}. RDForward has no client-side
 * container UI, but the API call must NOT throw — a NoSuchMethodError
 * here aborts the command with no recovery.
 */
class CreateInventoryTest {

    private StubRdServer rd;

    @BeforeEach void setUp() {
        rd = new StubRdServer();
        BukkitBridge.install(rd);
    }

    @AfterEach void tearDown() {
        BukkitBridge.uninstall();
    }

    @Test
    void createInventoryReturnsNonNullForDisposalCallShape() {
        // Exact call shape from Commanddisposal.run.
        Inventory inv = Bukkit.getServer().createInventory(null, 36, "Disposal");
        assertNotNull(inv);
        assertEquals(36, inv.getSize());
        assertTrue(inv.isEmpty());
    }

    @Test
    void createInventorySupportsItemReadWrite() {
        Inventory inv = Bukkit.getServer().createInventory(null, 9, "test");
        ItemStack stone = new ItemStack(org.bukkit.Material.STONE, 1);

        inv.setItem(0, stone);
        assertSame(stone, inv.getItem(0));
        assertFalse(inv.isEmpty());
        // Slot 0 is occupied; the next free slot is 1.
        assertEquals(1, inv.firstEmpty());

        inv.clear();
        assertTrue(inv.isEmpty());
        assertNull(inv.getItem(0));
    }

    @Test
    void createInventoryHonoursHolderField() {
        InventoryHolder holder = () -> null; // bare InventoryHolder lambda
        Inventory inv = Bukkit.getServer().createInventory(holder, 27, "x");
        assertSame(holder, inv.getHolder());
        assertSame(holder, inv.getHolder(false));
    }

    @Test
    void serverIsPrimaryThreadDelegatesToBukkitStatic() {
        // EssentialsX's UserWarpEvent.<init> (Commandwarp.warpUser path)
        // calls server.isPrimaryThread() instance method. NoSuchMethodError
        // here aborts /warp, /sethome, /tpaccept etc.
        boolean direct = org.bukkit.Bukkit.isPrimaryThread();
        boolean viaServer = Bukkit.getServer().isPrimaryThread();
        assertEquals(direct, viaServer);
    }

    @Test
    void createInventoryOverloadsAreNonNull() {
        // All four overloads on Server must return non-null so plugin
        // call sites that vary the signature don't NPE.
        InventoryHolder holder = () -> null;
        assertNotNull(Bukkit.getServer().createInventory(holder, 36));
        assertNotNull(Bukkit.getServer().createInventory(holder, 36, "title"));
        assertNotNull(Bukkit.getServer().createInventory(holder, InventoryType.HOPPER));
        assertNotNull(Bukkit.getServer().createInventory(holder, InventoryType.HOPPER, "title"));
    }
}
