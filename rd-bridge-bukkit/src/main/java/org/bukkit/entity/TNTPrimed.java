package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TNTPrimed extends org.bukkit.entity.Explosive {
    void setFuseTicks(int arg0);
    int getFuseTicks();
    org.bukkit.entity.Entity getSource();
    void setSource(org.bukkit.entity.Entity arg0);
    default org.bukkit.Location getSourceLoc() {
        return null;
    }
    void setBlockData(org.bukkit.block.data.BlockData arg0);
    org.bukkit.block.data.BlockData getBlockData();
}
