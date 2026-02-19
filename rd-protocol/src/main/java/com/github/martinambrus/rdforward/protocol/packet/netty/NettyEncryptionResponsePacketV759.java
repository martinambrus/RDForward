package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Login state, C2S packet 0x01: Encryption Response.
 *
 * 1.19 added a boolean before the verify token to support
 * the new chat signing system. In offline mode the client
 * sends hasVerifyToken=true followed by the RSA-encrypted token.
 *
 * Wire format:
 *   [VarInt]  shared secret length
 *   [byte[]]  shared secret (RSA encrypted)
 *   [Boolean] hasVerifyToken
 *   If true:
 *     [VarInt]  verify token length
 *     [byte[]]  verify token (RSA encrypted)
 *   If false:
 *     [Long]    salt
 *     [VarInt]  message signature length
 *     [byte[]]  message signature
 */
public class NettyEncryptionResponsePacketV759 implements Packet {

    private byte[] sharedSecret;
    private byte[] verifyToken;

    public NettyEncryptionResponsePacketV759() {}

    @Override
    public int getPacketId() { return 0x01; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, sharedSecret.length);
        buf.writeBytes(sharedSecret);
        buf.writeBoolean(true);
        McDataTypes.writeVarInt(buf, verifyToken.length);
        buf.writeBytes(verifyToken);
    }

    @Override
    public void read(ByteBuf buf) {
        int secretLen = McDataTypes.readVarInt(buf);
        sharedSecret = new byte[secretLen];
        buf.readBytes(sharedSecret);

        boolean hasVerifyToken = buf.readBoolean();
        if (hasVerifyToken) {
            int tokenLen = McDataTypes.readVarInt(buf);
            verifyToken = new byte[tokenLen];
            buf.readBytes(verifyToken);
        } else {
            // Chat signing: salt + signature — skip remaining bytes
            buf.skipBytes(buf.readableBytes());
            verifyToken = null;
        }
    }

    public byte[] getSharedSecret() { return sharedSecret; }
    public byte[] getVerifyToken() { return verifyToken; }
}
