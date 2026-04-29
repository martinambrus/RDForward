// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

/**
 * Functional {@link PlayerEvent} subtype that mirrors Bukkit's
 * shape closely enough for plugins gated on
 * {@link #isCancelled()} (CoreProtect's inspector mode) to work. The
 * auto-generated stub returned null from every getter and dropped
 * cancellation on the floor; CoreProtect's {@code BlockInspector}
 * registers via {@code @EventHandler PlayerInteractEvent} and cancels
 * the event to prevent the actual break/place — without state-carrying
 * fields the cancellation never propagated.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PlayerInteractEvent extends org.bukkit.event.player.PlayerEvent implements org.bukkit.event.Cancellable {

    private final org.bukkit.event.block.Action action;
    private final org.bukkit.inventory.ItemStack item;
    private final org.bukkit.block.Block clickedBlock;
    private final org.bukkit.block.BlockFace blockFace;
    private final org.bukkit.inventory.EquipmentSlot hand;
    private final org.bukkit.util.Vector clickedPosition;
    private boolean cancelled;
    private org.bukkit.event.Event$Result useInteractedBlock = org.bukkit.event.Event$Result.DEFAULT;
    private org.bukkit.event.Event$Result useItemInHand = org.bukkit.event.Event$Result.DEFAULT;

    public PlayerInteractEvent(org.bukkit.entity.Player p, org.bukkit.event.block.Action a,
                               org.bukkit.inventory.ItemStack i, org.bukkit.block.Block b,
                               org.bukkit.block.BlockFace f) {
        this(p, a, i, b, f, org.bukkit.inventory.EquipmentSlot.HAND, null);
    }
    public PlayerInteractEvent(org.bukkit.entity.Player p, org.bukkit.event.block.Action a,
                               org.bukkit.inventory.ItemStack i, org.bukkit.block.Block b,
                               org.bukkit.block.BlockFace f, org.bukkit.inventory.EquipmentSlot h) {
        this(p, a, i, b, f, h, null);
    }
    public PlayerInteractEvent(org.bukkit.entity.Player p, org.bukkit.event.block.Action a,
                               org.bukkit.inventory.ItemStack i, org.bukkit.block.Block b,
                               org.bukkit.block.BlockFace f, org.bukkit.inventory.EquipmentSlot h,
                               org.bukkit.util.Vector pos) {
        super(p);
        this.action = a;
        this.item = i;
        this.clickedBlock = b;
        this.blockFace = f;
        this.hand = h == null ? org.bukkit.inventory.EquipmentSlot.HAND : h;
        this.clickedPosition = pos;
    }
    public PlayerInteractEvent() { this(null, null, null, null, null, null, null); }

    public org.bukkit.event.block.Action getAction() { return action; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean c) { this.cancelled = c; }
    public org.bukkit.inventory.ItemStack getItem() { return item; }
    public org.bukkit.Material getMaterial() { return item == null ? org.bukkit.Material.AIR : item.getType(); }
    public boolean hasBlock() { return clickedBlock != null; }
    public boolean hasItem() { return item != null; }
    public boolean isBlockInHand() { return hasItem() && getMaterial().isBlock(); }
    public org.bukkit.block.Block getClickedBlock() { return clickedBlock; }
    public org.bukkit.block.BlockFace getBlockFace() { return blockFace; }
    public org.bukkit.inventory.EquipmentSlot getHand() { return hand; }
    public org.bukkit.util.Vector getClickedPosition() { return clickedPosition; }
    public org.bukkit.Location getInteractionPoint() {
        if (clickedBlock == null || clickedPosition == null) return null;
        return new org.bukkit.Location(clickedBlock.getWorld(),
                clickedBlock.getX() + clickedPosition.getX(),
                clickedBlock.getY() + clickedPosition.getY(),
                clickedBlock.getZ() + clickedPosition.getZ());
    }
    public org.bukkit.event.Event$Result useInteractedBlock() { return useInteractedBlock; }
    public void setUseInteractedBlock(org.bukkit.event.Event$Result r) { this.useInteractedBlock = r; }
    public org.bukkit.event.Event$Result useItemInHand() { return useItemInHand; }
    public void setUseItemInHand(org.bukkit.event.Event$Result r) { this.useItemInHand = r; }
    // Inherits getHandlers() / getHandlerList() from Event so plugins
    // that self-unregister via event.getHandlers().unregister(this)
    // (Essentials's SignPlayerListener) reach a non-null HandlerList.
    // The prior return-null overrides masked the base method and NPE'd.
}
