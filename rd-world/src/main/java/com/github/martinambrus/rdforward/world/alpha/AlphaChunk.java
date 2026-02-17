package com.github.martinambrus.rdforward.world.alpha;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Represents a single chunk in the Minecraft Alpha level format.
 *
 * Alpha chunks are 16 blocks wide (X) x 128 blocks tall (Y) x 16 blocks deep (Z).
 * Total: 32,768 blocks per chunk.
 *
 * Arrays use YZX ordering: index = y + (z * 128) + (x * 128 * 16)
 * This means blocks in the same vertical column are stored contiguously.
 */
public class AlphaChunk {

    public static final int WIDTH = 16;
    public static final int HEIGHT = 128;
    public static final int DEPTH = 16;
    public static final int BLOCK_COUNT = WIDTH * HEIGHT * DEPTH; // 32768
    public static final int NIBBLE_COUNT = BLOCK_COUNT / 2;       // 16384

    private final int xPos;
    private final int zPos;

    /** Block IDs, 1 byte per block, 32768 bytes total */
    private final byte[] blocks;

    /** Block metadata, 4 bits per block (nibble array), 16384 bytes total */
    private final byte[] data;

    /** Block-emitted light, 4 bits per block, 16384 bytes total */
    private final byte[] blockLight;

    /** Sky light, 4 bits per block, 16384 bytes total */
    private final byte[] skyLight;

    /** Highest non-air Y per XZ column, 256 bytes (16x16) */
    private final byte[] heightMap;

    /** Whether terrain features (trees, ores, etc.) have been generated */
    private boolean terrainPopulated;

    /** Tick when this chunk was last saved */
    private long lastUpdate;

    /** Entities within this chunk (mobs, items, projectiles, etc.) */
    private final List<AlphaEntity> entities;

    /** Tile entities (block entities) within this chunk (chests, furnaces, signs, etc.) */
    private final List<AlphaTileEntity> tileEntities;

    public AlphaChunk(int xPos, int zPos) {
        this.xPos = xPos;
        this.zPos = zPos;
        this.blocks = new byte[BLOCK_COUNT];
        this.data = new byte[NIBBLE_COUNT];
        this.blockLight = new byte[NIBBLE_COUNT];
        this.skyLight = new byte[NIBBLE_COUNT];
        this.heightMap = new byte[WIDTH * DEPTH];
        this.entities = new ArrayList<AlphaEntity>();
        this.tileEntities = new ArrayList<AlphaTileEntity>();
        this.terrainPopulated = false;
        this.lastUpdate = 0;

        // Sky light starts at zero; call generateSkylightMap() after all
        // blocks are placed to compute correct values from the height map.
    }

    /**
     * Calculate the array index for a block at local coordinates (x, y, z).
     * Uses YZX ordering as per the Alpha format spec.
     */
    public static int blockIndex(int x, int y, int z) {
        return y + (z * HEIGHT) + (x * HEIGHT * DEPTH);
    }

    /**
     * Get the block ID at local coordinates (x, y, z).
     */
    public int getBlock(int x, int y, int z) {
        return blocks[blockIndex(x, y, z)] & 0xFF;
    }

    /**
     * Set the block ID at local coordinates (x, y, z).
     */
    public void setBlock(int x, int y, int z, int blockId) {
        blocks[blockIndex(x, y, z)] = (byte) blockId;
        updateHeightMap(x, y, z, blockId);
    }

    /**
     * Get the 4-bit metadata for a block at local coordinates.
     */
    public int getBlockData(int x, int y, int z) {
        return getNibble(data, blockIndex(x, y, z));
    }

    /**
     * Set the 4-bit metadata for a block at local coordinates.
     */
    public void setBlockData(int x, int y, int z, int value) {
        setNibble(data, blockIndex(x, y, z), value);
    }

    /**
     * Get a nibble value (4 bits) from a nibble array at the given block index.
     */
    private static int getNibble(byte[] array, int blockIndex) {
        int byteIndex = blockIndex / 2;
        if ((blockIndex & 1) == 0) {
            return array[byteIndex] & 0x0F;
        } else {
            return (array[byteIndex] >> 4) & 0x0F;
        }
    }

    /**
     * Set a nibble value (4 bits) in a nibble array at the given block index.
     */
    private static void setNibble(byte[] array, int blockIndex, int value) {
        int byteIndex = blockIndex / 2;
        if ((blockIndex & 1) == 0) {
            array[byteIndex] = (byte) ((array[byteIndex] & 0xF0) | (value & 0x0F));
        } else {
            array[byteIndex] = (byte) ((array[byteIndex] & 0x0F) | ((value & 0x0F) << 4));
        }
    }

    /**
     * Update the height map when a block changes.
     */
    private void updateHeightMap(int x, int y, int z, int blockId) {
        int hmIndex = z + (x * DEPTH);
        int currentHeight = heightMap[hmIndex] & 0xFF;
        if (blockId != 0 && y >= currentHeight) {
            heightMap[hmIndex] = (byte) (y + 1);
        } else if (blockId == 0 && y == currentHeight - 1) {
            // Recalculate height for this column
            for (int scanY = y - 1; scanY >= 0; scanY--) {
                if (getBlock(x, scanY, z) != 0) {
                    heightMap[hmIndex] = (byte) (scanY + 1);
                    return;
                }
            }
            heightMap[hmIndex] = 0;
        }
    }

    /**
     * Compute skylight using column sweep + BFS flood-fill propagation.
     *
     * Phase 1: Direct sky access — blocks above the height map get light 15.
     * Phase 2: BFS flood-fill — propagates sky light laterally and downward
     * through transparent blocks (air, glass, leaves, etc.), decreasing by
     * each block's light opacity (minimum 1 per step).
     *
     * Must be called after all block data is finalized (e.g. after world
     * overlay) and before the chunk is serialized or sent to clients.
     * Without correct skylight, the Alpha client's light engine cascade-corrects
     * on any block change, leading to a StackOverflowError.
     *
     * Note: propagation is limited to within this chunk. Light does not
     * cross chunk boundaries.
     */
    public void generateSkylightMap() {
        Arrays.fill(skyLight, (byte) 0);

        // Phase 1: Column sweep — direct sky access gets light 15
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                int height = heightMap[z + (x * DEPTH)] & 0xFF;
                for (int y = height; y < HEIGHT; y++) {
                    setNibble(skyLight, blockIndex(x, y, z), 15);
                }
            }
        }

        // Phase 2: BFS flood-fill from sky boundary into underground
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        // Seed: sky-lit blocks whose neighbors include underground transparent blocks.
        // For each column, the sky-lit range is [height, HEIGHT). We only need to seed
        // blocks in that range up to the maximum adjacent column height, since those
        // are the only sky-lit blocks adjacent to underground blocks in other columns.
        // Also seed y=height if the block directly below (y=height-1) is transparent
        // (e.g. leaves, water) — light can enter from above through the height map block.
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < DEPTH; z++) {
                int height = heightMap[z + (x * DEPTH)] & 0xFF;
                if (height >= HEIGHT) continue; // Fully solid column, no sky-lit blocks

                // Find the tallest adjacent column — sky-lit blocks up to that height
                // border underground blocks in the taller column
                int maxAdjacentHeight = height;
                if (x > 0)
                    maxAdjacentHeight = Math.max(maxAdjacentHeight, heightMap[z + ((x - 1) * DEPTH)] & 0xFF);
                if (x < WIDTH - 1)
                    maxAdjacentHeight = Math.max(maxAdjacentHeight, heightMap[z + ((x + 1) * DEPTH)] & 0xFF);
                if (z > 0)
                    maxAdjacentHeight = Math.max(maxAdjacentHeight, heightMap[(z - 1) + (x * DEPTH)] & 0xFF);
                if (z < DEPTH - 1)
                    maxAdjacentHeight = Math.max(maxAdjacentHeight, heightMap[(z + 1) + (x * DEPTH)] & 0xFF);

                // Seed the block directly below the sky column if it's transparent
                // (handles light entering through leaves/water at the surface)
                if (height > 0 && getLightOpacity(getBlock(x, height - 1, z)) < 15) {
                    int newLight = 15 - Math.max(1, getLightOpacity(getBlock(x, height - 1, z)));
                    if (newLight > 0) {
                        setNibble(skyLight, blockIndex(x, height - 1, z), newLight);
                        queue.add(packCoord(x, height - 1, z));
                    }
                }

                // Seed sky-lit blocks in the range that borders taller adjacent columns
                for (int y = height; y < Math.min(maxAdjacentHeight, HEIGHT); y++) {
                    queue.add(packCoord(x, y, z));
                }
            }
        }

        // BFS: spread light to transparent neighbors, reducing by opacity (min 1)
        while (!queue.isEmpty()) {
            int packed = queue.poll();
            int x = (packed >> 11) & 0xF;
            int y = (packed >> 4) & 0x7F;
            int z = packed & 0xF;
            int light = getNibble(skyLight, blockIndex(x, y, z));
            if (light <= 1) continue;

            spreadSkyLight(queue, x - 1, y, z, light);
            spreadSkyLight(queue, x + 1, y, z, light);
            spreadSkyLight(queue, x, y - 1, z, light);
            spreadSkyLight(queue, x, y + 1, z, light);
            spreadSkyLight(queue, x, y, z - 1, light);
            spreadSkyLight(queue, x, y, z + 1, light);
        }
    }

    /**
     * Try to spread sky light into a neighboring block. If the neighbor is
     * within bounds, not fully opaque, and would receive more light than it
     * currently has, update it and enqueue for further propagation.
     */
    private void spreadSkyLight(ArrayDeque<Integer> queue, int x, int y, int z, int sourceLight) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT || z < 0 || z >= DEPTH) return;

        int opacity = getLightOpacity(getBlock(x, y, z));
        if (opacity >= 15) return; // Fully opaque

        int newLight = sourceLight - Math.max(1, opacity);
        if (newLight <= 0) return;

        int idx = blockIndex(x, y, z);
        if (getNibble(skyLight, idx) >= newLight) return; // Already at least as bright

        setNibble(skyLight, idx, newLight);
        queue.add(packCoord(x, y, z));
    }

    /** Pack local chunk coordinates into a single int (x: 4 bits, y: 7 bits, z: 4 bits). */
    private static int packCoord(int x, int y, int z) {
        return (x << 11) | (y << 4) | z;
    }

    /**
     * Get the sky-light opacity of a block type. Opaque blocks return 15
     * (block all light). Transparent blocks return 0. Some blocks like
     * water (3) and leaves (1) partially reduce light.
     */
    static int getLightOpacity(int blockId) {
        switch (blockId) {
            case 0:  // Air
            case 6:  // Sapling
            case 20: // Glass
            case 37: // Dandelion
            case 38: // Rose
            case 39: // Brown mushroom
            case 40: // Red mushroom
            case 50: // Torch
            case 51: // Fire
            case 55: // Redstone wire
            case 59: // Wheat
            case 63: // Sign post
            case 65: // Ladder
            case 66: // Rail
            case 68: // Wall sign
            case 69: // Lever
            case 70: // Stone pressure plate
            case 72: // Wooden pressure plate
            case 75: // Redstone torch (off)
            case 76: // Redstone torch (on)
            case 77: // Button
            case 79: // Ice
            case 83: // Sugar cane
            case 85: // Fence
            case 90: // Portal
                return 0;
            case 8: case 9: // Water
                return 3;
            case 18: // Leaves
                return 1;
            default:
                return 15; // Fully opaque
        }
    }

    /**
     * Serialize this chunk's data for the Alpha protocol MapChunkPacket (0x33).
     *
     * The compressed payload contains (in order):
     *   1. Block IDs — 32768 bytes (1 byte per block)
     *   2. Block metadata — 16384 bytes (nibble array, 4 bits per block)
     *   3. Block light — 16384 bytes (nibble array)
     *   4. Sky light — 16384 bytes (nibble array)
     * Total uncompressed: 81920 bytes, then zlib/deflate compressed.
     *
     * The block ordering matches the internal YZX storage, so the raw
     * arrays can be written directly without reordering.
     *
     * @return zlib-compressed chunk data suitable for MapChunkPacket
     */
    public byte[] serializeForAlphaProtocol() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_SPEED));
        dos.write(blocks);
        dos.write(data);
        dos.write(blockLight);
        dos.write(skyLight);
        dos.close();
        return baos.toByteArray();
    }

    /**
     * Serialize this chunk's data for the Release 1.2.1+ protocol (v28) MapChunkPacket.
     *
     * The v28 format uses section-based encoding with 16x16x16 sub-chunks.
     * Our 128-tall chunk maps to sections 0-7. Each section's blocks use YZX
     * ordering: index = (y & 15) * 256 + z * 16 + x.
     *
     * Uncompressed data layout (for each section in primaryBitMask):
     *   4096 bytes block type LSBs
     * Then for each section in primaryBitMask:
     *   2048 bytes metadata nibbles
     * Then for each section in primaryBitMask:
     *   2048 bytes block light nibbles
     * Then for each section in primaryBitMask:
     *   2048 bytes sky light nibbles
     * If groundUpContinuous: 256 bytes biome data (all plains = 1)
     *
     * @return result array: [0] = compressed data, [1..2] = primaryBitMask as
     *         two bytes (low, high). Use {@code (result[1][0] & 0xFF) | ((result[1][1] & 0xFF) << 8)}
     *         to reconstruct the short.
     */
    public V28ChunkData serializeForV28Protocol() throws IOException {
        // Determine which sections (0-7) contain non-air blocks.
        // Our chunk is 128 tall, so sections 0-7 (each 16 blocks tall).
        int primaryBitMask = 0;
        for (int section = 0; section < 8; section++) {
            int baseY = section * 16;
            boolean hasBlocks = false;
            for (int x = 0; x < WIDTH && !hasBlocks; x++) {
                for (int z = 0; z < DEPTH && !hasBlocks; z++) {
                    for (int ly = 0; ly < 16; ly++) {
                        if (getBlock(x, baseY + ly, z) != 0) {
                            hasBlocks = true;
                        }
                    }
                }
            }
            if (hasBlocks) {
                primaryBitMask |= (1 << section);
            }
        }

        // Count sections for buffer sizing
        int sectionCount = Integer.bitCount(primaryBitMask);

        // Each section contributes: 4096 (blocks) + 2048 (metadata) + 2048 (blockLight) + 2048 (skyLight)
        // Plus 256 bytes biome data for ground-up continuous
        int uncompressedSize = sectionCount * (4096 + 2048 + 2048 + 2048) + 256;
        byte[] uncompressed = new byte[uncompressedSize];
        int offset = 0;

        // Write block type LSBs for each section
        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x++) {
                        uncompressed[offset++] = (byte) getBlock(x, baseY + ly, z);
                    }
                }
            }
        }

        // Write metadata nibbles for each section
        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x += 2) {
                        int low = getBlockData(x, baseY + ly, z) & 0x0F;
                        int high = getBlockData(x + 1, baseY + ly, z) & 0x0F;
                        uncompressed[offset++] = (byte) (low | (high << 4));
                    }
                }
            }
        }

        // Write block light nibbles for each section
        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x += 2) {
                        int low = getBlockLight(x, baseY + ly, z) & 0x0F;
                        int high = getBlockLight(x + 1, baseY + ly, z) & 0x0F;
                        uncompressed[offset++] = (byte) (low | (high << 4));
                    }
                }
            }
        }

        // Write sky light nibbles for each section
        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x += 2) {
                        int low = getSkyLight(x, baseY + ly, z) & 0x0F;
                        int high = getSkyLight(x + 1, baseY + ly, z) & 0x0F;
                        uncompressed[offset++] = (byte) (low | (high << 4));
                    }
                }
            }
        }

        // addBitMask = 0, so no add data sections

        // Biome data: 256 bytes, all plains (1)
        Arrays.fill(uncompressed, offset, offset + 256, (byte) 1);
        offset += 256;

        // Zlib compress
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(Deflater.BEST_SPEED));
        dos.write(uncompressed, 0, offset);
        dos.close();

        return new V28ChunkData(baos.toByteArray(), (short) primaryBitMask);
    }

    /**
     * Serialize this chunk's data for the 1.8 (v47) MapChunkPacket.
     *
     * The v47 format uses section-based encoding with combined ushort blockStates.
     * Per section (in order):
     *   8192 bytes: ushort[4096] blockStates (little-endian, blockId << 4 | meta)
     *   2048 bytes: block light nibbles
     *   2048 bytes: sky light nibbles
     * After all sections: 256 bytes biome data (all plains = 1)
     * No zlib compression — the VarInt frame layer handles packet-level compression.
     *
     * @return V47ChunkData with raw (uncompressed) data and primaryBitMask
     */
    public V47ChunkData serializeForV47Protocol() {
        // Determine which sections (0-7) contain non-air blocks.
        int primaryBitMask = 0;
        for (int section = 0; section < 8; section++) {
            int baseY = section * 16;
            boolean hasBlocks = false;
            for (int x = 0; x < WIDTH && !hasBlocks; x++) {
                for (int z = 0; z < DEPTH && !hasBlocks; z++) {
                    for (int ly = 0; ly < 16; ly++) {
                        if (getBlock(x, baseY + ly, z) != 0) {
                            hasBlocks = true;
                        }
                    }
                }
            }
            if (hasBlocks) {
                primaryBitMask |= (1 << section);
            }
        }

        int sectionCount = Integer.bitCount(primaryBitMask);
        // Per section: 8192 (blockStates) + 2048 (blockLight) + 2048 (skyLight) = 12288
        // Plus 256 bytes biome data
        int dataSize = sectionCount * 12288 + 256;
        byte[] rawData = new byte[dataSize];
        int offset = 0;

        // 1.8 client reads: ALL block data, THEN all block light, THEN all sky light
        // (three separate passes over sections, NOT interleaved per section).

        // Pass 1: Block states for all sections
        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;
            // ushort[4096] in little-endian, index = (y*16+z)*16+x
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x++) {
                        int blockId = getBlock(x, baseY + ly, z);
                        int meta = getBlockData(x, baseY + ly, z);
                        int blockState = (blockId << 4) | (meta & 0x0F);
                        rawData[offset++] = (byte) (blockState & 0xFF);
                        rawData[offset++] = (byte) ((blockState >> 8) & 0xFF);
                    }
                }
            }
        }

        // Pass 2: Block light nibbles for all sections
        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x += 2) {
                        int low = getBlockLight(x, baseY + ly, z) & 0x0F;
                        int high = getBlockLight(x + 1, baseY + ly, z) & 0x0F;
                        rawData[offset++] = (byte) (low | (high << 4));
                    }
                }
            }
        }

        // Pass 3: Sky light nibbles for all sections
        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x += 2) {
                        int low = getSkyLight(x, baseY + ly, z) & 0x0F;
                        int high = getSkyLight(x + 1, baseY + ly, z) & 0x0F;
                        rawData[offset++] = (byte) (low | (high << 4));
                    }
                }
            }
        }

        // Biome data: 256 bytes, all plains (1)
        Arrays.fill(rawData, offset, offset + 256, (byte) 1);

        return new V47ChunkData(rawData, (short) primaryBitMask);
    }

    /**
     * Result container for v47 chunk serialization.
     */
    public static class V47ChunkData {
        private final byte[] rawData;
        private final short primaryBitMask;

        public V47ChunkData(byte[] rawData, short primaryBitMask) {
            this.rawData = rawData;
            this.primaryBitMask = primaryBitMask;
        }

        public byte[] getRawData() { return rawData; }
        public short getPrimaryBitMask() { return primaryBitMask; }
    }

    /**
     * Serialize this chunk's data for the 1.9 (v109) MapChunkPacket.
     *
     * The v109 format uses paletted section encoding. Each section contains:
     *   1. byte bitsPerBlock (min 4, = ceil(log2(uniqueBlockStates)))
     *   2. VarInt paletteLength + VarInt[] palette (global block state IDs)
     *   3. VarInt dataArrayLength + long[] dataArray (packed palette indices)
     *   4. byte[2048] blockLight nibbles
     *   5. byte[2048] skyLight nibbles
     * After all sections: byte[256] biome data (all plains=1)
     *
     * In 1.9-1.12, entries can span across long boundaries (bits are packed
     * consecutively without padding per long).
     *
     * @return V109ChunkData with raw (uncompressed) data and primaryBitMask
     */
    public V109ChunkData serializeForV109Protocol() {
        // Determine which sections (0-7) contain non-air blocks.
        int primaryBitMask = 0;
        for (int section = 0; section < 8; section++) {
            int baseY = section * 16;
            boolean hasBlocks = false;
            for (int x = 0; x < WIDTH && !hasBlocks; x++) {
                for (int z = 0; z < DEPTH && !hasBlocks; z++) {
                    for (int ly = 0; ly < 16; ly++) {
                        if (getBlock(x, baseY + ly, z) != 0) {
                            hasBlocks = true;
                        }
                    }
                }
            }
            if (hasBlocks) {
                primaryBitMask |= (1 << section);
            }
        }

        // Build the raw data using a ByteArrayOutputStream for simplicity
        // since paletted sections have variable size.
        ByteArrayOutputStream baos = new ByteArrayOutputStream(16384);

        for (int section = 0; section < 8; section++) {
            if ((primaryBitMask & (1 << section)) == 0) continue;
            int baseY = section * 16;

            // Collect unique block states and build palette
            // Block state = (blockId << 4) | metadata
            java.util.LinkedHashMap<Integer, Integer> paletteMap = new java.util.LinkedHashMap<>();
            int[] blockStates = new int[4096];
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x++) {
                        int blockId = getBlock(x, baseY + ly, z);
                        int meta = getBlockData(x, baseY + ly, z);
                        int state = (blockId << 4) | (meta & 0x0F);
                        int idx = (ly * 16 + z) * 16 + x;
                        blockStates[idx] = state;
                        if (!paletteMap.containsKey(state)) {
                            paletteMap.put(state, paletteMap.size());
                        }
                    }
                }
            }

            int paletteSize = paletteMap.size();

            // Use global palette mode (bitsPerBlock=13) to bypass section palette.
            // In global mode, data array contains raw global block state IDs directly.
            int bitsPerBlock = 13;

            // Write bitsPerBlock
            baos.write(bitsPerBlock);

            // Write empty palette (global palette mode: VarInt(0))
            writeVarIntToStream(baos, 0);

            // Pack raw global block state IDs into longs
            // 1.9-1.12: entries span across long boundaries
            int totalBits = 4096 * bitsPerBlock;
            int longsNeeded = (totalBits + 63) / 64;
            long[] dataArray = new long[longsNeeded];

            long mask = (1L << bitsPerBlock) - 1;
            for (int i = 0; i < 4096; i++) {
                long stateId = blockStates[i]; // raw global block state ID
                int bitIndex = i * bitsPerBlock;
                int longIndex = bitIndex / 64;
                int bitOffset = bitIndex % 64;
                dataArray[longIndex] |= (stateId & mask) << bitOffset;
                // Check if entry spans two longs
                if (bitOffset + bitsPerBlock > 64) {
                    int bitsInFirst = 64 - bitOffset;
                    dataArray[longIndex + 1] |= (stateId & mask) >> bitsInFirst;
                }
            }

            // Write dataArrayLength + data
            writeVarIntToStream(baos, longsNeeded);
            for (int i = 0; i < longsNeeded; i++) {
                writeLongToStream(baos, dataArray[i]);
            }

            // Write block light nibbles (2048 bytes)
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x += 2) {
                        int low = getBlockLight(x, baseY + ly, z) & 0x0F;
                        int high = getBlockLight(x + 1, baseY + ly, z) & 0x0F;
                        baos.write(low | (high << 4));
                    }
                }
            }

            // Write sky light nibbles (2048 bytes)
            for (int ly = 0; ly < 16; ly++) {
                for (int z = 0; z < DEPTH; z++) {
                    for (int x = 0; x < WIDTH; x += 2) {
                        int low = getSkyLight(x, baseY + ly, z) & 0x0F;
                        int high = getSkyLight(x + 1, baseY + ly, z) & 0x0F;
                        baos.write(low | (high << 4));
                    }
                }
            }
        }

        // Biome data: 256 bytes, all plains (1)
        byte[] biomes = new byte[256];
        Arrays.fill(biomes, (byte) 1);
        baos.write(biomes, 0, 256);

        return new V109ChunkData(baos.toByteArray(), primaryBitMask);
    }

    /** Write a VarInt to a ByteArrayOutputStream (for chunk serialization). */
    private static void writeVarIntToStream(ByteArrayOutputStream out, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.write(value);
                return;
            }
            out.write((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    /** Write a big-endian long to a ByteArrayOutputStream. */
    private static void writeLongToStream(ByteArrayOutputStream out, long value) {
        out.write((int) (value >> 56) & 0xFF);
        out.write((int) (value >> 48) & 0xFF);
        out.write((int) (value >> 40) & 0xFF);
        out.write((int) (value >> 32) & 0xFF);
        out.write((int) (value >> 24) & 0xFF);
        out.write((int) (value >> 16) & 0xFF);
        out.write((int) (value >> 8) & 0xFF);
        out.write((int) value & 0xFF);
    }

    /**
     * Result container for v109 chunk serialization.
     */
    public static class V109ChunkData {
        private final byte[] rawData;
        private final int primaryBitMask;

        public V109ChunkData(byte[] rawData, int primaryBitMask) {
            this.rawData = rawData;
            this.primaryBitMask = primaryBitMask;
        }

        public byte[] getRawData() { return rawData; }
        public int getPrimaryBitMask() { return primaryBitMask; }
    }

    /**
     * Result container for v28 chunk serialization.
     */
    public static class V28ChunkData {
        private final byte[] compressedData;
        private final short primaryBitMask;

        public V28ChunkData(byte[] compressedData, short primaryBitMask) {
            this.compressedData = compressedData;
            this.primaryBitMask = primaryBitMask;
        }

        public byte[] getCompressedData() { return compressedData; }
        public short getPrimaryBitMask() { return primaryBitMask; }
    }

    /**
     * Get the 4-bit block light for a block at local coordinates.
     */
    public int getBlockLight(int x, int y, int z) {
        return getNibble(blockLight, blockIndex(x, y, z));
    }

    /**
     * Get the 4-bit sky light for a block at local coordinates.
     */
    public int getSkyLight(int x, int y, int z) {
        return getNibble(skyLight, blockIndex(x, y, z));
    }

    // === Raw array access for serialization ===

    public byte[] getBlocks() { return blocks; }
    public byte[] getData() { return data; }
    public byte[] getBlockLight() { return blockLight; }
    public byte[] getSkyLight() { return skyLight; }
    public byte[] getHeightMap() { return heightMap; }

    public int getXPos() { return xPos; }
    public int getZPos() { return zPos; }
    public boolean isTerrainPopulated() { return terrainPopulated; }
    public void setTerrainPopulated(boolean populated) { this.terrainPopulated = populated; }
    public long getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }

    // === Entity management ===

    public List<AlphaEntity> getEntities() { return entities; }

    public void addEntity(AlphaEntity entity) {
        entities.add(entity);
    }

    public void clearEntities() {
        entities.clear();
    }

    // === Tile entity management ===

    public List<AlphaTileEntity> getTileEntities() { return tileEntities; }

    public void addTileEntity(AlphaTileEntity tileEntity) {
        tileEntities.add(tileEntity);
    }

    /**
     * Find the tile entity at the given block coordinates, or null if none exists.
     */
    public AlphaTileEntity getTileEntityAt(int x, int y, int z) {
        for (int i = 0; i < tileEntities.size(); i++) {
            AlphaTileEntity te = tileEntities.get(i);
            if (te.getX() == x && te.getY() == y && te.getZ() == z) {
                return te;
            }
        }
        return null;
    }

    /**
     * Remove the tile entity at the given block coordinates.
     * Returns true if a tile entity was removed.
     */
    public boolean removeTileEntityAt(int x, int y, int z) {
        Iterator<AlphaTileEntity> it = tileEntities.iterator();
        while (it.hasNext()) {
            AlphaTileEntity te = it.next();
            if (te.getX() == x && te.getY() == y && te.getZ() == z) {
                it.remove();
                return true;
            }
        }
        return false;
    }
}
