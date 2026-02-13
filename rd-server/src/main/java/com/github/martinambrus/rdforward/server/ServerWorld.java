package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.world.BlockRegistry;
import com.github.martinambrus.rdforward.world.WorldGenerator;

import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Server-authoritative world state.
 *
 * Holds the block array for the entire world. All block modifications
 * must go through this class so the server remains the source of truth.
 *
 * Uses RubyDung's flat array layout: index = (y * depth + z) * width + x.
 * The world dimensions match the original RubyDung default (256x64x256).
 *
 * Thread safety: block get/set are synchronized so the tick loop and
 * Netty I/O threads can safely access world state.
 */
public class ServerWorld {

    private static final String SAVE_FILE = "server-world.dat";
    private static final String PLAYERS_FILE = "server-players.dat";

    private final int width;
    private final int height;
    private final int depth;
    private final byte[] blocks;
    private volatile boolean dirty = false;

    /** Queued block changes from clients, processed during tick loop. */
    private final Queue<PendingBlockChange> pendingBlockChanges = new ConcurrentLinkedQueue<>();

    /** In-memory cache of all known player positions (online + disconnected). */
    private final java.util.concurrent.ConcurrentHashMap<String, short[]> playerPositionCache =
            new java.util.concurrent.ConcurrentHashMap<>();

    public ServerWorld(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new byte[width * height * depth];
    }

    /**
     * Generate the world using the given generator.
     *
     * For finite-world generators, this fills the entire block array in one
     * pass. For chunk-based generators (Alpha-style), the server would call
     * {@link WorldGenerator#generateChunk} on demand instead — that path
     * will be wired when the chunk-based ServerWorld is implemented.
     *
     * @param generator the world generator to use
     * @param seed      world seed for reproducible generation
     */
    public void generate(WorldGenerator generator, long seed) {
        generator.generate(blocks, width, height, depth, seed);
        dirty = true;
    }

    /**
     * Get the block type at the given coordinates.
     * Returns AIR (0) if out of bounds.
     */
    public synchronized byte getBlock(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return (byte) BlockRegistry.AIR;
        }
        return blocks[blockIndex(x, y, z)];
    }

    /**
     * Set a block at the given coordinates.
     * Returns true if the block was changed, false if out of bounds or same value.
     */
    public synchronized boolean setBlock(int x, int y, int z, byte blockType) {
        if (!inBounds(x, y, z)) {
            return false;
        }
        int index = blockIndex(x, y, z);
        if (blocks[index] == blockType) {
            return false;
        }
        blocks[index] = blockType;
        dirty = true;
        return true;
    }

    /**
     * Check if coordinates are within world bounds.
     */
    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    /**
     * Serialize the world for Classic protocol level transfer.
     *
     * MC Classic world format:
     *   [4 bytes] total volume (int, big-endian)
     *   [width * height * depth bytes] block data
     *   All GZip compressed
     *
     * The block ordering in Classic is: for x, for z, for y — i.e., XZY.
     */
    public byte[] serializeForClassicProtocol() throws IOException {
        int volume = width * height * depth;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos);
             DataOutputStream dos = new DataOutputStream(gzip)) {
            // Write volume as 4-byte big-endian int
            dos.writeInt(volume);
            // Write blocks in XZY order (Classic protocol order)
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    for (int y = 0; y < height; y++) {
                        dos.writeByte(blocks[blockIndex(x, y, z)]);
                    }
                }
            }
        }
        return baos.toByteArray();
    }

    /**
     * Load world from disk. Returns true if a saved world was loaded.
     */
    public boolean load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return false;
        }
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            int savedWidth = dis.readInt();
            int savedHeight = dis.readInt();
            int savedDepth = dis.readInt();
            if (savedWidth != width || savedHeight != height || savedDepth != depth) {
                System.err.println("Saved world dimensions (" + savedWidth + "x" + savedHeight + "x" + savedDepth
                    + ") don't match server (" + width + "x" + height + "x" + depth + "), generating fresh world");
                return false;
            }
            dis.readFully(blocks);
            dirty = false;
            System.out.println("Loaded world from " + SAVE_FILE);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to load world from " + SAVE_FILE + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Migrate invalid block types for RubyDung worlds.
     * RubyDung only supports AIR (0), GRASS (2), and COBBLESTONE (4).
     * The RD client historically sent Stone (1) when placing blocks.
     * This replaces any Stone blocks with Cobblestone.
     */
    public synchronized void migrateRubyDungBlocks() {
        int count = 0;
        for (int i = 0; i < blocks.length; i++) {
            if ((blocks[i] & 0xFF) == BlockRegistry.STONE) {
                blocks[i] = (byte) BlockRegistry.COBBLESTONE;
                count++;
            }
        }
        if (count > 0) {
            dirty = true;
            System.out.println("Migrated " + count + " stone block(s) to cobblestone");
        }
    }

    /**
     * Save world to disk (GZip compressed).
     */
    public synchronized void save() {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(SAVE_FILE)))) {
            dos.writeInt(width);
            dos.writeInt(height);
            dos.writeInt(depth);
            dos.write(blocks);
            dirty = false;
            System.out.println("World saved to " + SAVE_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save world: " + e.getMessage());
        }
    }

    /**
     * Save only if the world has been modified since last save.
     */
    public void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    private int blockIndex(int x, int y, int z) {
        return (y * depth + z) * width + x;
    }

    /**
     * Queue a block change for processing during the next tick.
     */
    public void queueBlockChange(int x, int y, int z, byte blockType) {
        pendingBlockChanges.add(new PendingBlockChange(x, y, z, blockType));
    }

    /**
     * Process all queued block changes. Called by the tick loop.
     * Returns the list of changes that were actually applied (for broadcasting).
     */
    public List<SetBlockServerPacket> processPendingBlockChanges() {
        List<SetBlockServerPacket> applied = new ArrayList<>();
        PendingBlockChange change;
        while ((change = pendingBlockChanges.poll()) != null) {
            if (setBlock(change.x, change.y, change.z, change.blockType)) {
                applied.add(new SetBlockServerPacket(change.x, change.y, change.z, change.blockType));
            }
        }
        return applied;
    }

    /**
     * Remember a player's position in the in-memory cache.
     * Called when a player disconnects so their position survives until the next save.
     */
    public void rememberPlayerPosition(ConnectedPlayer player) {
        playerPositionCache.put(player.getUsername(), new short[]{
            player.getX(), player.getY(), player.getZ(),
            player.getYaw(), player.getPitch()
        });
    }

    /**
     * Save player positions so they can be restored when a player reconnects.
     * Merges the in-memory cache (disconnected players) with currently online players.
     * Format: [int count] then for each player: [UTF name] [short x,y,z] [byte yaw,pitch].
     */
    public void savePlayers(java.util.Collection<ConnectedPlayer> players) {
        // Update cache with current online player positions
        for (ConnectedPlayer p : players) {
            playerPositionCache.put(p.getUsername(), new short[]{
                p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch()
            });
        }

        if (playerPositionCache.isEmpty()) return;

        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(PLAYERS_FILE)))) {
            dos.writeInt(playerPositionCache.size());
            for (java.util.Map.Entry<String, short[]> entry : playerPositionCache.entrySet()) {
                dos.writeUTF(entry.getKey());
                short[] pos = entry.getValue();
                dos.writeShort(pos[0]);
                dos.writeShort(pos[1]);
                dos.writeShort(pos[2]);
                dos.writeByte(pos[3]);
                dos.writeByte(pos[4]);
            }
            System.out.println("Saved " + playerPositionCache.size() + " player position(s) to " + PLAYERS_FILE);
        } catch (IOException e) {
            System.err.println("Failed to save player data: " + e.getMessage());
        }
    }

    /**
     * Load saved player positions into the in-memory cache and return it.
     */
    public java.util.Map<String, short[]> loadPlayerPositions() {
        File file = new File(PLAYERS_FILE);
        if (!file.exists()) return playerPositionCache;

        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            int count = dis.readInt();
            for (int i = 0; i < count; i++) {
                String name = dis.readUTF();
                short x = dis.readShort();
                short y = dis.readShort();
                short z = dis.readShort();
                byte yaw = dis.readByte();
                byte pitch = dis.readByte();
                // putIfAbsent: in-memory positions (from rememberPlayerPosition) take
                // priority over stale file data
                playerPositionCache.putIfAbsent(name, new short[]{x, y, z, yaw, pitch});
            }
            System.out.println("Loaded " + count + " saved player position(s) from " + PLAYERS_FILE);
        } catch (IOException e) {
            System.err.println("Failed to load player data: " + e.getMessage());
        }
        return playerPositionCache;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }

    /** A queued block change waiting to be processed. */
    static class PendingBlockChange {
        final int x, y, z;
        final byte blockType;

        PendingBlockChange(int x, int y, int z, byte blockType) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockType = blockType;
        }
    }
}
