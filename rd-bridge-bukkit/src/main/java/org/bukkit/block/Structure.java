package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Structure extends org.bukkit.block.TileState {
    java.lang.String getStructureName();
    void setStructureName(java.lang.String arg0);
    java.lang.String getAuthor();
    void setAuthor(java.lang.String arg0);
    void setAuthor(org.bukkit.entity.LivingEntity arg0);
    org.bukkit.util.BlockVector getRelativePosition();
    void setRelativePosition(org.bukkit.util.BlockVector arg0);
    org.bukkit.util.BlockVector getStructureSize();
    void setStructureSize(org.bukkit.util.BlockVector arg0);
    void setMirror(org.bukkit.block.structure.Mirror arg0);
    org.bukkit.block.structure.Mirror getMirror();
    void setRotation(org.bukkit.block.structure.StructureRotation arg0);
    org.bukkit.block.structure.StructureRotation getRotation();
    void setUsageMode(org.bukkit.block.structure.UsageMode arg0);
    org.bukkit.block.structure.UsageMode getUsageMode();
    void setIgnoreEntities(boolean arg0);
    boolean isIgnoreEntities();
    void setShowAir(boolean arg0);
    boolean isShowAir();
    void setBoundingBoxVisible(boolean arg0);
    boolean isBoundingBoxVisible();
    void setIntegrity(float arg0);
    float getIntegrity();
    void setSeed(long arg0);
    long getSeed();
    void setMetadata(java.lang.String arg0);
    java.lang.String getMetadata();
}
