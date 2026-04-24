package org.bukkit.spawner;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Spawner extends org.bukkit.spawner.BaseSpawner {
    void setDelay(int arg0);
    int getMinSpawnDelay();
    void setMinSpawnDelay(int arg0);
    int getMaxSpawnDelay();
    void setMaxSpawnDelay(int arg0);
    int getSpawnCount();
    void setSpawnCount(int arg0);
    int getMaxNearbyEntities();
    void setMaxNearbyEntities(int arg0);
    boolean isActivated();
    void resetTimer();
    void setSpawnedItem(org.bukkit.inventory.ItemStack arg0);
}
