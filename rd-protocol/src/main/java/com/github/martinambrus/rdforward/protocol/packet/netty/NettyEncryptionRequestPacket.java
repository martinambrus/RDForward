package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Login state, S2C packet 0x01: Encryption Request.
 *
 * Wire format:
 *   [String] serverId (empty for offline mode)
 *   [short]  publicKey length
 *   [byte[]] publicKey
 *   [short]  verifyToken length
 *   [byte[]] verifyToken
 */
public class NettyEncryptionRequestPacket implements Packet {

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    public NettyEncryptionRequestPacket() {}

    public NettyEncryptionRequestPacket(String serverId, byte[] publicKey, byte[] verifyToken) {
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
    }

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, serverId);
        buf.writeShort(publicKey.length);
        buf.writeBytes(publicKey);
        buf.writeShort(verifyToken.length);
        buf.writeBytes(verifyToken);
    }

    @Override
    public void read(ByteBuf buf) {
        serverId = McDataTypes.readVarIntString(buf);
        int pubKeyLen = buf.readShort();
        publicKey = new byte[pubKeyLen];
        buf.readBytes(publicKey);
        int tokenLen = buf.readShort();
        verifyToken = new byte[tokenLen];
        buf.readBytes(verifyToken);
    }

    public String getServerId() { return serverId; }
    public byte[] getPublicKey() { return publicKey; }
    public byte[] getVerifyToken() { return verifyToken; }
}
