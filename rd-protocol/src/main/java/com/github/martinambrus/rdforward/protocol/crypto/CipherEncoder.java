package com.github.martinambrus.rdforward.protocol.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Outbound handler that encrypts outgoing data using AES/CFB8.
 *
 * Inserted into the pipeline after the encryption handshake completes.
 * All outbound bytes are encrypted before being sent over the wire.
 *
 * Optimized to reuse an instance-level byte array instead of allocating
 * a new one per encrypt call. Uses MessageToMessageEncoder to avoid
 * the extra buffer copy that MessageToByteEncoder imposes.
 */
public class CipherEncoder extends MessageToMessageEncoder<ByteBuf> {

    private final MinecraftCipher cipher;
    private byte[] heapBuf = new byte[0];

    public CipherEncoder(MinecraftCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int readable = msg.readableBytes();
        if (heapBuf.length < readable) {
            heapBuf = new byte[readable];
        }
        msg.readBytes(heapBuf, 0, readable);
        byte[] encrypted = cipher.update(heapBuf, 0, readable);
        ByteBuf output = ctx.alloc().heapBuffer(encrypted.length);
        output.writeBytes(encrypted);
        out.add(output);
    }
}
