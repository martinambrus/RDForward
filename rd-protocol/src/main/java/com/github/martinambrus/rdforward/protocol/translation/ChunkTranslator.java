package com.github.martinambrus.rdforward.protocol.translation;

import com.github.martinambrus.rdforward.protocol.Capability;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

/**
 * Translates chunk data between protocol versions for cross-version clients.
 *
 * When sending chunk data from a newer server to an older client, this class:
 *   1. Translates all block IDs via {@link BlockTranslator}
 *   2. Strips the metadata nibble array for pre-BLOCK_METADATA clients
 *   3. Adjusts chunk height if the world exceeds the client's Y limit
 *   4. Compresses the result with GZip for bandwidth reduction
 *
 * The server stores block data in its native format and translates
 * on-the-fly when sending to each client based on their protocol version.
 */
public class ChunkTranslator {

    /** RubyDung/Classic worlds are 64 blocks tall */
    private static final int CLASSIC_HEIGHT_LIMIT = 64;

    /** Alpha chunks are 128 blocks tall */
    private static final int ALPHA_HEIGHT_LIMIT = 128;

    /**
     * Translate a raw block array for a specific client version.
     *
     * @param blocks        the block data (will be copied, not modified in-place)
     * @param serverVersion the server's protocol version
     * @param clientVersion the client's protocol version
     * @return translated block data (new array)
     */
    public static byte[] translateBlocks(byte[] blocks, ProtocolVersion serverVersion,
                                          ProtocolVersion clientVersion) {
        if (serverVersion == clientVersion) {
            return blocks;
        }

        byte[] translated = Arrays.copyOf(blocks, blocks.length);
        BlockTranslator.translateArray(translated, serverVersion, clientVersion);
        return translated;
    }

    /**
     * Translate chunk block data and optionally strip metadata for a client.
     *
     * @param blocks        block ID array (1 byte per block)
     * @param metadata      metadata nibble array (4 bits per block), may be null
     * @param serverVersion server protocol version
     * @param clientVersion client protocol version
     * @return translated result with blocks and optionally metadata
     */
    public static TranslatedChunk translateChunk(byte[] blocks, byte[] metadata,
                                                   ProtocolVersion serverVersion,
                                                   ProtocolVersion clientVersion) {
        // Translate block IDs
        byte[] translatedBlocks = translateBlocks(blocks, serverVersion, clientVersion);

        // Strip metadata for clients that don't support it
        byte[] translatedMetadata = null;
        if (metadata != null && Capability.BLOCK_METADATA.isAvailableIn(clientVersion)) {
            translatedMetadata = Arrays.copyOf(metadata, metadata.length);
        }

        return new TranslatedChunk(translatedBlocks, translatedMetadata);
    }

    /**
     * Adjust chunk height for clients with lower Y limits.
     * Truncates or pads the block array to match the client's maximum height.
     *
     * @param blocks       block array in column-major order (Y is innermost)
     * @param chunkWidth   chunk width (typically 16)
     * @param sourceHeight source world height
     * @param targetHeight client's maximum height
     * @param chunkDepth   chunk depth (typically 16)
     * @return adjusted block array, or the original if no adjustment needed
     */
    public static byte[] adjustHeight(byte[] blocks, int chunkWidth, int sourceHeight,
                                       int targetHeight, int chunkDepth) {
        if (sourceHeight <= targetHeight) {
            return blocks;
        }

        // Truncate: only keep blocks up to targetHeight
        byte[] adjusted = new byte[chunkWidth * targetHeight * chunkDepth];
        for (int x = 0; x < chunkWidth; x++) {
            for (int z = 0; z < chunkDepth; z++) {
                for (int y = 0; y < targetHeight; y++) {
                    int srcIdx = y + (z * sourceHeight) + (x * sourceHeight * chunkDepth);
                    int dstIdx = y + (z * targetHeight) + (x * targetHeight * chunkDepth);
                    adjusted[dstIdx] = blocks[srcIdx];
                }
            }
        }
        return adjusted;
    }

    /**
     * Get the maximum world height for a client version.
     */
    public static int getHeightLimit(ProtocolVersion version) {
        if (version.isClassicFormat()) {
            return CLASSIC_HEIGHT_LIMIT;
        }
        return ALPHA_HEIGHT_LIMIT;
    }

    /**
     * GZip-compress chunk data for network transmission.
     * Used by both Classic level transfer and Alpha chunk packets.
     */
    public static byte[] compressGzip(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }

    /**
     * Result of translating a chunk's block and metadata arrays.
     */
    public static class TranslatedChunk {
        public final byte[] blocks;
        public final byte[] metadata; // null if client doesn't support metadata

        TranslatedChunk(byte[] blocks, byte[] metadata) {
            this.blocks = blocks;
            this.metadata = metadata;
        }
    }
}
