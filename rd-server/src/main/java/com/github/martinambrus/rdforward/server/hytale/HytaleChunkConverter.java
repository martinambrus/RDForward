package com.github.martinambrus.rdforward.server.hytale;

import com.github.martinambrus.rdforward.server.ServerWorld;
import io.netty.buffer.ByteBufAllocator;

import static com.github.martinambrus.rdforward.server.hytale.HytaleProtocolConstants.*;

/**
 * Converts internal world data to Hytale's SetChunk (ID 131) packet format.
 *
 * Hytale chunks are 32x32x32 blocks (super-chunks) with 3D coordinates (x, y, z).
 * Each chunk uses palette-based storage:
 *   - Empty: all air (no block data)
 *   - HalfByte (<=16 block types): nibble-packed indices
 *   - Byte (<=256 block types): byte indices
 *   - Short: 16-bit indices (unused in our case)
 *
 * Palette format:
 *   short(LE) entryCount
 *   For each entry: byte internalId, int(LE) externalId, short(LE) count
 *
 * Block data indexing: index = y * 1024 + z * 32 + x (Y-major, then Z, then X)
 *
 * SetChunk wire format (fixed block = 25 bytes):
 *   [0]  byte    nullBits (bit0=localLight, bit1=globalLight, bit2=data)
 *   [1]  int32LE x
 *   [5]  int32LE y
 *   [9]  int32LE z
 *   [13] int32LE localLight offset
 *   [17] int32LE globalLight offset
 *   [21] int32LE data offset
 * Variable block: localLight(byte[]), globalLight(byte[]), data(byte[])
 */
public class HytaleChunkConverter {

    private static final int SUPER_CHUNK_SIZE = 32;
    private static final int BLOCKS_PER_CHUNK = SUPER_CHUNK_SIZE * SUPER_CHUNK_SIZE * SUPER_CHUNK_SIZE; // 32768
    private static final int FIXED_BLOCK_SIZE = 25;

    /** Palette type constants. */
    private static final byte PALETTE_EMPTY = 0;
    private static final byte PALETTE_HALF_BYTE = 1;
    private static final byte PALETTE_BYTE = 2;

    private final HytaleBlockMapper blockMapper;

    public HytaleChunkConverter(HytaleBlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    /**
     * Convert a 32x32x32 region of the world to a SetChunk packet.
     *
     * @param world the server world
     * @param chunkX super-chunk X coordinate (block X = chunkX * 32)
     * @param chunkY super-chunk Y coordinate (block Y = chunkY * 32)
     * @param chunkZ super-chunk Z coordinate (block Z = chunkZ * 32)
     * @param alloc ByteBuf allocator
     * @return SetChunk packet, or null if the region is entirely air
     */
    public HytalePacketBuffer convertChunk(ServerWorld world, int chunkX, int chunkY, int chunkZ,
                                            ByteBufAllocator alloc) {
        int baseX = chunkX * SUPER_CHUNK_SIZE;
        int baseY = chunkY * SUPER_CHUNK_SIZE;
        int baseZ = chunkZ * SUPER_CHUNK_SIZE;

        int worldWidth = world.getWidth();
        int worldHeight = world.getHeight();
        int worldDepth = world.getDepth();

        // Read block data from world, mapping to Hytale IDs
        int[] blocks = new int[BLOCKS_PER_CHUNK];
        boolean allAir = true;
        int uniqueTypes = 0;
        boolean[] seen = new boolean[92]; // internal block ID range

        for (int y = 0; y < SUPER_CHUNK_SIZE; y++) {
            int worldY = baseY + y;
            for (int z = 0; z < SUPER_CHUNK_SIZE; z++) {
                int worldZ = baseZ + z;
                for (int x = 0; x < SUPER_CHUNK_SIZE; x++) {
                    int worldX = baseX + x;

                    int blockId;
                    if (worldX < 0 || worldX >= worldWidth
                            || worldY < 0 || worldY >= worldHeight
                            || worldZ < 0 || worldZ >= worldDepth) {
                        blockId = 0; // out of bounds = air
                    } else {
                        blockId = world.getBlock(worldX, worldY, worldZ) & 0xFF;
                    }

                    int hytaleId = blockMapper.toHytaleId(blockId);
                    int index = y * 1024 + z * 32 + x;
                    blocks[index] = hytaleId;

                    if (hytaleId != 0) allAir = false;
                    if (!seen[hytaleId < 92 ? hytaleId : 0]) {
                        seen[hytaleId < 92 ? hytaleId : 0] = true;
                        uniqueTypes++;
                    }
                }
            }
        }

        // Build palette and block data
        byte[] chunkData;
        if (allAir) {
            // Empty palette: just the type byte
            chunkData = new byte[] { PALETTE_EMPTY };
        } else if (uniqueTypes <= 16) {
            chunkData = buildHalfBytePalette(blocks, seen);
        } else {
            chunkData = buildBytePalette(blocks, seen);
        }

        // Build SetChunk packet using exact wire format from decompiled SetChunk.serialize.
        //
        // SetChunk fixed block (25 bytes):
        //   [0]  byte    nullBits (bit0=localLight, bit1=globalLight, bit2=data)
        //   [1]  int32LE x
        //   [5]  int32LE y
        //   [9]  int32LE z
        //   [13] int32LE localLight offset (relative to VARIABLE_BLOCK_START=25)
        //   [17] int32LE globalLight offset (relative to VARIABLE_BLOCK_START=25)
        //   [21] int32LE data offset (relative to VARIABLE_BLOCK_START=25)
        // Variable block (byte 25+): localLight(byte[]), globalLight(byte[]), data(byte[])
        //
        // Offsets are backpatched: write placeholder, then set to (writerIndex - varBlockStart).
        // Null fields get offset -1.
        //
        // Light data: client accesses light arrays unconditionally during rendering.
        // Send full-brightness arrays (all 0x0F) for both local and global light.
        // Format is 1 byte per block = 32768 bytes per array.

        // Full-brightness light array (reused for both local and global)
        byte[] lightData = new byte[BLOCKS_PER_CHUNK];
        java.util.Arrays.fill(lightData, (byte) 0x0F);

        HytalePacketBuffer pkt = HytalePacketBuffer.create(PACKET_SET_CHUNK, alloc,
                FIXED_BLOCK_SIZE + lightData.length * 2 + chunkData.length + 20);
        io.netty.buffer.ByteBuf buf = pkt.getBuf();

        // Null bits: all three fields present (localLight + globalLight + data)
        buf.writeByte(0x07);

        // Chunk coordinates
        buf.writeIntLE(chunkX);
        buf.writeIntLE(chunkY);
        buf.writeIntLE(chunkZ);

        // Variable block offset slots (will backpatch)
        int localLightOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);   // localLight: placeholder
        int globalLightOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);   // globalLight: placeholder
        int dataOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);   // data: placeholder
        int varBlockStart = buf.writerIndex(); // VARIABLE_BLOCK_START = byte 25

        // Write localLight
        buf.setIntLE(localLightOffsetSlot, buf.writerIndex() - varBlockStart);
        pkt.writeByteArray(lightData);

        // Write globalLight
        buf.setIntLE(globalLightOffsetSlot, buf.writerIndex() - varBlockStart);
        pkt.writeByteArray(lightData);

        // Write block data
        buf.setIntLE(dataOffsetSlot, buf.writerIndex() - varBlockStart);
        pkt.writeByteArray(chunkData);

        return pkt;
    }

    /**
     * Build HalfByte palette (<=16 block types).
     * Format: paletteType(1) + entryCount(2 LE) + entries(7 each) + nibble data(16384).
     */
    private byte[] buildHalfBytePalette(int[] blocks, boolean[] seen) {
        // Build palette entries
        int[] paletteIds = new int[16]; // external ID for each palette index
        int[] idToPaletteIndex = new int[92];
        int paletteSize = 0;
        int[] counts = new int[92];

        for (int block : blocks) {
            int id = block < 92 ? block : 0;
            counts[id]++;
        }

        for (int i = 0; i < 92; i++) {
            if (seen[i]) {
                idToPaletteIndex[i] = paletteSize;
                paletteIds[paletteSize] = i;
                paletteSize++;
            }
        }

        // Calculate output size: 1 (type) + 2 (count) + paletteSize*7 (entries) + 16384 (data)
        int dataSize = 1 + 2 + paletteSize * 7 + (BLOCKS_PER_CHUNK / 2);
        byte[] out = new byte[dataSize];
        int pos = 0;

        out[pos++] = PALETTE_HALF_BYTE;

        // Entry count (short LE)
        out[pos++] = (byte) (paletteSize & 0xFF);
        out[pos++] = (byte) ((paletteSize >> 8) & 0xFF);

        // Palette entries: internalId(1) + externalId(4 LE) + count(2 LE)
        for (int i = 0; i < paletteSize; i++) {
            int extId = paletteIds[i];
            out[pos++] = (byte) i; // internal palette index
            out[pos++] = (byte) (extId & 0xFF);
            out[pos++] = (byte) ((extId >> 8) & 0xFF);
            out[pos++] = (byte) ((extId >> 16) & 0xFF);
            out[pos++] = (byte) ((extId >> 24) & 0xFF);
            int count = counts[extId];
            out[pos++] = (byte) (count & 0xFF);
            out[pos++] = (byte) ((count >> 8) & 0xFF);
        }

        // Nibble-packed block indices (2 blocks per byte)
        for (int i = 0; i < BLOCKS_PER_CHUNK; i += 2) {
            int id0 = blocks[i] < 92 ? blocks[i] : 0;
            int id1 = (i + 1 < BLOCKS_PER_CHUNK) ? (blocks[i + 1] < 92 ? blocks[i + 1] : 0) : 0;
            int idx0 = idToPaletteIndex[id0];
            int idx1 = idToPaletteIndex[id1];
            out[pos++] = (byte) ((idx1 << 4) | (idx0 & 0x0F));
        }

        return out;
    }

    /**
     * Build Byte palette (<=256 block types).
     * Format: paletteType(1) + entryCount(2 LE) + entries(7 each) + byte data(32768).
     */
    private byte[] buildBytePalette(int[] blocks, boolean[] seen) {
        int[] paletteIds = new int[256];
        int[] idToPaletteIndex = new int[92];
        int paletteSize = 0;
        int[] counts = new int[92];

        for (int block : blocks) {
            int id = block < 92 ? block : 0;
            counts[id]++;
        }

        for (int i = 0; i < 92; i++) {
            if (seen[i]) {
                idToPaletteIndex[i] = paletteSize;
                paletteIds[paletteSize] = i;
                paletteSize++;
            }
        }

        int dataSize = 1 + 2 + paletteSize * 7 + BLOCKS_PER_CHUNK;
        byte[] out = new byte[dataSize];
        int pos = 0;

        out[pos++] = PALETTE_BYTE;

        out[pos++] = (byte) (paletteSize & 0xFF);
        out[pos++] = (byte) ((paletteSize >> 8) & 0xFF);

        for (int i = 0; i < paletteSize; i++) {
            int extId = paletteIds[i];
            out[pos++] = (byte) i;
            out[pos++] = (byte) (extId & 0xFF);
            out[pos++] = (byte) ((extId >> 8) & 0xFF);
            out[pos++] = (byte) ((extId >> 16) & 0xFF);
            out[pos++] = (byte) ((extId >> 24) & 0xFF);
            int count = counts[extId];
            out[pos++] = (byte) (count & 0xFF);
            out[pos++] = (byte) ((count >> 8) & 0xFF);
        }

        for (int i = 0; i < BLOCKS_PER_CHUNK; i++) {
            int id = blocks[i] < 92 ? blocks[i] : 0;
            out[pos++] = (byte) idToPaletteIndex[id];
        }

        return out;
    }
}
