package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SpawnEggMeta extends org.bukkit.inventory.meta.ItemMeta {
    org.bukkit.entity.EntityType getSpawnedType();
    void setSpawnedType(org.bukkit.entity.EntityType arg0);
    org.bukkit.entity.EntitySnapshot getSpawnedEntity();
    void setSpawnedEntity(org.bukkit.entity.EntitySnapshot arg0);
    org.bukkit.entity.EntityType getCustomSpawnedType();
    void setCustomSpawnedType(org.bukkit.entity.EntityType arg0);
    org.bukkit.inventory.meta.SpawnEggMeta clone();
}
