package org.bukkit.generator;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class ChunkGenerator {
    public ChunkGenerator() {}
    public void generateNoise(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3, org.bukkit.generator.ChunkGenerator$ChunkData arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.generateNoise(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;IILorg/bukkit/generator/ChunkGenerator$ChunkData;)V");
    }
    public void generateSurface(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3, org.bukkit.generator.ChunkGenerator$ChunkData arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.generateSurface(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;IILorg/bukkit/generator/ChunkGenerator$ChunkData;)V");
    }
    public void generateBedrock(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3, org.bukkit.generator.ChunkGenerator$ChunkData arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.generateBedrock(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;IILorg/bukkit/generator/ChunkGenerator$ChunkData;)V");
    }
    public void generateCaves(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3, org.bukkit.generator.ChunkGenerator$ChunkData arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.generateCaves(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;IILorg/bukkit/generator/ChunkGenerator$ChunkData;)V");
    }
    public org.bukkit.generator.BiomeProvider getDefaultBiomeProvider(org.bukkit.generator.WorldInfo arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.getDefaultBiomeProvider(Lorg/bukkit/generator/WorldInfo;)Lorg/bukkit/generator/BiomeProvider;");
        return null;
    }
    public int getBaseHeight(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3, org.bukkit.HeightMap arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.getBaseHeight(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;IILorg/bukkit/HeightMap;)I");
        return 0;
    }
    public org.bukkit.generator.ChunkGenerator$ChunkData generateChunkData(org.bukkit.World arg0, java.util.Random arg1, int arg2, int arg3, org.bukkit.generator.ChunkGenerator$BiomeGrid arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.generateChunkData(Lorg/bukkit/World;Ljava/util/Random;IILorg/bukkit/generator/ChunkGenerator$BiomeGrid;)Lorg/bukkit/generator/ChunkGenerator$ChunkData;");
        return null;
    }
    protected final org.bukkit.generator.ChunkGenerator$ChunkData createChunkData(org.bukkit.World arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.createChunkData(Lorg/bukkit/World;)Lorg/bukkit/generator/ChunkGenerator$ChunkData;");
        return null;
    }
    public boolean canSpawn(org.bukkit.World arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.canSpawn(Lorg/bukkit/World;II)Z");
        return false;
    }
    public java.util.List getDefaultPopulators(org.bukkit.World arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.getDefaultPopulators(Lorg/bukkit/World;)Ljava/util/List;");
        return java.util.Collections.emptyList();
    }
    public org.bukkit.Location getFixedSpawnLocation(org.bukkit.World arg0, java.util.Random arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.getFixedSpawnLocation(Lorg/bukkit/World;Ljava/util/Random;)Lorg/bukkit/Location;");
        return null;
    }
    public boolean isParallelCapable() {
        return false;
    }
    public boolean shouldGenerateNoise() {
        return false;
    }
    public boolean shouldGenerateNoise(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.shouldGenerateNoise(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;II)Z");
        return false;
    }
    public boolean shouldGenerateSurface() {
        return false;
    }
    public boolean shouldGenerateSurface(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.shouldGenerateSurface(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;II)Z");
        return false;
    }
    public boolean shouldGenerateBedrock() {
        return false;
    }
    public boolean shouldGenerateCaves() {
        return false;
    }
    public boolean shouldGenerateCaves(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.shouldGenerateCaves(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;II)Z");
        return false;
    }
    public boolean shouldGenerateDecorations() {
        return false;
    }
    public boolean shouldGenerateDecorations(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.shouldGenerateDecorations(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;II)Z");
        return false;
    }
    public boolean shouldGenerateMobs() {
        return false;
    }
    public boolean shouldGenerateMobs(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.shouldGenerateMobs(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;II)Z");
        return false;
    }
    public boolean shouldGenerateStructures() {
        return false;
    }
    public boolean shouldGenerateStructures(org.bukkit.generator.WorldInfo arg0, java.util.Random arg1, int arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.ChunkGenerator.shouldGenerateStructures(Lorg/bukkit/generator/WorldInfo;Ljava/util/Random;II)Z");
        return false;
    }
}
