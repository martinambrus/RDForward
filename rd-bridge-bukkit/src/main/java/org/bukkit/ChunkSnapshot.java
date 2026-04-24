package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ChunkSnapshot {
    int getX();
    int getZ();
    java.lang.String getWorldName();
    net.kyori.adventure.key.Key getWorldKey();
    org.bukkit.Material getBlockType(int arg0, int arg1, int arg2);
    org.bukkit.block.data.BlockData getBlockData(int arg0, int arg1, int arg2);
    int getData(int arg0, int arg1, int arg2);
    int getBlockSkyLight(int arg0, int arg1, int arg2);
    int getBlockEmittedLight(int arg0, int arg1, int arg2);
    int getHighestBlockYAt(int arg0, int arg1);
    org.bukkit.block.Biome getBiome(int arg0, int arg1);
    org.bukkit.block.Biome getBiome(int arg0, int arg1, int arg2);
    double getRawBiomeTemperature(int arg0, int arg1);
    double getRawBiomeTemperature(int arg0, int arg1, int arg2);
    long getCaptureFullTime();
    boolean isSectionEmpty(int arg0);
    boolean contains(org.bukkit.block.data.BlockData arg0);
    boolean contains(org.bukkit.block.Biome arg0);
}
