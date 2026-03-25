package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;
import java.util.zip.Inflater;

/**
 * Decompresses inbound packets that were zlib-compressed by the client.
 *
 * Inserted into the Netty pipeline after Set Compression is sent (1.8+).
 * Sits between the frame decoder and the packet decoder.
 *
 * Input: a complete frame (after VarInt length has been stripped by VarIntFrameDecoder)
 * Wire format inside the frame: [VarInt dataLength] [data]
 *   - dataLength=0: data is uncompressed (below threshold)
 *   - dataLength>0: data is zlib-compressed; dataLength = uncompressed size
 *
 * Each connection gets its own Inflater instance (not thread-safe).
 */
public class PacketDecompressDecoder extends MessageToMessageDecoder<ByteBuf> {

    /** Vanilla maximum uncompressed packet size: 8 MiB. */
    private static final int MAX_UNCOMPRESSED_SIZE = 8 * 1024 * 1024;

    private final int threshold;
    private final Inflater inflater = new Inflater();

    public PacketDecompressDecoder(int threshold) {
        this.threshold = threshold;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int dataLength = McDataTypes.readVarInt(in);

        if (dataLength == 0) {
            // Not compressed — pass through the remaining bytes
            out.add(in.readRetainedSlice(in.readableBytes()));
            return;
        }

        if (dataLength < threshold) {
            throw new DecoderException("Compressed packet below threshold: "
                    + dataLength + " < " + threshold);
        }
        if (dataLength > MAX_UNCOMPRESSED_SIZE) {
            throw new DecoderException("Uncompressed size " + dataLength
                    + " exceeds maximum " + MAX_UNCOMPRESSED_SIZE);
        }

        // Read compressed bytes
        byte[] compressed = new byte[in.readableBytes()];
        in.readBytes(compressed);

        // Inflate
        byte[] uncompressed = new byte[dataLength];
        inflater.setInput(compressed);
        inflater.inflate(uncompressed);
        inflater.reset();

        out.add(Unpooled.wrappedBuffer(uncompressed));
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        inflater.end();
    }
}
