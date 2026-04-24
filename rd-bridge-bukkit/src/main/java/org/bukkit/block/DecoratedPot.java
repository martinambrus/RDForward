package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DecoratedPot extends io.papermc.paper.block.TileStateInventoryHolder, org.bukkit.loot.Lootable {
    void setSherd(org.bukkit.block.DecoratedPot$Side arg0, org.bukkit.Material arg1);
    org.bukkit.Material getSherd(org.bukkit.block.DecoratedPot$Side arg0);
    java.util.Map getSherds();
    java.util.List getShards();
    org.bukkit.inventory.DecoratedPotInventory getInventory();
    org.bukkit.inventory.DecoratedPotInventory getSnapshotInventory();
    void startWobble(org.bukkit.block.DecoratedPot$WobbleStyle arg0);
}
