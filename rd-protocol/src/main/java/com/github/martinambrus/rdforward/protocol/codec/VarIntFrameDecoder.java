package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
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
 */
public class VarIntFrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (in.readableBytes() > 0) {
            in.markReaderIndex();

            // Try to read VarInt length prefix
            int length;
            try {
                length = readVarIntOrReset(in);
            } catch (IndexOutOfBoundsException e) {
                // Not enough bytes for VarInt — wait
                in.resetReaderIndex();
                return;
            }
            if (length == -1) {
                // Incomplete VarInt — wait
                in.resetReaderIndex();
                return;
            }

            if (length < 0) {
                throw new RuntimeException("Negative VarInt frame length: " + length);
            }

            if (in.readableBytes() < length) {
                // Not enough data for the full frame — wait
                in.resetReaderIndex();
                return;
            }

            // Read the complete frame
            out.add(in.readRetainedSlice(length));
        }
    }

    /**
     * Read a VarInt without consuming bytes if incomplete.
     * Returns -1 if not enough bytes are available.
     */
    private static int readVarIntOrReset(ByteBuf buf) {
        int value = 0;
        int position = 0;

        while (true) {
            if (buf.readableBytes() < 1) {
                return -1;
            }
            byte currentByte = buf.readByte();
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) {
                return value;
            }

            position += 7;
            if (position >= 32) {
                throw new RuntimeException("VarInt is too big");
            }
        }
    }
}
