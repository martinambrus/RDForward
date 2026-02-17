package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Login state, C2S packet 0x01: Encryption Response.
 *
 * 1.8 changed array length prefixes from short to VarInt.
 *
 * Wire format:
 *   [VarInt] shared secret length
 *   [byte[]] shared secret (RSA encrypted)
 *   [VarInt] verify token length
 *   [byte[]] verify token (RSA encrypted)
 */
public class NettyEncryptionResponsePacketV47 implements Packet {

    private byte[] sharedSecret;
    private byte[] verifyToken;

    public NettyEncryptionResponsePacketV47() {}

    public NettyEncryptionResponsePacketV47(byte[] sharedSecret, byte[] verifyToken) {
        this.sharedSecret = sharedSecret;
        this.verifyToken = verifyToken;
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, sharedSecret.length);
        buf.writeBytes(sharedSecret);
        McDataTypes.writeVarInt(buf, verifyToken.length);
        buf.writeBytes(verifyToken);
    }

    @Override
    public void read(ByteBuf buf) {
        int secretLen = McDataTypes.readVarInt(buf);
        sharedSecret = new byte[secretLen];
        buf.readBytes(sharedSecret);
        int tokenLen = McDataTypes.readVarInt(buf);
        verifyToken = new byte[tokenLen];
        buf.readBytes(verifyToken);
    }

    public byte[] getSharedSecret() { return sharedSecret; }
    public byte[] getVerifyToken() { return verifyToken; }
}
