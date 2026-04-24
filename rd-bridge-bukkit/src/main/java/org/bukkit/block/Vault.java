package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vault extends org.bukkit.block.TileState {
    double getActivationRange();
    void setActivationRange(double arg0);
    double getDeactivationRange();
    void setDeactivationRange(double arg0);
    org.bukkit.inventory.ItemStack getKeyItem();
    void setKeyItem(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.loot.LootTable getLootTable();
    void setLootTable(org.bukkit.loot.LootTable arg0);
    org.bukkit.loot.LootTable getDisplayedLootTable();
    void setDisplayedLootTable(org.bukkit.loot.LootTable arg0);
    long getNextStateUpdateTime();
    void setNextStateUpdateTime(long arg0);
    java.util.Collection getRewardedPlayers();
    boolean addRewardedPlayer(java.util.UUID arg0);
    boolean removeRewardedPlayer(java.util.UUID arg0);
    boolean hasRewardedPlayer(java.util.UUID arg0);
    java.util.Set getConnectedPlayers();
    boolean hasConnectedPlayer(java.util.UUID arg0);
    org.bukkit.inventory.ItemStack getDisplayedItem();
    void setDisplayedItem(org.bukkit.inventory.ItemStack arg0);
}
