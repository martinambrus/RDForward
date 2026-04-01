package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.api.ServerProperties;
import com.github.martinambrus.rdforward.world.BlockRegistry;
import com.github.martinambrus.rdforward.world.WorldGenerator;
import com.github.martinambrus.rdforward.world.convert.ServerWorldHeader;

import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
 * Thread safety: uses a {@link ReentrantReadWriteLock} so multiple readers
 * (getBlock, serializeForClassicProtocol, overlay) can proceed concurrently
 * while writes (setBlock, migrateRubyDungBlocks) get exclusive access.
 * Saves snapshot the block array under a read lock and write to disk
 * asynchronously on a dedicated thread, avoiding tick-loop stalls.
 */
public class ServerWorld {

    private static final String SAVE_FILE_NAME = "server-world.dat";
    private static final String PLAYERS_FILE_NAME = "server-players.dat";

    private final int width;
    private final int height;
    private final int depth;
    private final byte[] blocks;
    /**
     * Block ownership IDs, parallel to {@code blocks[]}.
     * 0 = unowned (natural/expired). Positive values are player IDs from
     * {@link com.github.martinambrus.rdforward.server.api.BlockOwnerRegistry}.
     */
    private volatile short[] blockOwnerIds;
    private final File saveFile;
    private final File playersFile;
    private volatile boolean dirty = false;

    /** Read-write lock replacing synchronized for block access. */
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Shared I/O thread for async world and player saves.
     * Set via {@link #setIOThread(ChunkIOThread)} before the tick loop starts.
     * When null (e.g. in unit tests), saves fall back to synchronous execution.
     */
    private volatile ChunkIOThread ioThread;

    // === Day/night cycle and weather ===
    private volatile long worldTime = 6000; // 0=dawn, 6000=noon, 12000=sunset, 18000=midnight
    private volatile boolean timeFrozen = false;
    private volatile WeatherState weather = WeatherState.CLEAR;
    private volatile int weatherDuration = 0; // ticks remaining (0=indefinite)

    public enum WeatherState { CLEAR, RAIN, THUNDER }

    /** Queued block changes from clients, processed during tick loop. */
    private final Queue<PendingBlockChange> pendingBlockChanges = new ConcurrentLinkedQueue<>();

    /** In-memory cache of all known player positions (online + disconnected). */
    private final ConcurrentHashMap<String, short[]> playerPositionCache =
            new ConcurrentHashMap<>();

    /** Set the shared I/O thread for async saves. Call before tick loop starts. */
    public void setIOThread(ChunkIOThread ioThread) {
        this.ioThread = ioThread;
    }

    public ServerWorld(int width, int height, int depth) {
        this(width, height, depth, null);
    }

    public ServerWorld(int width, int height, int depth, File dataDir) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new byte[width * height * depth];
        this.blockOwnerIds = null; // lazy-allocated on first ownership write
        File dir = (dataDir != null) ? dataDir : new File(".");
        this.saveFile = new File(dir, SAVE_FILE_NAME);
        this.playersFile = new File(dir, PLAYERS_FILE_NAME);
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
    public byte getBlock(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return (byte) BlockRegistry.AIR;
        }
        rwLock.readLock().lock();
        try {
            return blocks[blockIndex(x, y, z)];
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Set a block at the given coordinates.
     * Returns true if the block was changed, false if out of bounds or same value.
     */
    public boolean setBlock(int x, int y, int z, byte blockType) {
        if (!inBounds(x, y, z)) {
            return false;
        }
        rwLock.writeLock().lock();
        try {
            int index = blockIndex(x, y, z);
            if (blocks[index] == blockType) {
                return false;
            }
            blocks[index] = blockType;
            dirty = true;
            return true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Get the owner ID of the block at the given coordinates.
     * Returns 0 (unowned) if out of bounds.
     */
    public short getBlockOwnerId(int x, int y, int z) {
        if (!inBounds(x, y, z)) return 0;
        rwLock.readLock().lock();
        try {
            short[] owners = blockOwnerIds;
            if (owners == null) return 0;
            return owners[blockIndex(x, y, z)];
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Set the owner ID of the block at the given coordinates.
     */
    public void setBlockOwnerId(int x, int y, int z, short ownerId) {
        if (!inBounds(x, y, z)) return;
        rwLock.writeLock().lock();
        try {
            if (blockOwnerIds == null) {
                if (ownerId == 0) return; // no-op: clearing an already-unowned block
                blockOwnerIds = new short[width * height * depth];
            }
            blockOwnerIds[blockIndex(x, y, z)] = ownerId;
            dirty = true;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Clear the owner ID of the block at the given coordinates.
     */
    public void clearBlockOwner(int x, int y, int z) {
        setBlockOwnerId(x, y, z, (short) 0);
    }

    /**
     * Copy a rectangular block region into the caller's buffer in a single
     * lock acquisition. Used by {@code ChunkManager.overlayServerWorldBlocks()}
     * to avoid 32,768 individual {@link #getBlock} calls per chunk.
     *
     * @param startX  world X of the region origin
     * @param startZ  world Z of the region origin
     * @param regionWidth  width in X
     * @param regionDepth  depth in Z
     * @param maxY    number of Y layers to copy
     * @param dest    output buffer, filled in order [localX][localZ][y]
     */
    public void getBlockRegion(int startX, int startZ, int regionWidth, int regionDepth,
                               int maxY, byte[] dest) {
        rwLock.readLock().lock();
        try {
            int idx = 0;
            for (int localX = 0; localX < regionWidth; localX++) {
                int worldX = startX + localX;
                for (int localZ = 0; localZ < regionDepth; localZ++) {
                    int worldZ = startZ + localZ;
                    boolean inB = worldX >= 0 && worldX < width
                               && worldZ >= 0 && worldZ < depth;
                    for (int y = 0; y < maxY; y++) {
                        dest[idx++] = inB ? blocks[blockIndex(worldX, y, worldZ)] : 0;
                    }
                }
            }
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Return a snapshot of the entire block array under a single read lock.
     * Callers can read from the snapshot without any locking. The snapshot
     * is a point-in-time copy; subsequent setBlock calls won't affect it.
     */
    public byte[] getBlockSnapshot() {
        rwLock.readLock().lock();
        try {
            return Arrays.copyOf(blocks, blocks.length);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    /**
     * Check if coordinates are within world bounds.
     */
    public final boolean inBounds(int x, int y, int z) {
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
     * Classic v7 expects XZY block ordering (x outermost, y innermost).
     * Classic 0.0.15a's Level.setData swaps Y/Z dimensions, so its getTile
     * reads blocks in the same YZX order as our internal storage — no
     * reordering needed.
     */
    public byte[] serializeForClassicProtocol(ProtocolVersion version) throws IOException {
        int volume = width * height * depth;
        byte[] snapshot = getBlockSnapshot();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos);
             DataOutputStream dos = new DataOutputStream(gzip)) {
            dos.writeInt(volume);
            if (version == ProtocolVersion.CLASSIC_0_0_15A) {
                // 0.0.15a swaps Y/Z in setData, so internal YZX order matches
                dos.write(snapshot);
            } else {
                // Classic v7 expects XZY order — reorder from internal YZX
                for (int x = 0; x < width; x++) {
                    for (int z = 0; z < depth; z++) {
                        for (int y = 0; y < height; y++) {
                            dos.writeByte(snapshot[(y * depth + z) * width + x]);
                        }
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
        if (!saveFile.exists()) {
            // Recover from a crash during save: if the .tmp file exists,
            // the rename was interrupted after the old file was deleted.
            File tmp = new File(saveFile.getPath() + ".tmp");
            if (tmp.exists()) {
                System.out.println("[ServerWorld] Recovering world from interrupted save (" + tmp.getName() + ")...");
                try {
                    Files.move(tmp.toPath(), saveFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException e) {
                    // ATOMIC_MOVE may not be supported; fall back
                    if (!tmp.renameTo(saveFile)) {
                        System.err.println("[ServerWorld] Failed to recover " + tmp + ": " + e.getMessage());
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(saveFile)))) {
            ServerWorldHeader header = ServerWorldHeader.read(dis);
            if (header.width != width || header.height != height || header.depth != depth) {
                System.err.println("Saved world dimensions (" + header.width + "x" + header.height + "x" + header.depth
                    + ") don't match server (" + width + "x" + height + "x" + depth + "), generating fresh world");
                return false;
            }
            dis.readFully(blocks);
            // V2+: read block ownership IDs
            if (header.formatVersion >= ServerWorldHeader.FORMAT_V2_OWNERSHIP) {
                short[] owners = new short[width * height * depth];
                for (int i = 0; i < owners.length; i++) {
                    owners[i] = dis.readShort();
                }
                int ownedCount = 0;
                for (short id : owners) {
                    if (id != 0) ownedCount++;
                }
                // Only keep the array if there are actual owners (lazy allocation)
                if (ownedCount > 0) {
                    blockOwnerIds = owners;
                }
                System.out.println("Loaded " + ownedCount + " owned block(s) from world save");
            }
            dirty = false;
            System.out.println("Loaded world from " + saveFile + " (format version " + header.formatVersion + ")");
            return true;
        } catch (IOException e) {
            System.err.println("Failed to load world from " + saveFile + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Migrate invalid block types for RubyDung worlds.
     * RubyDung only supports AIR (0), GRASS (2), and COBBLESTONE (4).
     * The RD client historically sent Stone (1) when placing blocks.
     * This replaces any Stone blocks with Cobblestone.
     */
    public void migrateRubyDungBlocks() {
        rwLock.writeLock().lock();
        try {
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
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    /**
     * Save world to disk synchronously. Snapshots the block array under a read
     * lock (~1ms for 4MB copy) then writes GZip-compressed data outside any lock.
     * Used at shutdown for a guaranteed-consistent final save.
     */
    public void save() {
        byte[] snapshot;
        short[] ownerSnapshot;
        rwLock.readLock().lock();
        try {
            snapshot = Arrays.copyOf(blocks, blocks.length);
            short[] owners = blockOwnerIds;
            ownerSnapshot = (owners != null)
                    ? Arrays.copyOf(owners, owners.length)
                    : new short[width * height * depth];
            dirty = false;
        } finally {
            rwLock.readLock().unlock();
        }
        writeSnapshot(snapshot, ownerSnapshot);
    }

    /**
     * Save only if the world has been modified since last save (synchronous).
     */
    public void saveIfDirty() {
        if (dirty) {
            save();
        }
    }

    /**
     * Save the world asynchronously if dirty. Snapshots the block array under
     * a read lock (~1ms), then submits the GZip+write to a background thread.
     * The tick loop can continue immediately without waiting for disk I/O.
     */
    public void saveIfDirtyAsync() {
        if (!dirty) return;
        byte[] snapshot;
        short[] ownerSnapshot;
        rwLock.readLock().lock();
        try {
            snapshot = Arrays.copyOf(blocks, blocks.length);
            short[] owners = blockOwnerIds;
            ownerSnapshot = (owners != null)
                    ? Arrays.copyOf(owners, owners.length)
                    : new short[width * height * depth];
            dirty = false;
        } finally {
            rwLock.readLock().unlock();
        }
        ChunkIOThread io = this.ioThread;
        if (io != null) {
            io.submitWrite(() -> writeSnapshot(snapshot, ownerSnapshot));
        } else {
            // Fallback for unit tests or early startup
            writeSnapshot(snapshot, ownerSnapshot);
        }
    }

    /**
     * Write block + ownership snapshots to disk via temp file + atomic rename.
     * V2 format: header + blocks + ownership shorts (big-endian).
     */
    private void writeSnapshot(byte[] snapshot, short[] ownerSnapshot) {
        File tmp = new File(saveFile.getPath() + ".tmp");
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(tmp)))) {
            ServerWorldHeader.write(dos, width, height, depth);
            dos.write(snapshot);
            // V2: write ownership IDs as big-endian shorts
            for (short id : ownerSnapshot) {
                dos.writeShort(id);
            }
        } catch (IOException e) {
            dirty = true; // retry on next cycle
            tmp.delete();
            System.err.println("Failed to save world: " + e.getMessage());
            return;
        }
        try {
            Files.move(tmp.toPath(), saveFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // ATOMIC_MOVE not supported (e.g. cross-filesystem); try REPLACE_EXISTING alone
            try {
                Files.move(tmp.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                dirty = true;
                System.err.println("Failed to rename world save " + tmp + " -> " + saveFile + ": " + e2.getMessage());
                return;
            }
        }
        System.out.println("World saved to " + saveFile);
    }

    /**
     * Save player positions asynchronously. Gathers position data on the
     * calling thread, then submits GZip+write to the background save thread.
     */
    public void savePlayersAsync(Collection<ConnectedPlayer> players) {
        gatherPlayerPositions(players);
        if (playerPositionCache.isEmpty()) return;
        Map<String, short[]> snapshot = new HashMap<>(playerPositionCache);
        ChunkIOThread io = this.ioThread;
        if (io != null) {
            io.submitWrite(() -> writePlayerSnapshot(snapshot));
        } else {
            writePlayerSnapshot(snapshot);
        }
    }

    /** Write player position snapshot to disk via temp file + atomic rename. */
    private void writePlayerSnapshot(Map<String, short[]> snapshot) {
        File tmp = new File(playersFile.getPath() + ".tmp");
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream(tmp)))) {
            dos.writeInt(snapshot.size());
            for (Map.Entry<String, short[]> entry : snapshot.entrySet()) {
                dos.writeUTF(entry.getKey());
                short[] pos = entry.getValue();
                dos.writeShort(pos[0]);
                dos.writeShort(pos[1]);
                dos.writeShort(pos[2]);
                dos.writeByte(pos[3]);
                dos.writeByte(pos[4]);
            }
        } catch (IOException e) {
            tmp.delete();
            System.err.println("Failed to save player data: " + e.getMessage());
            return;
        }
        if (!tmp.renameTo(playersFile)) {
            playersFile.delete();
            if (!tmp.renameTo(playersFile)) {
                System.err.println("Failed to rename player save " + tmp + " -> " + playersFile);
                return;
            }
        }
        System.out.println("Saved " + snapshot.size() + " player position(s) to " + playersFile);
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
     * Acquires the write lock once for the entire batch to avoid per-block
     * lock overhead.
     */
    public List<SetBlockServerPacket> processPendingBlockChanges() {
        // Drain the queue first (lock-free)
        List<PendingBlockChange> batch = new ArrayList<>();
        PendingBlockChange change;
        while ((change = pendingBlockChanges.poll()) != null) {
            batch.add(change);
        }
        if (batch.isEmpty()) return List.of();

        // Apply all changes under a single write lock
        List<SetBlockServerPacket> applied = new ArrayList<>();
        rwLock.writeLock().lock();
        try {
            for (PendingBlockChange c : batch) {
                if (!inBounds(c.x, c.y, c.z)) continue;
                int index = blockIndex(c.x, c.y, c.z);
                if (blocks[index] == c.blockType) continue;
                blocks[index] = c.blockType;
                dirty = true;
                applied.add(new SetBlockServerPacket(c.x, c.y, c.z, c.blockType));
            }
        } finally {
            rwLock.writeLock().unlock();
        }
        return applied;
    }

    /**
     * Remember a player's position in the in-memory cache.
     * Called when a player disconnects so their position survives until the next save.
     */
    public void rememberPlayerPosition(ConnectedPlayer player) {
        gatherPlayerPositions(Collections.singletonList(player));
    }

    /**
     * Update the in-memory position cache from a collection of online players.
     * Uses double-precision with rounding for Alpha/Bedrock clients to avoid
     * fixed-point truncation that shifts Y downward on restore.
     * Classic/RubyDung clients only use short fields (doubleX/Y/Z stay 0.0),
     * so fall back to the short coordinates for those clients.
     */
    private void gatherPlayerPositions(Collection<ConnectedPlayer> players) {
        boolean online = ServerProperties.isOnlineMode();
        for (ConnectedPlayer p : players) {
            short fx, fy, fz;
            if (p.getDoubleX() != 0 || p.getDoubleY() != 0 || p.getDoubleZ() != 0) {
                fx = (short) Math.round(p.getDoubleX() * 32);
                fy = (short) Math.round(p.getDoubleY() * 32);
                fz = (short) Math.round(p.getDoubleZ() * 32);
            } else {
                fx = p.getX(); fy = p.getY(); fz = p.getZ();
            }
            if (DebugLog.pos() && DebugLog.forPlayer(p.getUsername())) {
                DebugLog.log(DebugLog.POS, "save " + p.getUsername()
                        + " d=(" + String.format("%.2f,%.2f,%.2f", p.getDoubleX(), p.getDoubleY(), p.getDoubleZ()) + ")"
                        + " f=(" + fx + "," + fy + "," + fz + ")");
            }
            // In online mode, use UUID as save key; in offline mode, use username
            String key = (online && p.getUuid() != null) ? p.getUuid() : p.getUsername();
            playerPositionCache.put(key, new short[]{
                fx, fy, fz, p.getYaw(), p.getPitch()
            });
        }
    }

    /**
     * Remove a player's saved position from the in-memory cache.
     * Used by E2E tests and when a player needs a fresh spawn position.
     */
    public void forgetPlayerPosition(String username) {
        playerPositionCache.remove(username);
    }

    public void savePlayerPosition(String username) {
        int cx = getSpawnX();
        int cz = getSpawnZ();
        int spawnY = height * 2 / 3 + 1;
        int[] safe = findSafePosition(cx, spawnY, cz, 50);
        short fx = (short) Math.round((safe[0] + 0.5) * 32);
        short fy = (short) Math.round((safe[1] + (double) 1.62f) * 32);
        short fz = (short) Math.round((safe[2] + 0.5) * 32);
        playerPositionCache.put(username, new short[]{fx, fy, fz, 0, 0});
    }

    /**
     * Save player positions so they can be restored when a player reconnects.
     * Merges the in-memory cache (disconnected players) with currently online players.
     * Format: [int count] then for each player: [UTF name] [short x,y,z] [byte yaw,pitch].
     */
    public void savePlayers(Collection<ConnectedPlayer> players) {
        gatherPlayerPositions(players);
        if (playerPositionCache.isEmpty()) return;
        writePlayerSnapshot(new HashMap<>(playerPositionCache));
    }

    private volatile boolean playersLoadedFromDisk = false;

    /**
     * Load saved player positions into the in-memory cache and return it.
     * Only reads from disk once; subsequent calls return the cache directly.
     */
    public Map<String, short[]> loadPlayerPositions() {
        if (playersLoadedFromDisk) return playerPositionCache;
        return loadPlayerPositionsFromDisk();
    }

    private synchronized Map<String, short[]> loadPlayerPositionsFromDisk() {
        if (playersLoadedFromDisk) return playerPositionCache;
        if (!playersFile.exists()) {
            playersLoadedFromDisk = true;
            return playerPositionCache;
        }

        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(playersFile)))) {
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
            System.out.println("Loaded " + count + " saved player position(s) from " + playersFile);
        } catch (IOException e) {
            System.err.println("Failed to load player data: " + e.getMessage());
        }
        playersLoadedFromDisk = true;
        return playerPositionCache;
    }

    /**
     * Look up a saved player position by username or UUID, depending on online mode.
     * In online mode, tries UUID first, then falls back to username (handles
     * transition from offline to online mode). In offline mode, tries username
     * first, then falls back to UUID.
     * Returns null if no saved position exists.
     */
    public short[] getSavedPlayerPosition(String username, String uuid) {
        if (username == null && uuid == null) return null;
        Map<String, short[]> saved = loadPlayerPositions();
        String trimmedName = (username != null) ? username.trim() : null;
        short[] result;
        if (ServerProperties.isOnlineMode() && uuid != null) {
            result = saved.get(uuid);
            if (result == null && trimmedName != null) result = saved.get(trimmedName);
        } else if (trimmedName != null) {
            result = saved.get(trimmedName);
            if (result == null && uuid != null) result = saved.get(uuid);
        } else {
            result = saved.get(uuid);
        }
        if (DebugLog.pos() && username != null && DebugLog.forPlayer(username)) {
            DebugLog.log(DebugLog.POS, "restore " + username
                    + (result != null ? " f=(" + result[0] + "," + result[1] + "," + result[2] + ")" : " (no saved pos)"));
        }
        return result;
    }

    /**
     * Find a safe position near the given coordinates by scanning upward.
     * A safe position requires: solid block below (ground), 2 air blocks above (feet + head).
     * Searches upward from the player's Y first, then expands horizontally.
     * This keeps players near their original position (e.g. in a cave) rather
     * than teleporting them to the surface.
     *
     * Takes a snapshot of the block array under a single read lock, then
     * performs all searching lock-free on the snapshot. This replaces up to
     * millions of individual lock acquire/release cycles with one ~1ms copy.
     *
     * @param x block X
     * @param y block Y (feet position)
     * @param z block Z
     * @param maxRadius horizontal search radius in blocks
     * @return int[3] with safe {x, feetY, z}
     */
    public int[] findSafePosition(int x, int y, int z, int maxRadius) {
        byte[] snapshot = getBlockSnapshot();
        int startY = Math.max(1, Math.min(y, height - 2));

        // First: search straight up from current position
        int safeY = findSafeYUpward(snapshot, x, z, startY);
        if (safeY >= 0) {
            return new int[]{x, safeY, z};
        }

        // Expand horizontally in square rings, searching upward at each column
        for (int r = 1; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue;
                    int tx = x + dx;
                    int tz = z + dz;
                    if (tx < 0 || tx >= width || tz < 0 || tz >= depth) continue;
                    safeY = findSafeYUpward(snapshot, tx, tz, startY);
                    if (safeY >= 0) {
                        return new int[]{tx, safeY, tz};
                    }
                }
            }
        }

        // Fallback: world center surface (top-down scan)
        int cx = width / 2;
        int cz = depth / 2;
        for (int testY = height - 2; testY >= 1; testY--) {
            if (getBlockFromSnapshot(snapshot, cx, testY - 1, cz) != 0
                    && getBlockFromSnapshot(snapshot, cx, testY, cz) == 0
                    && getBlockFromSnapshot(snapshot, cx, testY + 1, cz) == 0) {
                return new int[]{cx, testY, cz};
            }
        }
        return new int[]{cx, height * 2 / 3 + 1, cz};
    }

    /** Read a block from a snapshot array without any locking. */
    private byte getBlockFromSnapshot(byte[] snapshot, int x, int y, int z) {
        if (!inBounds(x, y, z)) return (byte) BlockRegistry.AIR;
        return snapshot[blockIndex(x, y, z)];
    }

    /**
     * Search upward from startY for a safe position using a snapshot array.
     * Solid ground below and 2 air blocks for feet and head. Narrow pits
     * (1-wide columns) are valid — the player may have dug them intentionally.
     * Returns the feet Y, or -1 if none found.
     */
    private int findSafeYUpward(byte[] snapshot, int x, int z, int startY) {
        for (int testY = startY; testY < height - 1; testY++) {
            if (getBlockFromSnapshot(snapshot, x, testY - 1, z) != 0       // solid ground below
                    && getBlockFromSnapshot(snapshot, x, testY, z) == 0     // feet in air
                    && getBlockFromSnapshot(snapshot, x, testY + 1, z) == 0) { // head in air
                return testY;
            }
        }
        return -1;
    }

    // === Time and weather methods ===

    /**
     * Advance world time by 1 tick (if not frozen) and decrement weather duration.
     * Called once per server tick.
     */
    public void tickTime() {
        if (!timeFrozen) {
            worldTime++;
        }
        if (weatherDuration > 0) {
            weatherDuration--;
            if (weatherDuration == 0) {
                weather = WeatherState.CLEAR;
            }
        }
    }

    public long getWorldTime() { return worldTime; }

    public void setWorldTime(long time) { this.worldTime = time; }

    public boolean isTimeFrozen() { return timeFrozen; }

    public void setTimeFrozen(boolean frozen) { this.timeFrozen = frozen; }

    public WeatherState getWeather() { return weather; }

    public void setWeather(WeatherState state, int durationTicks) {
        this.weather = state;
        this.weatherDuration = durationTicks;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDepth() { return depth; }

    /** Spawn X coordinate (chunk-aligned world center). */
    public int getSpawnX() { return ((width / 2) >> 4) * 16 + 8; }

    /** Spawn Z coordinate (chunk-aligned world center). */
    public int getSpawnZ() { return ((depth / 2) >> 4) * 16 + 8; }

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
