package com.github.martinambrus.rdforward.server.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.lce.LCEBlockRegionUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.lce.LCEChunkVisibilityPacket;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

import java.io.ByteArrayOutputStream;

import java.util.zip.Deflater;

/**
 * Serializes chunks for the LCE protocol.
 *
 * LCE uses BlockRegionUpdatePacket (ID 0x33) with RLE+zlib compression.
 * Raw data format for a full chunk: blockIDs + blockData nibbles + biomes.
 * Compression: RLE encode first, then zlib compress.
 *
 * The RLE scheme (from 4J's compression.cpp):
 *   - Bytes 0-254: pass through literally
 *   - 255 followed by 0/1/2: encodes 1/2/3 literal 0xFF bytes
 *   - 255 followed by N (3-255) followed by byte B: encodes N+1 repetitions of B
 */
public class LCEChunkSerializer {

    /**
     * LCE's Level::maxBuildHeight. The client overrides ys to this value for
     * full chunks in handleBlockRegionUpdate, so we must send data at this
     * height even though our world is only 128 blocks tall.
     */
    private static final int LCE_HEIGHT = 256;
    /**
     * Build LCE chunk packets for the given chunk.
     * Returns [ChunkVisibility(visible=true), BlockRegionUpdate].
     */
    public static Packet[] buildChunkPackets(AlphaChunk chunk) {
        int chunkX = chunk.getXPos();
        int chunkZ = chunk.getZPos();
        int blockX = chunkX * AlphaChunk.WIDTH;
        int blockZ = chunkZ * AlphaChunk.DEPTH;
        int srcHeight = AlphaChunk.HEIGHT; // 128

        byte[] blocks = chunk.getBlocks();
        byte[] srcData = chunk.getData();
        byte[] srcSkyLight = chunk.getSkyLight();
        byte[] srcBlockLight = chunk.getBlockLight();

        int w = AlphaChunk.WIDTH;  // 16
        int d = AlphaChunk.DEPTH;  // 16

        // Raw data layout at LCE_HEIGHT=256:
        // blockIDs (65536) + data nibbles (32768) + skyLight (32768) + blockLight (32768) + biomes (256)
        int lceBlockCount = w * LCE_HEIGHT * d;
        int lceNibbleCount = lceBlockCount / 2;
        int rawSize = lceBlockCount + lceNibbleCount * 3 + (w * d);
        byte[] raw = new byte[rawSize]; // zeros = air

        // Reorder block IDs from YZX (our storage, 128 high) to XZY (LCE wire, 256 high).
        // Upper 128 layers stay as 0 (air).
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                for (int y = 0; y < srcHeight; y++) {
                    int srcIndex = y + z * srcHeight + x * d * srcHeight;
                    int dstIndex = y * w * d + z * w + x;
                    raw[dstIndex] = blocks[srcIndex];
                }
            }
        }

        // Expand nibble data from 128-height columns to 256-height columns.
        // Nibbles are NOT reordered (memcpy in LCE source), but column strides
        // change with height so we can't just copy the flat array.
        int offset = lceBlockCount;
        expandNibbleColumns(srcData, raw, offset, srcHeight, w, d, (byte) 0);
        offset += lceNibbleCount;
        expandNibbleColumns(srcSkyLight, raw, offset, srcHeight, w, d, (byte) 0xFF);
        offset += lceNibbleCount;
        expandNibbleColumns(srcBlockLight, raw, offset, srcHeight, w, d, (byte) 0);

        // Biome data (all plains = 1)
        offset += lceNibbleCount;
        for (int i = 0; i < w * d; i++) {
            raw[offset + i] = 1;
        }

        byte[] compressed = compressRLEZlib(raw);

        return new Packet[]{
            new LCEChunkVisibilityPacket(chunkX, chunkZ, true),
            new LCEBlockRegionUpdatePacket(
                blockX, (short) 0, blockZ,
                w, LCE_HEIGHT, d,
                true,  // fullChunk
                0,     // dimensionIndex (overworld)
                compressed
            )
        };
    }

    /**
     * Expand nibble data from srcHeight-tall columns to LCE_HEIGHT-tall columns.
     * Each column (x,z) has srcHeight/2 bytes in the source; LCE_HEIGHT/2 in the dest.
     * Upper portion filled with fillValue (0 for data/blocklight, 0xFF for skylight).
     */
    private static void expandNibbleColumns(byte[] src, byte[] dst, int dstOffset,
                                             int srcHeight, int w, int d, byte fillValue) {
        int srcBytesPerCol = srcHeight / 2;           // 64
        int dstBytesPerCol = LCE_HEIGHT / 2;          // 128

        for (int x = 0; x < w; x++) {
            for (int z = 0; z < d; z++) {
                int srcOff = z * srcBytesPerCol + x * d * srcBytesPerCol;
                int dstOff = dstOffset + z * dstBytesPerCol + x * d * dstBytesPerCol;

                // Copy lower 128 blocks' nibbles
                System.arraycopy(src, srcOff, dst, dstOff, srcBytesPerCol);

                // Fill upper 128 blocks' nibbles
                if (fillValue != 0) {
                    for (int i = srcBytesPerCol; i < dstBytesPerCol; i++) {
                        dst[dstOff + i] = fillValue;
                    }
                }
            }
        }
    }

    /**
     * RLE encode then zlib compress (matches LCE's CompressLZXRLE on Windows64).
     */
    static byte[] compressRLEZlib(byte[] raw) {
        // Step 1: RLE encode
        byte[] rle = rleEncode(raw);

        // One-time verification: decode RLE and check round-trip
        if (!rleVerified) {
            byte[] decoded = rleDecode(rle, raw.length);
            boolean match = decoded != null && decoded.length == raw.length;
            if (match) {
                for (int i = 0; i < raw.length; i++) {
                    if (raw[i] != decoded[i]) {
                        System.err.println("[LCE-RLE] Round-trip mismatch at offset " + i
                                + ": orig=" + (raw[i] & 0xFF) + " decoded=" + (decoded[i] & 0xFF));
                        match = false;
                        break;
                    }
                }
            }
            if (!match && decoded != null) {
                System.err.println("[LCE-RLE] Round-trip FAILED: rawLen=" + raw.length
                        + " decodedLen=" + decoded.length);
            }
            rleVerified = true;
        }

        // Step 2: zlib compress
        return zlibCompress(rle);
    }

    private static volatile boolean rleVerified = false;

    /**
     * RLE encode using LCE's custom scheme.
     *
     * Rules:
     * - Bytes 0-254: pass through literally
     * - Byte 0xFF: escape marker
     *   - [0xFF, 0]: one 0xFF
     *   - [0xFF, 1]: two 0xFF
     *   - [0xFF, 2]: three 0xFF
     *   - [0xFF, N, B] where N >= 3: N+1 repetitions of byte B
     */
    static byte[] rleEncode(byte[] input) {
        // Worst case: every byte is 0xFF → 2x expansion
        byte[] output = new byte[input.length * 2];
        int inPos = 0;
        int outPos = 0;

        while (inPos < input.length) {
            int b = input[inPos++] & 0xFF;
            int count = 1;

            while (inPos < input.length && (input[inPos] & 0xFF) == b && count < 256) {
                inPos++;
                count++;
            }

            if (count <= 3) {
                if (b == 0xFF) {
                    output[outPos++] = (byte) 0xFF;
                    output[outPos++] = (byte) (count - 1);
                } else {
                    for (int i = 0; i < count; i++) {
                        output[outPos++] = (byte) b;
                    }
                }
            } else {
                output[outPos++] = (byte) 0xFF;
                output[outPos++] = (byte) (count - 1);
                output[outPos++] = (byte) b;
            }
        }

        byte[] result = new byte[outPos];
        System.arraycopy(output, 0, result, 0, outPos);
        return result;
    }

    /**
     * RLE decode (inverse of rleEncode). Used for round-trip verification only.
     */
    static byte[] rleDecode(byte[] input, int expectedLen) {
        byte[] output = new byte[expectedLen];
        int inPos = 0, outPos = 0;
        while (inPos < input.length && outPos < expectedLen) {
            int b = input[inPos++] & 0xFF;
            if (b != 0xFF) {
                output[outPos++] = (byte) b;
            } else {
                if (inPos >= input.length) break;
                int n = input[inPos++] & 0xFF;
                if (n <= 2) {
                    // n+1 copies of 0xFF
                    for (int i = 0; i <= n && outPos < expectedLen; i++) {
                        output[outPos++] = (byte) 0xFF;
                    }
                } else {
                    // n+1 copies of next byte
                    if (inPos >= input.length) break;
                    int v = input[inPos++] & 0xFF;
                    for (int i = 0; i <= n && outPos < expectedLen; i++) {
                        output[outPos++] = (byte) v;
                    }
                }
            }
        }
        if (outPos != expectedLen) return null;
        return output;
    }

    /**
     * Standard zlib compress (matches LCE's Compress() on Windows64).
     */
    static byte[] zlibCompress(byte[] input) {
        Deflater deflater = new Deflater(Deflater.BEST_SPEED);
        deflater.setInput(input);
        deflater.finish();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        byte[] buf = new byte[8192];
        while (!deflater.finished()) {
            int len = deflater.deflate(buf);
            baos.write(buf, 0, len);
        }
        deflater.end();
        return baos.toByteArray();
    }
}
