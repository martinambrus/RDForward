package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ChiseledBookshelf extends io.papermc.paper.block.TileStateInventoryHolder {
    int getLastInteractedSlot();
    void setLastInteractedSlot(int arg0);
    org.bukkit.inventory.ChiseledBookshelfInventory getInventory();
    org.bukkit.inventory.ChiseledBookshelfInventory getSnapshotInventory();
    int getSlot(org.bukkit.util.Vector arg0);
}
