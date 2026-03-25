package com.github.martinambrus.rdforward.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Decodes VarInt-framed packets (1.7.2+ Netty protocol).
 *
 * Wire format: [VarInt length] [length bytes of packet data]
 * Reads the VarInt length prefix, waits for enough bytes, then emits
 * a complete frame ByteBuf containing only the packet data (no length).
 *
 * Uses an optimized VarInt decoder derived from Velocity/Krypton that
 * reads up to 3 bytes of VarInt in a single int read when possible.
 */
public class VarIntFrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!ctx.channel().isActive()) {
            in.clear();
            return;
        }

        while (in.readableBytes() > 0) {
            in.markReaderIndex();
            int preIndex = in.readerIndex();

            int length = readRawVarInt21(in);
            if (preIndex == in.readerIndex()) {
                // No bytes consumed — incomplete VarInt, wait for more data
                return;
            }
            if (length < 0) {
                throw QuietDecoderException.BAD_LENGTH;
            }

            // Zero-length packets are ignored (same as Krypton)
            if (length > 0) {
                if (in.readableBytes() < length) {
                    // Not enough data for the full frame — wait
                    in.resetReaderIndex();
                    return;
                }
                out.add(in.readRetainedSlice(length));
            }
        }
    }

    /**
     * Reads a VarInt of up to 21 bits (3 bytes) from the buffer.
     * When 4+ bytes are available, reads them as a little-endian int and
     * extracts the value using bit manipulation — avoiding per-byte branching.
     * Falls back to byte-by-byte for buffers with fewer than 4 readable bytes.
     *
     * Derived from Velocity/Krypton and Netty PR #14050.
     *
     * @return the decoded VarInt, or 0 if not enough bytes
     * @throws QuietDecoderException if the VarInt exceeds 3 bytes
     */
    private static int readRawVarInt21(ByteBuf buffer) {
        if (buffer.readableBytes() < 4) {
            return readRawVarintSmallBuf(buffer);
        }
        int wholeOrMore = buffer.getIntLE(buffer.readerIndex());

        // Find the first byte without the continuation bit set
        int atStop = ~wholeOrMore & 0x808080;
        if (atStop == 0) {
            throw QuietDecoderException.VARINT_TOO_BIG;
        }

        int bitsToKeep = Integer.numberOfTrailingZeros(atStop) + 1;
        buffer.skipBytes(bitsToKeep >> 3);

        // Extract the value bits using bit manipulation
        int preservedBytes = wholeOrMore & (atStop ^ (atStop - 1));
        preservedBytes = (preservedBytes & 0x007F007F) | ((preservedBytes & 0x00007F00) >> 1);
        preservedBytes = (preservedBytes & 0x00003FFF) | ((preservedBytes & 0x3FFF0000) >> 2);
        return preservedBytes;
    }

    /**
     * Fallback VarInt reader for buffers with fewer than 4 readable bytes.
     * Returns 0 (no bytes consumed) if the VarInt is incomplete.
     */
    private static int readRawVarintSmallBuf(ByteBuf buffer) {
        if (!buffer.isReadable()) {
            return 0;
        }
        buffer.markReaderIndex();

        byte tmp = buffer.readByte();
        if (tmp >= 0) {
            return tmp;
        }
        int result = tmp & 0x7F;
        if (!buffer.isReadable()) {
            buffer.resetReaderIndex();
            return 0;
        }
        if ((tmp = buffer.readByte()) >= 0) {
            return result | tmp << 7;
        }
        result |= (tmp & 0x7F) << 7;
        if (!buffer.isReadable()) {
            buffer.resetReaderIndex();
            return 0;
        }
        if ((tmp = buffer.readByte()) >= 0) {
            return result | tmp << 14;
        }
        return result | (tmp & 0x7F) << 14;
    }
}
