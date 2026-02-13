package com.github.martinambrus.rdforward.server.bedrock;

import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket;
import org.cloudburstmc.protocol.common.util.VarInts;

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

    public BedrockChunkConverter(BedrockBlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    /**
     * Convert an AlphaChunk to a Bedrock LevelChunkPacket.
     */
    public LevelChunkPacket convertChunk(AlphaChunk chunk) {
        ByteBuf data = ByteBufAllocator.DEFAULT.buffer();
        try {
            int worldSubChunks = (AlphaChunk.HEIGHT + SUB_CHUNK_HEIGHT - 1) / SUB_CHUNK_HEIGHT;
            int totalSubChunks = OVERWORLD_Y_OFFSET_CHUNKS + worldSubChunks;

            // Empty sub-chunks for Y=-64 to Y=-1
            int airRuntimeId = blockMapper.toRuntimeId(0);
            for (int i = 0; i < OVERWORLD_Y_OFFSET_CHUNKS; i++) {
                data.writeByte(8);
                data.writeByte(1);
                data.writeByte((0 << 1) | 1);
                VarInts.writeInt(data, airRuntimeId);
            }

            // World data sub-chunks
            for (int subY = 0; subY < worldSubChunks; subY++) {
                writeSubChunk(data, chunk, subY);
            }

            writeBiomeSections(data);
            data.writeByte(0);

            LevelChunkPacket packet = new LevelChunkPacket();
            packet.setChunkX(chunk.getXPos());
            packet.setChunkZ(chunk.getZPos());
            packet.setSubChunksLength(totalSubChunks);
            packet.setCachingEnabled(false);
            packet.setDimension(0);
            packet.setData(data.retain());
            return packet;
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
     */
    public LevelChunkPacket convertWorldColumn(ServerWorld world, int chunkX, int chunkZ) {
        ByteBuf data = ByteBufAllocator.DEFAULT.buffer();
        try {
            int baseX = chunkX * 16;
            int baseZ = chunkZ * 16;
            int worldHeight = Math.min(world.getHeight(), 128);

            // World data sub-chunks needed (ceil(worldHeight / 16))
            int worldSubChunks = (worldHeight + SUB_CHUNK_HEIGHT - 1) / SUB_CHUNK_HEIGHT;
            // Total sub-chunks = empty offset + world data
            int totalSubChunks = OVERWORLD_Y_OFFSET_CHUNKS + worldSubChunks;

            // First 4 sub-chunks: empty air (Y=-64 to Y=-1)
            int airRuntimeId = blockMapper.toRuntimeId(0);
            for (int i = 0; i < OVERWORLD_Y_OFFSET_CHUNKS; i++) {
                data.writeByte(8);  // version 8
                data.writeByte(1);  // 1 storage layer
                data.writeByte((0 << 1) | 1);  // bpb=0, runtime format
                VarInts.writeInt(data, airRuntimeId);
            }

            // Remaining sub-chunks: actual world data (Y=0+)
            for (int subY = 0; subY < worldSubChunks; subY++) {
                writeSubChunkFromWorld(data, world, baseX, baseZ, subY, worldHeight);
            }

            writeBiomeSections(data);
            data.writeByte(0);

            LevelChunkPacket packet = new LevelChunkPacket();
            packet.setChunkX(chunkX);
            packet.setChunkZ(chunkZ);
            packet.setSubChunksLength(totalSubChunks);
            packet.setCachingEnabled(false);
            packet.setDimension(0);
            packet.setData(data.retain());
            return packet;
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

    private void writeSubChunk(ByteBuf buf, AlphaChunk chunk, int subY) {
        int baseBlockY = subY * SUB_CHUNK_HEIGHT;

        int[] runtimeIds = new int[BLOCKS_PER_SUB_CHUNK];
        int[] palette = new int[BLOCKS_PER_SUB_CHUNK];
        int paletteSize = 0;
        java.util.Map<Integer, Integer> paletteIndex = new java.util.HashMap<>();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < SUB_CHUNK_HEIGHT; y++) {
                for (int z = 0; z < 16; z++) {
                    int blockY = baseBlockY + y;
                    int blockType = 0;
                    if (blockY < AlphaChunk.HEIGHT) {
                        blockType = chunk.getBlock(x, blockY, z);
                    }
                    int runtimeId = blockMapper.toRuntimeId(blockType);
                    int idx = (x << 8) | (z << 4) | y; // XZY order

                    Integer palIdx = paletteIndex.get(runtimeId);
                    if (palIdx == null) {
                        palIdx = paletteSize;
                        palette[paletteSize] = runtimeId;
                        paletteIndex.put(runtimeId, paletteSize);
                        paletteSize++;
                    }
                    runtimeIds[idx] = palIdx;
                }
            }
        }

        writeSubChunkData(buf, subY, runtimeIds, palette, paletteSize);
    }

    private void writeSubChunkFromWorld(ByteBuf buf, ServerWorld world,
                                        int baseX, int baseZ, int subY, int worldHeight) {
        int baseBlockY = subY * SUB_CHUNK_HEIGHT;

        int[] indices = new int[BLOCKS_PER_SUB_CHUNK];
        int[] palette = new int[BLOCKS_PER_SUB_CHUNK];
        int paletteSize = 0;
        java.util.Map<Integer, Integer> paletteIndex = new java.util.HashMap<>();

        for (int x = 0; x < 16; x++) {
            int worldX = baseX + x;
            for (int y = 0; y < SUB_CHUNK_HEIGHT; y++) {
                int worldY = baseBlockY + y;
                for (int z = 0; z < 16; z++) {
                    int worldZ = baseZ + z;
                    int blockType = 0;
                    if (worldX >= 0 && worldX < world.getWidth()
                            && worldZ >= 0 && worldZ < world.getDepth()
                            && worldY < worldHeight) {
                        blockType = world.getBlock(worldX, worldY, worldZ) & 0xFF;
                    }
                    int runtimeId = blockMapper.toRuntimeId(blockType);
                    int idx = (x << 8) | (z << 4) | y; // XZY order

                    Integer palIdx = paletteIndex.get(runtimeId);
                    if (palIdx == null) {
                        palIdx = paletteSize;
                        palette[paletteSize] = runtimeId;
                        paletteIndex.put(runtimeId, paletteSize);
                        paletteSize++;
                    }
                    indices[idx] = palIdx;
                }
            }
        }

        if (debugLog) {
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
