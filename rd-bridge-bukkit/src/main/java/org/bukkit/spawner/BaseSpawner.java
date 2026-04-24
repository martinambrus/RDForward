package org.bukkit.spawner;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BaseSpawner {
    org.bukkit.entity.EntityType getSpawnedType();
    void setSpawnedType(org.bukkit.entity.EntityType arg0);
    int getDelay();
    void setDelay(int arg0);
    int getRequiredPlayerRange();
    void setRequiredPlayerRange(int arg0);
    int getSpawnRange();
    void setSpawnRange(int arg0);
    org.bukkit.entity.EntitySnapshot getSpawnedEntity();
    void setSpawnedEntity(org.bukkit.entity.EntitySnapshot arg0);
    void setSpawnedEntity(org.bukkit.block.spawner.SpawnerEntry arg0);
    void addPotentialSpawn(org.bukkit.entity.EntitySnapshot arg0, int arg1, org.bukkit.block.spawner.SpawnRule arg2);
    void addPotentialSpawn(org.bukkit.block.spawner.SpawnerEntry arg0);
    void setPotentialSpawns(java.util.Collection arg0);
    java.util.List getPotentialSpawns();
}
