package com.github.martinambrus.rdforward.world.alpha;

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

    public AlphaChunk(int xPos, int zPos) {
        this.xPos = xPos;
        this.zPos = zPos;
        this.blocks = new byte[BLOCK_COUNT];
        this.data = new byte[NIBBLE_COUNT];
        this.blockLight = new byte[NIBBLE_COUNT];
        this.skyLight = new byte[NIBBLE_COUNT];
        this.heightMap = new byte[WIDTH * DEPTH];
        this.terrainPopulated = false;
        this.lastUpdate = 0;

        // Initialize skylight to full brightness (15) for all blocks
        // Each byte holds two nibbles, so 0xFF = two blocks at light level 15
        for (int i = 0; i < skyLight.length; i++) {
            skyLight[i] = (byte) 0xFF;
        }
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
}
