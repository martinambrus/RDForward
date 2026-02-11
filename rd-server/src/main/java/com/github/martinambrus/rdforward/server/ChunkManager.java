package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.alpha.MapChunkPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PreChunkPacket;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import com.github.martinambrus.rdforward.world.alpha.AlphaLevelFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages chunk loading, unloading, and per-player chunk tracking
 * for Alpha-style infinite worlds.
 *
 * Responsibilities:
 * 1. Track which chunks are loaded in memory
 * 2. Track which chunks each player has received
 * 3. Load/generate chunks on demand when players move
 * 4. Send PreChunkPacket + MapChunkPacket for newly visible chunks
 * 5. Send PreChunkPacket(unload) for chunks that leave view distance
 * 6. Unload chunks from memory when no player needs them
 * 7. Save dirty chunks to disk via AlphaLevelFormat
 *
 * Thread safety: all maps are ConcurrentHashMap. The chunk update cycle
 * is called from the tick loop thread, while player add/remove may come
 * from Netty I/O threads, so concurrent access is expected.
 */
public class ChunkManager {

    /** Default view distance in chunks (radius around the player's chunk). */
    public static final int DEFAULT_VIEW_DISTANCE = 10;

    /** Chunks loaded in memory, keyed by coordinate. */
    private final Map<ChunkCoord, AlphaChunk> loadedChunks = new ConcurrentHashMap<>();

    /** Which chunks each player currently has loaded (sent to their client). */
    private final Map<ConnectedPlayer, Set<ChunkCoord>> playerChunks = new ConcurrentHashMap<>();

    /** World generator for creating new chunks. */
    private final WorldGenerator worldGenerator;

    /** World seed for reproducible generation. */
    private final long seed;

    /** Directory for Alpha-format chunk files. */
    private final File worldDir;

    /** View distance in chunks (radius). */
    private final int viewDistance;

    /** Tracks which chunks have been modified since last save. */
    private final Set<ChunkCoord> dirtyChunks = ConcurrentHashMap.newKeySet();

    public ChunkManager(WorldGenerator worldGenerator, long seed, File worldDir) {
        this(worldGenerator, seed, worldDir, DEFAULT_VIEW_DISTANCE);
    }

    public ChunkManager(WorldGenerator worldGenerator, long seed, File worldDir, int viewDistance) {
        this.worldGenerator = worldGenerator;
        this.seed = seed;
        this.worldDir = worldDir;
        this.viewDistance = viewDistance;
    }

    /**
     * Register a player for chunk tracking.
     * Call this when a player finishes login.
     */
    public void addPlayer(ConnectedPlayer player) {
        playerChunks.put(player, ConcurrentHashMap.newKeySet());
    }

    /**
     * Unregister a player, unloading any chunks only they were using.
     * Call this when a player disconnects.
     */
    public void removePlayer(ConnectedPlayer player) {
        Set<ChunkCoord> chunks = playerChunks.remove(player);
        if (chunks == null) return;

        for (ChunkCoord coord : chunks) {
            if (!isChunkNeededByAnyPlayer(coord)) {
                unloadChunk(coord);
            }
        }
    }

    /**
     * Update chunk loading for a player based on their current position.
     *
     * Calculates which chunks should be visible given the player's position
     * and the view distance, then:
     * - Sends new chunks that entered the view distance
     * - Unloads chunks that left the view distance
     *
     * Player positions are in fixed-point units (divide by 32 for blocks).
     *
     * @param player the player to update chunks for
     */
    public void updatePlayerChunks(ConnectedPlayer player) {
        Set<ChunkCoord> current = playerChunks.get(player);
        if (current == null) return;

        // Convert fixed-point position to chunk coordinates
        int blockX = player.getX() / 32;
        int blockZ = player.getZ() / 32;
        int centerChunkX = blockX >> 4;
        int centerChunkZ = blockZ >> 4;

        // Calculate desired chunk set within view distance
        Set<ChunkCoord> desired = new HashSet<>();
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                desired.add(new ChunkCoord(centerChunkX + dx, centerChunkZ + dz));
            }
        }

        // Find chunks to load (in desired but not in current)
        List<ChunkCoord> toLoad = new ArrayList<>();
        for (ChunkCoord coord : desired) {
            if (!current.contains(coord)) {
                toLoad.add(coord);
            }
        }

        // Find chunks to unload (in current but not in desired)
        List<ChunkCoord> toUnload = new ArrayList<>();
        for (ChunkCoord coord : current) {
            if (!desired.contains(coord)) {
                toUnload.add(coord);
            }
        }

        // Sort chunks to load by distance from player (closest first)
        toLoad.sort((a, b) -> {
            int distA = (a.getX() - centerChunkX) * (a.getX() - centerChunkX)
                      + (a.getZ() - centerChunkZ) * (a.getZ() - centerChunkZ);
            int distB = (b.getX() - centerChunkX) * (b.getX() - centerChunkX)
                      + (b.getZ() - centerChunkZ) * (b.getZ() - centerChunkZ);
            return Integer.compare(distA, distB);
        });

        // Unload chunks that left view distance
        for (ChunkCoord coord : toUnload) {
            sendChunkUnload(player, coord);
            current.remove(coord);
            if (!isChunkNeededByAnyPlayer(coord)) {
                unloadChunk(coord);
            }
        }

        // Load and send chunks that entered view distance
        for (ChunkCoord coord : toLoad) {
            AlphaChunk chunk = getOrLoadChunk(coord);
            if (chunk != null) {
                sendChunkToPlayer(player, chunk);
                current.add(coord);
            }
        }
    }

    /**
     * Send all chunks within view distance to a player.
     * Used during initial login for Alpha-mode clients.
     *
     * @param player the player to send chunks to
     * @param blockX player block X position
     * @param blockZ player block Z position
     */
    public void sendInitialChunks(ConnectedPlayer player, int blockX, int blockZ) {
        Set<ChunkCoord> current = playerChunks.get(player);
        if (current == null) {
            current = ConcurrentHashMap.newKeySet();
            playerChunks.put(player, current);
        }

        int centerChunkX = blockX >> 4;
        int centerChunkZ = blockZ >> 4;

        // Build sorted list of chunks to send (closest first)
        List<ChunkCoord> toSend = new ArrayList<>();
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                toSend.add(new ChunkCoord(centerChunkX + dx, centerChunkZ + dz));
            }
        }
        toSend.sort((a, b) -> {
            int distA = (a.getX() - centerChunkX) * (a.getX() - centerChunkX)
                      + (a.getZ() - centerChunkZ) * (a.getZ() - centerChunkZ);
            int distB = (b.getX() - centerChunkX) * (b.getX() - centerChunkX)
                      + (b.getZ() - centerChunkZ) * (b.getZ() - centerChunkZ);
            return Integer.compare(distA, distB);
        });

        for (ChunkCoord coord : toSend) {
            AlphaChunk chunk = getOrLoadChunk(coord);
            if (chunk != null) {
                sendChunkToPlayer(player, chunk);
                current.add(coord);
            }
        }
    }

    /**
     * Get a chunk from memory, or load/generate it if not loaded.
     */
    public AlphaChunk getOrLoadChunk(ChunkCoord coord) {
        AlphaChunk chunk = loadedChunks.get(coord);
        if (chunk != null) {
            return chunk;
        }

        // Try loading from disk
        try {
            chunk = AlphaLevelFormat.loadChunk(worldDir, coord.getX(), coord.getZ());
        } catch (IOException e) {
            System.err.println("Failed to load chunk " + coord + ": " + e.getMessage());
        }

        // If not on disk, generate
        if (chunk == null && worldGenerator.supportsChunkGeneration()) {
            chunk = worldGenerator.generateChunk(coord.getX(), coord.getZ(), seed);
        }

        if (chunk != null) {
            loadedChunks.put(coord, chunk);
        }

        return chunk;
    }

    /**
     * Get a loaded chunk without loading/generating. Returns null if not in memory.
     */
    public AlphaChunk getChunkIfLoaded(ChunkCoord coord) {
        return loadedChunks.get(coord);
    }

    /**
     * Get a block from the chunk world.
     * Loads the chunk if necessary. Returns 0 (AIR) if chunk can't be loaded.
     */
    public byte getBlock(int blockX, int blockY, int blockZ) {
        ChunkCoord coord = ChunkCoord.fromBlock(blockX, blockZ);
        AlphaChunk chunk = getOrLoadChunk(coord);
        if (chunk == null || blockY < 0 || blockY >= AlphaChunk.HEIGHT) {
            return 0;
        }
        int localX = blockX & 15;
        int localZ = blockZ & 15;
        return (byte) chunk.getBlock(localX, blockY, localZ);
    }

    /**
     * Set a block in the chunk world.
     * Returns true if the block was changed.
     */
    public boolean setBlock(int blockX, int blockY, int blockZ, byte blockType) {
        ChunkCoord coord = ChunkCoord.fromBlock(blockX, blockZ);
        AlphaChunk chunk = getOrLoadChunk(coord);
        if (chunk == null || blockY < 0 || blockY >= AlphaChunk.HEIGHT) {
            return false;
        }
        int localX = blockX & 15;
        int localZ = blockZ & 15;
        int oldBlock = chunk.getBlock(localX, blockY, localZ);
        if (oldBlock == (blockType & 0xFF)) {
            return false;
        }
        chunk.setBlock(localX, blockY, localZ, blockType & 0xFF);
        dirtyChunks.add(coord);
        return true;
    }

    /**
     * Save all dirty chunks to disk.
     */
    public void saveAllDirty() {
        for (ChunkCoord coord : dirtyChunks) {
            AlphaChunk chunk = loadedChunks.get(coord);
            if (chunk != null) {
                try {
                    AlphaLevelFormat.saveChunk(worldDir, chunk);
                } catch (IOException e) {
                    System.err.println("Failed to save chunk " + coord + ": " + e.getMessage());
                }
            }
        }
        int count = dirtyChunks.size();
        dirtyChunks.clear();
        if (count > 0) {
            System.out.println("Saved " + count + " dirty chunk(s) to " + worldDir);
        }
    }

    /**
     * Save all loaded chunks to disk (for server shutdown).
     */
    public void saveAll() {
        int count = 0;
        for (Map.Entry<ChunkCoord, AlphaChunk> entry : loadedChunks.entrySet()) {
            try {
                AlphaLevelFormat.saveChunk(worldDir, entry.getValue());
                count++;
            } catch (IOException e) {
                System.err.println("Failed to save chunk " + entry.getKey() + ": " + e.getMessage());
            }
        }
        dirtyChunks.clear();
        System.out.println("Saved " + count + " chunk(s) to " + worldDir);
    }

    /**
     * Send a chunk to a player via PreChunkPacket + MapChunkPacket.
     */
    private void sendChunkToPlayer(ConnectedPlayer player, AlphaChunk chunk) {
        // First: tell client to allocate the chunk column
        player.sendPacket(new PreChunkPacket(chunk.getXPos(), chunk.getZPos(), true));

        // Then: send the compressed chunk data
        try {
            byte[] compressed = chunk.serializeForAlphaProtocol();
            int blockX = chunk.getXPos() * AlphaChunk.WIDTH;
            int blockZ = chunk.getZPos() * AlphaChunk.DEPTH;
            player.sendPacket(new MapChunkPacket(
                blockX, (short) 0, blockZ,
                AlphaChunk.WIDTH - 1,   // sizeX = 15 (width - 1)
                AlphaChunk.HEIGHT - 1,  // sizeY = 127 (height - 1)
                AlphaChunk.DEPTH - 1,   // sizeZ = 15 (depth - 1)
                compressed
            ));
        } catch (IOException e) {
            System.err.println("Failed to serialize chunk (" + chunk.getXPos() + ", " + chunk.getZPos()
                + ") for player " + player.getUsername() + ": " + e.getMessage());
        }
    }

    /**
     * Tell a player's client to unload a chunk.
     */
    private void sendChunkUnload(ConnectedPlayer player, ChunkCoord coord) {
        player.sendPacket(new PreChunkPacket(coord.getX(), coord.getZ(), false));
    }

    /**
     * Unload a chunk from memory, saving it to disk first if dirty.
     */
    private void unloadChunk(ChunkCoord coord) {
        AlphaChunk chunk = loadedChunks.remove(coord);
        if (chunk != null && dirtyChunks.remove(coord)) {
            try {
                AlphaLevelFormat.saveChunk(worldDir, chunk);
            } catch (IOException e) {
                System.err.println("Failed to save chunk " + coord + " during unload: " + e.getMessage());
            }
        }
    }

    /**
     * Check if any player currently needs a chunk loaded.
     */
    private boolean isChunkNeededByAnyPlayer(ChunkCoord coord) {
        for (Set<ChunkCoord> chunks : playerChunks.values()) {
            if (chunks.contains(coord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the number of chunks currently loaded in memory.
     */
    public int getLoadedChunkCount() {
        return loadedChunks.size();
    }

    /**
     * Get the view distance (in chunks).
     */
    public int getViewDistance() {
        return viewDistance;
    }

    /**
     * Get the set of chunks a player currently has loaded.
     * Returns an empty set if the player is not tracked.
     */
    public Set<ChunkCoord> getPlayerLoadedChunks(ConnectedPlayer player) {
        Set<ChunkCoord> chunks = playerChunks.get(player);
        return chunks != null ? chunks : new HashSet<>();
    }
}
