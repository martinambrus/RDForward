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
 */
public class CipherDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final MinecraftCipher cipher;

    public CipherDecoder(MinecraftCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        int readable = msg.readableBytes();
        byte[] input = new byte[readable];
        msg.readBytes(input);
        byte[] decrypted = cipher.update(input);
        ByteBuf output = ctx.alloc().buffer(decrypted.length);
        output.writeBytes(decrypted);
        out.add(output);
    }
}
