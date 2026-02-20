package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Login state, S2C packet 0x01: Encryption Request.
 *
 * 1.20.5 added a Boolean shouldAuthenticate at the end. In offline mode,
 * this is always false (the server does not verify against Mojang session servers).
 *
 * Wire format:
 *   [String]  serverId
 *   [VarInt]  publicKey length
 *   [byte[]]  publicKey
 *   [VarInt]  verifyToken length
 *   [byte[]]  verifyToken
 *   [Boolean] shouldAuthenticate
 */
public class NettyEncryptionRequestPacketV766 implements Packet {

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;
    private boolean shouldAuthenticate;

    public NettyEncryptionRequestPacketV766() {}

    public NettyEncryptionRequestPacketV766(String serverId, byte[] publicKey,
                                             byte[] verifyToken, boolean shouldAuthenticate) {
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
        this.shouldAuthenticate = shouldAuthenticate;
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
        buf.writeBoolean(shouldAuthenticate);
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
        shouldAuthenticate = buf.readBoolean();
    }

    public String getServerId() { return serverId; }
    public byte[] getPublicKey() { return publicKey; }
    public byte[] getVerifyToken() { return verifyToken; }
    public boolean isShouldAuthenticate() { return shouldAuthenticate; }
}
