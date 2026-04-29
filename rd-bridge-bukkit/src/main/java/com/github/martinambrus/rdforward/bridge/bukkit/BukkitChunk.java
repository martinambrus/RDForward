// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import org.bukkit.Chunk;
import org.bukkit.Chunk$LoadLevel;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.structure.Structure;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

/**
 * Snapshot {@link Chunk} returned by {@link Block#getChunk()}. CoreProtect's
 * {@code RollbackProcessor.processChunk} feeds this back to
 * {@code World.isChunkLoaded(Chunk)} and reads {@code getEntities()} to
 * scan for entity rollback work; without a non-null Chunk the call
 * {@link NoSuchMethodError}s.
 *
 * <p>RDForward holds the whole world in memory so every chunk is treated
 * as loaded; entity rollback is out of scope (no entity audit), so
 * {@link #getEntities()} returns an empty array. Other methods are
 * either safe defaults or {@code StubCallLog}-tagged no-ops.
 */
public final class BukkitChunk implements Chunk {

    private final World world;
    private final int x;
    private final int z;

    public BukkitChunk(World world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override public int getX() { return x; }
    @Override public int getZ() { return z; }
    @Override public World getWorld() { return world; }

    @Override
    public Block getBlock(int dx, int dy, int dz) {
        if (world == null) return null;
        return world.getBlockAt((x << 4) + dx, dy, (z << 4) + dz);
    }

    @Override public ChunkSnapshot getChunkSnapshot(boolean a, boolean b, boolean c, boolean d) { return null; }

    @Override public boolean isEntitiesLoaded() { return true; }
    @Override public Entity[] getEntities() { return new Entity[0]; }
    @Override public BlockState[] getTileEntities(boolean useSnapshot) { return new BlockState[0]; }
    @Override public Collection getTileEntities(Predicate filter, boolean useSnapshot) { return Collections.emptyList(); }
    @Override public boolean isGenerated() { return true; }
    @Override public boolean isLoaded() { return true; }
    @Override public boolean load(boolean generate) { return true; }
    @Override public boolean load() { return true; }
    @Override public boolean unload(boolean save) { return false; }
    @Override public boolean unload() { return false; }
    @Override public boolean isSlimeChunk() { return false; }
    @Override public boolean isForceLoaded() { return false; }
    @Override public void setForceLoaded(boolean forced) {}
    @Override public boolean addPluginChunkTicket(Plugin plugin) { return false; }
    @Override public boolean removePluginChunkTicket(Plugin plugin) { return false; }
    @Override public Collection getPluginChunkTickets() { return Collections.emptyList(); }
    @Override public long getInhabitedTime() { return 0L; }
    @Override public void setInhabitedTime(long ticks) {}
    @Override public boolean contains(BlockData data) { return false; }
    @Override public boolean contains(Biome biome) { return false; }
    @Override public Chunk$LoadLevel getLoadLevel() { return null; }
    @Override public Collection getStructures() { return Collections.emptyList(); }
    @Override public Collection getStructures(Structure structure) { return Collections.emptyList(); }
    @Override public Collection<Player> getPlayersSeeingChunk() { return Collections.emptyList(); }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        return new StubPersistentDataContainer();
    }
}
