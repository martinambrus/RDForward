package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0xFD (Server -> Client): Encryption Key Request.
 *
 * Sent by the server after receiving the v39+ Handshake. Contains the
 * server's RSA public key and a random verify token.
 *
 * Wire format:
 *   [string16] server ID ("" for offline mode)
 *   [short]    public key length
 *   [byte[]]   public key (X.509 encoded)
 *   [short]    verify token length
 *   [byte[]]   verify token (4 random bytes)
 */
public class EncryptionKeyRequestPacket implements Packet {

    private String serverId;
    private byte[] publicKey;
    private byte[] verifyToken;

    public EncryptionKeyRequestPacket() {}

    public EncryptionKeyRequestPacket(String serverId, byte[] publicKey, byte[] verifyToken) {
        this.serverId = serverId;
        this.publicKey = publicKey;
        this.verifyToken = verifyToken;
    }

    @Override
    public int getPacketId() {
        return 0xFD;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeString16(buf, serverId);
        buf.writeShort(publicKey.length);
        buf.writeBytes(publicKey);
        buf.writeShort(verifyToken.length);
        buf.writeBytes(verifyToken);
    }

    @Override
    public void read(ByteBuf buf) {
        serverId = McDataTypes.readString16(buf);
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
