package org.bukkit.generator;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ChunkGenerator$ChunkData {
    int getMinHeight();
    int getMaxHeight();
    org.bukkit.block.Biome getBiome(int arg0, int arg1, int arg2);
    void setBlock(int arg0, int arg1, int arg2, org.bukkit.Material arg3);
    void setBlock(int arg0, int arg1, int arg2, org.bukkit.material.MaterialData arg3);
    void setBlock(int arg0, int arg1, int arg2, org.bukkit.block.data.BlockData arg3);
    void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, org.bukkit.Material arg6);
    void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, org.bukkit.material.MaterialData arg6);
    void setRegion(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5, org.bukkit.block.data.BlockData arg6);
    org.bukkit.Material getType(int arg0, int arg1, int arg2);
    org.bukkit.material.MaterialData getTypeAndData(int arg0, int arg1, int arg2);
    org.bukkit.block.data.BlockData getBlockData(int arg0, int arg1, int arg2);
    byte getData(int arg0, int arg1, int arg2);
    int getHeight(org.bukkit.HeightMap arg0, int arg1, int arg2);
}
