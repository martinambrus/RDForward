package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Login state, S2C packet 0x01: Encryption Request.
 *
 * 1.8 changed array length prefixes from short to VarInt.
 *
 * Wire format:
 *   [String] serverId
 *   [VarInt] publicKey length
 *   [byte[]] publicKey
 *   [VarInt] verifyToken length
 *   [byte[]] verifyToken
 */
public class NettyEncryptionRequestPacketV47 implements Packet {

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    public NettyEncryptionRequestPacketV47() {}

    public NettyEncryptionRequestPacketV47(String serverId, byte[] publicKey, byte[] verifyToken) {
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, serverId);
        McDataTypes.writeVarInt(buf, publicKey.length);
        buf.writeBytes(publicKey);
        McDataTypes.writeVarInt(buf, verifyToken.length);
        buf.writeBytes(verifyToken);
    }

    @Override
    public void read(ByteBuf buf) {
        serverId = McDataTypes.readVarIntString(buf);
        int pubKeyLen = McDataTypes.readVarInt(buf);
        publicKey = new byte[pubKeyLen];
        buf.readBytes(publicKey);
        int tokenLen = McDataTypes.readVarInt(buf);
        verifyToken = new byte[tokenLen];
        buf.readBytes(verifyToken);
    }

    public String getServerId() { return serverId; }
    public byte[] getPublicKey() { return publicKey; }
    public byte[] getVerifyToken() { return verifyToken; }
}
