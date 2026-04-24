package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Enderman extends org.bukkit.entity.Monster {
    boolean teleportRandomly();
    org.bukkit.material.MaterialData getCarriedMaterial();
    void setCarriedMaterial(org.bukkit.material.MaterialData arg0);
    org.bukkit.block.data.BlockData getCarriedBlock();
    void setCarriedBlock(org.bukkit.block.data.BlockData arg0);
    boolean teleport();
    boolean teleportTowards(org.bukkit.entity.Entity arg0);
    boolean isScreaming();
    void setScreaming(boolean arg0);
    boolean hasBeenStaredAt();
    void setHasBeenStaredAt(boolean arg0);
}
