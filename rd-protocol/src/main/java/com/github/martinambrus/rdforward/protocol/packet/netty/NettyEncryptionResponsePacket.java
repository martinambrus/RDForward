package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Login state, C2S packet 0x01: Encryption Response.
 *
 * Wire format:
 *   [short]  shared secret length
 *   [byte[]] shared secret (RSA encrypted)
 *   [short]  verify token length
 *   [byte[]] verify token (RSA encrypted)
 */
public class NettyEncryptionResponsePacket implements Packet {

    private byte[] sharedSecret;
    private byte[] verifyToken;

    public NettyEncryptionResponsePacket() {}

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(sharedSecret.length);
        buf.writeBytes(sharedSecret);
        buf.writeShort(verifyToken.length);
        buf.writeBytes(verifyToken);
    }

    @Override
    public void read(ByteBuf buf) {
        int secretLen = buf.readShort();
        sharedSecret = new byte[secretLen];
        buf.readBytes(sharedSecret);
        int tokenLen = buf.readShort();
        verifyToken = new byte[tokenLen];
        buf.readBytes(verifyToken);
    }

    public byte[] getSharedSecret() { return sharedSecret; }
    public byte[] getVerifyToken() { return verifyToken; }
}
