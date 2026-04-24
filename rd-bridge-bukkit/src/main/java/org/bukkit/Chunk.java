package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Chunk extends org.bukkit.persistence.PersistentDataHolder {
    int getX();
    int getZ();
    default long getChunkKey() {
        return 0L;
    }
    static long getChunkKey(org.bukkit.Location arg0) {
        return 0L;
    }
    static long getChunkKey(int arg0, int arg1) {
        return 0L;
    }
    org.bukkit.World getWorld();
    org.bukkit.block.Block getBlock(int arg0, int arg1, int arg2);
    default org.bukkit.ChunkSnapshot getChunkSnapshot() {
        return null;
    }
    default org.bukkit.ChunkSnapshot getChunkSnapshot(boolean arg0, boolean arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.Chunk.getChunkSnapshot(ZZZ)Lorg/bukkit/ChunkSnapshot;");
        return null;
    }
    org.bukkit.ChunkSnapshot getChunkSnapshot(boolean arg0, boolean arg1, boolean arg2, boolean arg3);
    boolean isEntitiesLoaded();
    org.bukkit.entity.Entity[] getEntities();
    default org.bukkit.block.BlockState[] getTileEntities() {
        return new org.bukkit.block.BlockState[0];
    }
    org.bukkit.block.BlockState[] getTileEntities(boolean arg0);
    java.util.Collection getTileEntities(java.util.function.Predicate arg0, boolean arg1);
    boolean isGenerated();
    boolean isLoaded();
    boolean load(boolean arg0);
    boolean load();
    boolean unload(boolean arg0);
    boolean unload();
    boolean isSlimeChunk();
    boolean isForceLoaded();
    void setForceLoaded(boolean arg0);
    boolean addPluginChunkTicket(org.bukkit.plugin.Plugin arg0);
    boolean removePluginChunkTicket(org.bukkit.plugin.Plugin arg0);
    java.util.Collection getPluginChunkTickets();
    long getInhabitedTime();
    void setInhabitedTime(long arg0);
    boolean contains(org.bukkit.block.data.BlockData arg0);
    boolean contains(org.bukkit.block.Biome arg0);
    org.bukkit.Chunk$LoadLevel getLoadLevel();
    java.util.Collection getStructures();
    java.util.Collection getStructures(org.bukkit.generator.structure.Structure arg0);
    java.util.Collection getPlayersSeeingChunk();
}
