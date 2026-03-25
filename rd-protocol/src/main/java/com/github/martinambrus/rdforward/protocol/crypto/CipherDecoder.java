package com.github.martinambrus.rdforward.protocol.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Inbound handler that decrypts incoming data using AES/CFB8.
 *
 * Inserted into the pipeline after the encryption handshake completes.
 * All inbound bytes are decrypted before reaching the packet decoder.
 *
 * Optimized to reuse an instance-level byte array instead of allocating
 * a new one per decrypt call.
 */
public class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final MinecraftCipher cipher;
    private byte[] heapBuf = new byte[0];

    public CipherDecoder(MinecraftCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int readable = msg.readableBytes();
        if (heapBuf.length < readable) {
            heapBuf = new byte[readable];
        }
        msg.readBytes(heapBuf, 0, readable);
        byte[] decrypted = cipher.update(heapBuf, 0, readable);
        ByteBuf output = ctx.alloc().heapBuffer(decrypted.length);
        output.writeBytes(decrypted);
        out.add(output);
    }
}
