package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.zip.Deflater;

/**
 * Compresses outbound packets using zlib when they exceed the threshold.
 *
 * Inserted into the Netty pipeline after Set Compression is sent (1.8+).
 * Sits between the packet encoder and the frame encoder.
 *
 * Input: raw packet bytes (VarInt packetId + fields)
 * Output: [VarInt dataLength] [data]
 *   - dataLength=0 + raw bytes if below threshold
 *   - dataLength=uncompressedSize + zlib-compressed bytes if at/above threshold
 *
 * The frame encoder then prepends the outer VarInt totalLength.
 *
 * Each connection gets its own Deflater instance (not thread-safe).
 */
public class PacketCompressEncoder extends MessageToByteEncoder<ByteBuf> {

    private final int threshold;
    private final Deflater deflater = new Deflater();
    private byte[] encodeBuf = new byte[8192];

    public PacketCompressEncoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int uncompressed = msg.readableBytes();
        if (uncompressed < threshold) {
            // Below threshold: write dataLength=0, then raw bytes
            McDataTypes.writeVarInt(out, 0);
            out.writeBytes(msg);
        } else {
            // At/above threshold: compress
            byte[] input = new byte[uncompressed];
            msg.readBytes(input);

            McDataTypes.writeVarInt(out, uncompressed);

            deflater.setInput(input, 0, uncompressed);
            deflater.finish();
            while (!deflater.finished()) {
                int count = deflater.deflate(encodeBuf);
                out.writeBytes(encodeBuf, 0, count);
            }
            deflater.reset();
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        deflater.end();
    }
}
