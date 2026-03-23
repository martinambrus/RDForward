package com.github.martinambrus.rdforward.server.bedrock;

import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Converts world data (ServerWorld flat array or AlphaChunk) to Bedrock
 * LevelChunkPacket format with sub-chunk encoding.
 *
 * Bedrock chunks are 16x16 columns divided into 16-block-tall sub-chunks.
 * Alpha world height is 128 blocks = 8 sub-chunks.
 *
 * Sub-chunk format v8 (network/runtime):
 * [version=8][numStorages][paletteHeader][blockIndices][paletteSize][paletteEntries]
 *
 * Palette header byte = (bitsPerBlock << 1) | 1
 * The low bit = 1 indicates runtime/network format.
 * Palette size and entries use unsigned varints.
 *
 * After block sub-chunks, biome data uses the same paletted format
 * (one section per sub-chunk, 4x4x4 biome resolution = 64 entries per section).
 *
 * Performance optimizations applied:
 * 1. Chunk cache: serialized chunk data cached by coordinate, invalidated on block change.
 * 2. Top-down empty section scan: only encodes sub-chunks up to the highest non-empty one.
 * 3. Last-value palette cache: skips HashMap lookups for runs of the same block type.
 * 4. Singleton palette: uniform sections use 0-bpb fast path (no indices written).
 * 5. Buffer pooling: ThreadLocal int[] arrays reused across sub-chunk serializations.
 */
public class BedrockChunkConverter {

    private static final int SUB_CHUNK_HEIGHT = 16;
    private static final int BLOCKS_PER_SUB_CHUNK = 16 * SUB_CHUNK_HEIGHT * 16; // 4096

    /**
     * Overworld min Y is -64. Sub-chunks in LevelChunkPacket start from that Y.
     * Our world data starts at Y=0, which is sub-chunk index 4 (-64/16 = -4, offset = 4).
     */
    private static final int OVERWORLD_Y_OFFSET_CHUNKS = 4;

    /** Number of biome sections for overworld dimension (Y=-64 to Y=319, 24 sub-chunks). */
    private static final int OVERWORLD_SECTIONS = 24;

    /** Total test sub-chunks (offset + 8 data layers for test methods). */
    private static final int TEST_SUB_CHUNKS = OVERWORLD_Y_OFFSET_CHUNKS + 8;

    /** Plains biome runtime ID. */
    private static final int BIOME_PLAINS = 1;

    private final BedrockBlockMapper blockMapper;

    /** Cached air runtime ID (avoids repeated lookup). */
    private final int airRuntimeId;

    /**
     * Cache of serialized chunk data, keyed by encoded (chunkX, chunkZ).
     * Eliminates redundant serialization when the same chunk is requested
     * multiple times (e.g., multiple Bedrock clients, or client reconnection).
     * Invalidated when blocks change via {@link #invalidateCache(int, int)}.
     */
    private final ConcurrentHashMap<Long, CachedChunkData> chunkCache = new ConcurrentHashMap<>();

    /** Max entries in the chunk cache before eviction. */
    private static final int MAX_CACHE_SIZE = 2000;

    /** Tracks cache size without O(n) ConcurrentHashMap.size() calls. */
    private final AtomicInteger cacheSize = new AtomicInteger();

    /**
     * Reusable int arrays for palette building (one per thread).
     * Avoids allocating 2x 4096-element arrays per sub-chunk serialization,
     * saving ~32KB of allocation per chunk conversion (8 sub-chunks).
     */
    private static final ThreadLocal<int[]> TL_INDICES =
            ThreadLocal.withInitial(() -> new int[BLOCKS_PER_SUB_CHUNK]);
    private static final ThreadLocal<int[]> TL_PALETTE =
            ThreadLocal.withInitial(() -> new int[BLOCKS_PER_SUB_CHUNK]);

    public BedrockChunkConverter(BedrockBlockMapper blockMapper) {
        this.blockMapper = blockMapper;
        this.airRuntimeId = blockMapper.toRuntimeId(0);
    }

    /**
     * Invalidate cached chunk data for a given chunk coordinate.
     * Called when a block changes in that chunk.
     */
    public void invalidateCache(int chunkX, int chunkZ) {
        if (chunkCache.remove(cacheKey(chunkX, chunkZ)) != null) {
            cacheSize.decrementAndGet();
        }
    }

    /**
     * Clear the entire chunk cache (e.g., on shutdown or world reset).
     */
    public void clearCache() {
        chunkCache.clear();
        cacheSize.set(0);
    }

    /**
     * Insert into the chunk cache with eviction when full.
     * Evicts ~half of entries (arbitrary order ≈ random eviction).
     */
    private void cacheInsert(long key, CachedChunkData entry) {
        if (cacheSize.get() >= MAX_CACHE_SIZE) {
            int toEvict = MAX_CACHE_SIZE / 2;
            Iterator<Long> it = chunkCache.keySet().iterator();
            while (it.hasNext() && toEvict-- > 0) {
                it.next();
                it.remove();
                cacheSize.decrementAndGet();
            }
        }
        if (chunkCache.putIfAbsent(key, entry) == null) {
            cacheSize.incrementAndGet();
        }
    }

    private static long cacheKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    /**
     * Immutable holder for cached serialized chunk data.
     * Stores the raw bytes and sub-chunk count so a LevelChunkPacket
     * can be reconstructed without re-serialization.
     */
    private static final class CachedChunkData {
        final byte[] data;
        final int subChunksLength;

        CachedChunkData(byte[] data, int subChunksLength) {
            this.data = data;
            this.subChunksLength = subChunksLength;
        }

        LevelChunkPacket toPacket(int chunkX, int chunkZ) {
            LevelChunkPacket packet = new LevelChunkPacket();
            packet.setChunkX(chunkX);
            packet.setChunkZ(chunkZ);
            packet.setSubChunksLength(subChunksLength);
            packet.setCachingEnabled(false);
            packet.setDimension(0);
            packet.setData(Unpooled.wrappedBuffer(data));
            return packet;
        }

        static CachedChunkData fromByteBuf(ByteBuf buf, int subChunksLength) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            return new CachedChunkData(bytes, subChunksLength);
        }
    }

    /**
     * Convert an AlphaChunk to a Bedrock LevelChunkPacket.
     * Returns a cached packet if available, otherwise serializes and caches.
     */
    public LevelChunkPacket convertChunk(AlphaChunk chunk) {
        long key = cacheKey(chunk.getXPos(), chunk.getZPos());
        CachedChunkData cached = chunkCache.get(key);
        if (cached != null) {
            return cached.toPacket(chunk.getXPos(), chunk.getZPos());
        }

        ByteBuf data = ByteBufAllocator.DEFAULT.buffer();
        try {
            int worldSubChunks = (AlphaChunk.HEIGHT + SUB_CHUNK_HEIGHT - 1) / SUB_CHUNK_HEIGHT;

            // Top-down scan: find highest non-empty sub-chunk using heightMap.
            // Avoids encoding empty air sub-chunks above the terrain surface.
            int highestNonEmpty = findHighestNonEmptySubChunk(chunk);
            int actualWorldSubChunks = Math.max(0, highestNonEmpty + 1);
            int totalSubChunks = OVERWORLD_Y_OFFSET_CHUNKS + actualWorldSubChunks;

            // Empty sub-chunks for Y=-64 to Y=-1 (singleton air palette)
            for (int i = 0; i < OVERWORLD_Y_OFFSET_CHUNKS; i++) {
                writeEmptyAirSubChunk(data);
            }

            // World data sub-chunks (only up to highest non-empty)
            for (int subY = 0; subY < actualWorldSubChunks; subY++) {
                writeSubChunk(data, chunk, subY);
            }

            writeBiomeSections(data);
            data.writeByte(0);

            CachedChunkData entry = CachedChunkData.fromByteBuf(data, totalSubChunks);
            cacheInsert(key, entry);
            return entry.toPacket(chunk.getXPos(), chunk.getZPos());
        } finally {
            data.release();
        }
    }

    /**
     * Create a test chunk with all blocks as a single type (for debugging encoding).
     */
    public LevelChunkPacket createTestChunk(int chunkX, int chunkZ, int runtimeId) {
        ByteBuf data = ByteBufAllocator.DEFAULT.buffer();
        try {
            for (int subY = 0; subY < TEST_SUB_CHUNKS; subY++) {
                data.writeByte(8);  // version 8
                data.writeByte(1);  // 1 storage layer
                data.writeByte((0 << 1) | 1);  // bpb=0, runtime format
                VarInts.writeInt(data, runtimeId);
            }

            writeBiomeSections(data);
            data.writeByte(0);

            LevelChunkPacket packet = new LevelChunkPacket();
            packet.setChunkX(chunkX);
            packet.setChunkZ(chunkZ);
            packet.setSubChunksLength(TEST_SUB_CHUNKS);
            packet.setCachingEnabled(false);
            packet.setDimension(0);
            packet.setData(data.retain());
            return packet;
        } finally {
            data.release();
        }
    }

    /**
     * Create a layered test chunk: each sub-chunk gets a single block type (bpb=0).
     * runtimeIds array must have TEST_SUB_CHUNKS (8) entries, one per sub-chunk.
     */
    public LevelChunkPacket createLayeredTestChunk(int chunkX, int chunkZ, int[] runtimeIds) {
        ByteBuf data = ByteBufAllocator.DEFAULT.buffer();
        try {
            for (int subY = 0; subY < TEST_SUB_CHUNKS; subY++) {
                data.writeByte(8);  // version 8
                data.writeByte(1);  // 1 storage layer
                data.writeByte((0 << 1) | 1);  // bpb=0, runtime format
                VarInts.writeInt(data, runtimeIds[subY]);
            }

            writeBiomeSections(data);
            data.writeByte(0);

            LevelChunkPacket packet = new LevelChunkPacket();
            packet.setChunkX(chunkX);
            packet.setChunkZ(chunkZ);
            packet.setSubChunksLength(TEST_SUB_CHUNKS);
            packet.setCachingEnabled(false);
            packet.setDimension(0);
            packet.setData(data.retain());
            return packet;
        } finally {
            data.release();
        }
    }

    /**
     * Convert a column from ServerWorld to a Bedrock LevelChunkPacket.
     * Returns a cached packet if available, otherwise serializes and caches.
     */
    public LevelChunkPacket convertWorldColumn(ServerWorld world, int chunkX, int chunkZ) {
        long key = cacheKey(chunkX, chunkZ);
        CachedChunkData cached = chunkCache.get(key);
        if (cached != null) {
            return cached.toPacket(chunkX, chunkZ);
        }

        ByteBuf data = ByteBufAllocator.DEFAULT.buffer();
        try {
            int baseX = chunkX * 16;
            int baseZ = chunkZ * 16;
            int worldHeight = Math.min(world.getHeight(), 128);

            // World data sub-chunks needed (ceil(worldHeight / 16))
            int worldSubChunks = (worldHeight + SUB_CHUNK_HEIGHT - 1) / SUB_CHUNK_HEIGHT;

            // Top-down scan: find highest non-empty sub-chunk in this column.
            // Avoids encoding empty air sub-chunks above the terrain surface.
            int highestNonEmpty = findHighestNonEmptyWorldSubChunk(
                    world, baseX, baseZ, worldSubChunks, worldHeight);
            int actualWorldSubChunks = Math.max(0, highestNonEmpty + 1);
            // Total sub-chunks = empty offset + world data
            int totalSubChunks = OVERWORLD_Y_OFFSET_CHUNKS + actualWorldSubChunks;

            // First 4 sub-chunks: empty air (Y=-64 to Y=-1)
            for (int i = 0; i < OVERWORLD_Y_OFFSET_CHUNKS; i++) {
                writeEmptyAirSubChunk(data);
            }

            // Remaining sub-chunks: actual world data (only up to highest non-empty)
            for (int subY = 0; subY < actualWorldSubChunks; subY++) {
                writeSubChunkFromWorld(data, world, baseX, baseZ, subY, worldHeight);
            }

            writeBiomeSections(data);
            data.writeByte(0);

            CachedChunkData entry = CachedChunkData.fromByteBuf(data, totalSubChunks);
            cacheInsert(key, entry);
            return entry.toPacket(chunkX, chunkZ);
        } finally {
            data.release();
        }
    }

    /**
     * Write biome palette sections for the full overworld dimension height.
     */
    private void writeBiomeSections(ByteBuf buf) {
        // First section: single-entry palette (0 bpb, runtime format)
        buf.writeByte((0 << 1) | 1);
        VarInts.writeInt(buf, BIOME_PLAINS);

        // Subsequent sections: copy-previous marker (127 << 1 | 1 = 0xFF)
        for (int i = 1; i < OVERWORLD_SECTIONS; i++) {
            buf.writeByte((127 << 1) | 1);
        }
    }

    /**
     * Write a singleton air sub-chunk (0 bpb, just the air runtime ID).
     * Used for empty offset sections and uniform air sections.
     */
    private void writeEmptyAirSubChunk(ByteBuf buf) {
        buf.writeByte(8);  // version 8
        buf.writeByte(1);  // 1 storage layer
        buf.writeByte((0 << 1) | 1);  // bpb=0, runtime format
        VarInts.writeInt(buf, airRuntimeId);
    }

    /**
     * Find the highest non-empty sub-chunk in an AlphaChunk using its heightMap.
     * The heightMap stores the highest non-air Y+1 per XZ column (256 entries).
     * Returns -1 if all sub-chunks are empty (all air).
     */
    private int findHighestNonEmptySubChunk(AlphaChunk chunk) {
        byte[] heightMap = chunk.getHeightMap();
        int maxHeight = 0;
        for (int i = 0; i < heightMap.length; i++) {
            int h = heightMap[i] & 0xFF;
            if (h > maxHeight) maxHeight = h;
        }
        if (maxHeight == 0) return -1;
        return (maxHeight - 1) / SUB_CHUNK_HEIGHT;
    }

    /**
     * Find the highest non-empty sub-chunk in a ServerWorld column.
     * Scans top-down, returning as soon as any non-air block is found.
     * Returns -1 if all sub-chunks are empty.
     */
    private int findHighestNonEmptyWorldSubChunk(ServerWorld world, int baseX, int baseZ,
                                                  int worldSubChunks, int worldHeight) {
        for (int subY = worldSubChunks - 1; subY >= 0; subY--) {
            int baseBlockY = subY * SUB_CHUNK_HEIGHT;
            int topY = Math.min(baseBlockY + SUB_CHUNK_HEIGHT, worldHeight);
            // Scan from top of section downward for early exit
            for (int y = topY - 1; y >= baseBlockY; y--) {
                for (int x = 0; x < 16; x++) {
                    int worldX = baseX + x;
                    if (worldX < 0 || worldX >= world.getWidth()) continue;
                    for (int z = 0; z < 16; z++) {
                        int worldZ = baseZ + z;
                        if (worldZ < 0 || worldZ >= world.getDepth()) continue;
                        if ((world.getBlock(worldX, y, worldZ) & 0xFF) != 0) {
                            return subY;
                        }
                    }
                }
            }
        }
        return -1;
    }

    /** Provides block type at a local (x, y, z) position within a sub-chunk. */
    @FunctionalInterface
    private interface BlockGetter {
        int getBlock(int x, int y, int z);
    }

    /**
     * Write a sub-chunk from AlphaChunk data.
     */
    private void writeSubChunk(ByteBuf buf, AlphaChunk chunk, int subY) {
        int baseBlockY = subY * SUB_CHUNK_HEIGHT;
        buildPaletteAndWrite(buf, subY, (x, y, z) -> {
            int blockY = baseBlockY + y;
            return (blockY < AlphaChunk.HEIGHT) ? chunk.getBlock(x, blockY, z) : 0;
        });
    }

    /**
     * Write a sub-chunk from ServerWorld data.
     */
    private void writeSubChunkFromWorld(ByteBuf buf, ServerWorld world,
                                        int baseX, int baseZ, int subY, int worldHeight) {
        int baseBlockY = subY * SUB_CHUNK_HEIGHT;
        buildPaletteAndWrite(buf, subY, (x, y, z) -> {
            int worldX = baseX + x;
            int worldZ = baseZ + z;
            int worldY = baseBlockY + y;
            if (worldX >= 0 && worldX < world.getWidth()
                    && worldZ >= 0 && worldZ < world.getDepth()
                    && worldY < worldHeight) {
                return world.getBlock(worldX, worldY, worldZ) & 0xFF;
            }
            return 0;
        });
    }

    /**
     * Build palette from block data and write the sub-chunk.
     * Uses last-value cache to skip HashMap lookups for runs of the same block type,
     * which is very common in terrain (runs of stone, dirt, air).
     */
    private void buildPaletteAndWrite(ByteBuf buf, int subY, BlockGetter getter) {
        int[] indices = TL_INDICES.get();
        int[] palette = TL_PALETTE.get();
        int paletteSize = 0;
        Map<Integer, Integer> paletteIndex = new HashMap<>();

        int lastBlockType = -1;
        int lastPaletteIdx = -1;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < SUB_CHUNK_HEIGHT; y++) {
                for (int z = 0; z < 16; z++) {
                    int blockType = getter.getBlock(x, y, z);
                    int idx = (x << 8) | (z << 4) | y; // XZY order

                    if (blockType == lastBlockType) {
                        indices[idx] = lastPaletteIdx;
                    } else {
                        int runtimeId = blockMapper.toRuntimeId(blockType);
                        Integer palIdx = paletteIndex.get(runtimeId);
                        if (palIdx == null) {
                            palIdx = paletteSize;
                            palette[paletteSize] = runtimeId;
                            paletteIndex.put(runtimeId, paletteSize);
                            paletteSize++;
                        }
                        indices[idx] = palIdx;
                        lastBlockType = blockType;
                        lastPaletteIdx = palIdx;
                    }
                }
            }
        }

        if (debugLog) {
            int baseBlockY = subY * SUB_CHUNK_HEIGHT;
            StringBuilder sb = new StringBuilder();
            sb.append("[Bedrock] SubChunk Y=").append(baseBlockY).append(": palette=[");
            for (int i = 0; i < paletteSize; i++) {
                if (i > 0) sb.append(", ");
                sb.append(palette[i]);
            }
            sb.append("] (").append(paletteSize).append(" entries)");
            System.out.println(sb);
        }

        writeSubChunkData(buf, subY, indices, palette, paletteSize);
    }

    /** Enable debug logging for chunk conversion. */
    public void setDebugLog(boolean debug) {
        this.debugLog = debug;
    }
    private boolean debugLog = false;

    /**
     * Write a sub-chunk in Bedrock v8 format (paletted block storage, runtime/network).
     * When paletteSize is 1 (singleton/uniform section), uses 0-bpb fast path:
     * only the palette entry is written, with no block index data at all.
     */
    private void writeSubChunkData(ByteBuf buf, int subY,
                                    int[] blockIndices, int[] palette, int paletteSize) {
        buf.writeByte(8);  // version 8
        buf.writeByte(1);  // 1 storage layer

        int bitsPerBlock = 0;
        if (paletteSize > 1) {
            bitsPerBlock = Integer.SIZE - Integer.numberOfLeadingZeros(paletteSize - 1);
        }
        bitsPerBlock = padBitsPerBlock(bitsPerBlock);

        // Palette header: (bitsPerBlock << 1) | 1 (runtime format)
        buf.writeByte((bitsPerBlock << 1) | 1);

        if (bitsPerBlock == 0) {
            // Single block type: just the entry, no palette size prefix
            VarInts.writeInt(buf, palette[0]);
            return;
        }

        // Write block indices packed into 32-bit words (little-endian)
        int blocksPerWord = 32 / bitsPerBlock;
        int wordsNeeded = (BLOCKS_PER_SUB_CHUNK + blocksPerWord - 1) / blocksPerWord;
        int mask = (1 << bitsPerBlock) - 1;

        for (int w = 0; w < wordsNeeded; w++) {
            int word = 0;
            for (int b = 0; b < blocksPerWord; b++) {
                int blockIdx = w * blocksPerWord + b;
                if (blockIdx < BLOCKS_PER_SUB_CHUNK) {
                    word |= (blockIndices[blockIdx] & mask) << (b * bitsPerBlock);
                }
            }
            buf.writeIntLE(word);
        }

        // Palette size and entries as unsigned varints
        VarInts.writeInt(buf, paletteSize);
        for (int i = 0; i < paletteSize; i++) {
            VarInts.writeInt(buf, palette[i]);
        }
    }

    /**
     * Pad bits-per-block to a Bedrock-supported value.
     * Supported: 1, 2, 3, 4, 5, 6, 8, 16
     */
    private static int padBitsPerBlock(int bpb) {
        if (bpb <= 1) return 1;
        if (bpb <= 2) return 2;
        if (bpb <= 3) return 3;
        if (bpb <= 4) return 4;
        if (bpb <= 5) return 5;
        if (bpb <= 6) return 6;
        if (bpb <= 8) return 8;
        return 16;
    }

}
