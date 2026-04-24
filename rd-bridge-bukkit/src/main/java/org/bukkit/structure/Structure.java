package org.bukkit.structure;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Structure extends org.bukkit.persistence.PersistentDataHolder {
    org.bukkit.util.BlockVector getSize();
    java.util.List getPalettes();
    int getPaletteCount();
    java.util.List getEntities();
    int getEntityCount();
    void place(org.bukkit.Location arg0, boolean arg1, org.bukkit.block.structure.StructureRotation arg2, org.bukkit.block.structure.Mirror arg3, int arg4, float arg5, java.util.Random arg6);
    void place(org.bukkit.Location arg0, boolean arg1, org.bukkit.block.structure.StructureRotation arg2, org.bukkit.block.structure.Mirror arg3, int arg4, float arg5, java.util.Random arg6, java.util.Collection arg7, java.util.Collection arg8);
    void place(org.bukkit.RegionAccessor arg0, org.bukkit.util.BlockVector arg1, boolean arg2, org.bukkit.block.structure.StructureRotation arg3, org.bukkit.block.structure.Mirror arg4, int arg5, float arg6, java.util.Random arg7);
    void place(org.bukkit.RegionAccessor arg0, org.bukkit.util.BlockVector arg1, boolean arg2, org.bukkit.block.structure.StructureRotation arg3, org.bukkit.block.structure.Mirror arg4, int arg5, float arg6, java.util.Random arg7, java.util.Collection arg8, java.util.Collection arg9);
    void fill(org.bukkit.Location arg0, org.bukkit.Location arg1, boolean arg2);
    void fill(org.bukkit.Location arg0, org.bukkit.util.BlockVector arg1, boolean arg2);
}
