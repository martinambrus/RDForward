package com.github.martinambrus.rdforward.server.hytale;

import com.github.luben.zstd.Zstd;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * Netty codec for Hytale's wire frame format.
 *
 * Each frame is:
 *   [4 bytes LE] payload length (excludes the 8-byte header)
 *   [4 bytes LE] packet ID
 *   [N bytes]    payload (may be Zstd-compressed)
 *
 * Compression is per-packet: the packet registry marks which packet IDs
 * are compressed. For simplicity, we attempt Zstd decompression on inbound
 * packets whose payload starts with a Zstd magic number, and compress
 * outbound packets that exceed the compression threshold.
 */
public class HytaleFrameCodec extends ByteToMessageCodec<HytalePacketBuffer> {

    /** Zstd frame magic number (little-endian: 0xFD2FB528). */
    private static final int ZSTD_MAGIC = 0xFD2FB528;

    /** Set of packet IDs that should be Zstd-compressed on encode. */
    private final boolean[] compressedPackets;

    public HytaleFrameCodec() {
        // Packet IDs known to use compression (from decompiled registry)
        compressedPackets = new boolean[500];
        int[] compressed = {
            20, 23, 25, 31, 34, // Setup
            40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55,
            56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 72, 73, 74,
            75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, // Registries
            131, 132, 133, 134, 136, // Chunks
            161, // EntityUpdates
            170, // UpdatePlayerInventory
        };
        for (int id : compressed) {
            compressedPackets[id] = true;
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, HytalePacketBuffer msg, ByteBuf out) throws Exception {
        ByteBuf payload = msg.getBuf();
        int packetId = msg.getPacketId();
        int payloadLen = payload.readableBytes();

        // Packets marked IS_COMPRESSED in the Hytale registry MUST always be
        // Zstd-compressed — the client unconditionally decompresses based on packet ID.
        boolean shouldCompress = packetId < compressedPackets.length && compressedPackets[packetId]
                && payloadLen > 0;

        if (shouldCompress) {
            // Zstd compress
            byte[] uncompressed = new byte[payloadLen];
            payload.readBytes(uncompressed);
            byte[] compressed = Zstd.compress(uncompressed, HytaleProtocolConstants.ZSTD_COMPRESSION_LEVEL);

            out.writeIntLE(compressed.length); // payload length
            out.writeIntLE(packetId);          // packet ID
            out.writeBytes(compressed);        // compressed payload
        } else {
            out.writeIntLE(payloadLen);  // payload length
            out.writeIntLE(packetId);    // packet ID
            out.writeBytes(payload);     // raw payload
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= 8) {
            in.markReaderIndex();

            int payloadLength = in.readIntLE();
            int packetId = in.readIntLE();

            if (payloadLength < 0 || payloadLength > HytaleProtocolConstants.MAX_PAYLOAD_SIZE) {
                throw new RuntimeException("Invalid Hytale payload length: " + payloadLength
                        + " for packet ID " + packetId);
            }

            if (in.readableBytes() < payloadLength) {
                // Not enough data yet — wait for more
                in.resetReaderIndex();
                return;
            }

            ByteBuf payloadBuf;
            if (payloadLength > 0 && isZstdCompressed(in)) {
                // Decompress Zstd payload
                byte[] compressed = new byte[payloadLength];
                in.readBytes(compressed);
                long decompressedSize = Zstd.decompressedSize(compressed);
                if (decompressedSize <= 0 || decompressedSize > HytaleProtocolConstants.MAX_PAYLOAD_SIZE) {
                    // Fallback: estimate 4x expansion
                    decompressedSize = payloadLength * 4L;
                }
                byte[] decompressed = Zstd.decompress(compressed, (int) decompressedSize);
                payloadBuf = ctx.alloc().buffer(decompressed.length);
                payloadBuf.writeBytes(decompressed);
            } else if (payloadLength > 0) {
                payloadBuf = ctx.alloc().buffer(payloadLength);
                in.readBytes(payloadBuf, payloadLength);
            } else {
                payloadBuf = ctx.alloc().buffer(0);
            }

            out.add(new HytalePacketBuffer(packetId, payloadBuf));
        }
    }

    /** Check if the next bytes look like a Zstd frame (peek without consuming). */
    private boolean isZstdCompressed(ByteBuf buf) {
        if (buf.readableBytes() < 4) return false;
        int magic = buf.getIntLE(buf.readerIndex());
        return magic == ZSTD_MAGIC;
    }
}
