package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.BukkitBlock;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Event$Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The auto-generated {@link PlayerInteractEvent} stub returned null from
 * every getter and dropped cancellation on the floor. This rewrite
 * carries state through the event lifecycle so CoreProtect's
 * {@code BlockInspector} (which gates on {@link #isCancelled()}) and
 * Essentials's interact-driven kit/menu listeners both observe the
 * fields they expect.
 */
class PlayerInteractEventStateTest {

    @Test
    void ctorStoresActionItemBlockFaceHandPosition() {
        Block clicked = makeBlock();
        ItemStack item = new ItemStack(Material.STONE);
        org.bukkit.util.Vector pos = new org.bukkit.util.Vector(0.5, 0.5, 0.5);
        PlayerInteractEvent ev = new PlayerInteractEvent(
                /*player*/ null,
                Action.LEFT_CLICK_BLOCK,
                item,
                clicked,
                BlockFace.UP,
                EquipmentSlot.HAND,
                pos);

        assertSame(Action.LEFT_CLICK_BLOCK, ev.getAction());
        assertSame(item, ev.getItem());
        assertSame(clicked, ev.getClickedBlock());
        assertSame(BlockFace.UP, ev.getBlockFace());
        assertSame(EquipmentSlot.HAND, ev.getHand());
        assertSame(pos, ev.getClickedPosition());
    }

    @Test
    void shorterCtorsDefaultHandToHandAndPositionToNull() {
        // Two-overload chain (5-arg, 6-arg) ultimately calls the 7-arg
        // constructor with hand=HAND and position=null when omitted.
        PlayerInteractEvent ev5 = new PlayerInteractEvent(null,
                Action.RIGHT_CLICK_BLOCK, null, null, BlockFace.SELF);
        assertSame(EquipmentSlot.HAND, ev5.getHand());
        assertNull(ev5.getClickedPosition());

        PlayerInteractEvent ev6 = new PlayerInteractEvent(null,
                Action.RIGHT_CLICK_BLOCK, null, null, BlockFace.SELF, null);
        assertSame(EquipmentSlot.HAND, ev6.getHand(),
                "null hand normalised to HAND");
    }

    @Test
    void noArgCtorYieldsAllNullFieldsButNonNullHand() {
        PlayerInteractEvent ev = new PlayerInteractEvent();
        assertNull(ev.getAction());
        assertNull(ev.getItem());
        assertNull(ev.getClickedBlock());
        assertNull(ev.getBlockFace());
        // Hand is normalised to HAND even when the no-arg ctor passes
        // null — matches real Bukkit behaviour where hand is always set.
        assertSame(EquipmentSlot.HAND, ev.getHand());
        assertFalse(ev.isCancelled());
    }

    @Test
    void cancellationFlagToggles() {
        // CoreProtect's BlockInspector cancels the event so the actual
        // break/place doesn't happen — the toggle must persist.
        PlayerInteractEvent ev = new PlayerInteractEvent();
        assertFalse(ev.isCancelled());
        ev.setCancelled(true);
        assertTrue(ev.isCancelled());
        ev.setCancelled(false);
        assertFalse(ev.isCancelled());
    }

    @Test
    void hasBlockHasItemReflectStoredFields() {
        Block clicked = makeBlock();
        ItemStack item = new ItemStack(Material.STONE);
        PlayerInteractEvent withBoth = new PlayerInteractEvent(null,
                Action.LEFT_CLICK_BLOCK, item, clicked, BlockFace.UP);
        PlayerInteractEvent withNeither = new PlayerInteractEvent();

        assertTrue(withBoth.hasBlock());
        assertTrue(withBoth.hasItem());
        assertFalse(withNeither.hasBlock());
        assertFalse(withNeither.hasItem());
    }

    @Test
    void getMaterialReturnsItemTypeOrAir() {
        ItemStack item = new ItemStack(Material.DIRT);
        PlayerInteractEvent withItem = new PlayerInteractEvent(null,
                Action.LEFT_CLICK_BLOCK, item, null, BlockFace.UP);
        PlayerInteractEvent withoutItem = new PlayerInteractEvent();

        assertSame(Material.DIRT, withItem.getMaterial());
        // No item -> AIR (real Bukkit semantic).
        assertSame(Material.AIR, withoutItem.getMaterial());
    }

    @Test
    void useInteractedBlockAndUseItemInHandDefaultToDefault() {
        // Plugins like Essentials consult these to decide whether to
        // suppress the consequent vanilla action — defaults must mirror
        // real Bukkit's Result.DEFAULT.
        PlayerInteractEvent ev = new PlayerInteractEvent();
        assertSame(Event$Result.DEFAULT, ev.useInteractedBlock());
        assertSame(Event$Result.DEFAULT, ev.useItemInHand());

        ev.setUseInteractedBlock(Event$Result.DENY);
        ev.setUseItemInHand(Event$Result.ALLOW);
        assertSame(Event$Result.DENY, ev.useInteractedBlock());
        assertSame(Event$Result.ALLOW, ev.useItemInHand());
    }

    @Test
    void getInteractionPointAddsClickedOffsetToBlockOrigin() {
        Block clicked = new BukkitBlock(new StubWorld(), 10, 64, 20, Material.STONE);
        org.bukkit.util.Vector pos = new org.bukkit.util.Vector(0.5d, 0.25d, 0.75d);
        PlayerInteractEvent ev = new PlayerInteractEvent(null,
                Action.RIGHT_CLICK_BLOCK, null, clicked, BlockFace.UP,
                EquipmentSlot.HAND, pos);

        org.bukkit.Location ip = ev.getInteractionPoint();
        assertNotNull(ip);
        // Block origin (10, 64, 20) + offset (0.5, 0.25, 0.75)
        org.junit.jupiter.api.Assertions.assertEquals(10.5d, ip.getX(), 1e-9);
        org.junit.jupiter.api.Assertions.assertEquals(64.25d, ip.getY(), 1e-9);
        org.junit.jupiter.api.Assertions.assertEquals(20.75d, ip.getZ(), 1e-9);
    }

    @Test
    void getInteractionPointNullWhenBlockOrPositionMissing() {
        // No block — point is undefined.
        assertNull(new PlayerInteractEvent().getInteractionPoint());
        // Block but no clicked position — also null.
        Block clicked = new BukkitBlock(new StubWorld(), 0, 0, 0, Material.STONE);
        assertNull(new PlayerInteractEvent(null, Action.LEFT_CLICK_BLOCK,
                null, clicked, BlockFace.UP).getInteractionPoint());
    }

    private static Block makeBlock() {
        return new BukkitBlock(new StubWorld(), 0, 0, 0, Material.STONE);
    }

    private static final class StubWorld implements World {
        @Override public String getName() { return "stub"; }
        @Override public Block getBlockAt(int x, int y, int z) { return null; }
        @Override public boolean setBlockType(int x, int y, int z, Material type) { return true; }
        @Override public int getMaxHeight() { return 256; }
        @Override public long getTime() { return 0; }
        @Override public void setTime(long time) {}
    }
}
