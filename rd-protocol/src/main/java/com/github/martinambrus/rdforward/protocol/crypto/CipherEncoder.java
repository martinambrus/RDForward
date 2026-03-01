package com.github.martinambrus.rdforward.protocol.crypto;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Outbound handler that encrypts outgoing data using AES/CFB8.
 *
 * Inserted into the pipeline after the encryption handshake completes.
 * All outbound bytes are encrypted before being sent over the wire.
 */
public class CipherEncoder extends MessageToByteEncoder<ByteBuf> {

    private final MinecraftCipher cipher;

    public CipherEncoder(MinecraftCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        int readable = msg.readableBytes();
        byte[] input = new byte[readable];
        msg.readBytes(input);
        byte[] encrypted = cipher.update(input);
        if (encrypted != null) {
            out.writeBytes(encrypted);
        }
    }
}
