package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EntityBlockStorage extends org.bukkit.block.TileState {
    boolean isFull();
    int getEntityCount();
    int getMaxEntities();
    void setMaxEntities(int arg0);
    java.util.List releaseEntities();
    void addEntity(org.bukkit.entity.Entity arg0);
    void clearEntities();
}
