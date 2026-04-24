package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Jukebox extends io.papermc.paper.block.TileStateInventoryHolder {
    org.bukkit.Material getPlaying();
    void setPlaying(org.bukkit.Material arg0);
    boolean hasRecord();
    org.bukkit.inventory.ItemStack getRecord();
    void setRecord(org.bukkit.inventory.ItemStack arg0);
    boolean isPlaying();
    boolean startPlaying();
    void stopPlaying();
    boolean eject();
    org.bukkit.inventory.JukeboxInventory getInventory();
    org.bukkit.inventory.JukeboxInventory getSnapshotInventory();
}
