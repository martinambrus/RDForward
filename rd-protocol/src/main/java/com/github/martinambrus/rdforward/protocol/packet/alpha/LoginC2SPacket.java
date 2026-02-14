package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x01 (Client -> Server): Login Request.
 *
 * Sent after the handshake to initiate login.
 * Self-adaptive: reads mapSeed/dimension only for protocol v3+,
 * since the decoder doesn't yet know the client's version when
 * decoding this packet.
 *
 * Wire format (v3+):
 *   [int]      protocol version
 *   [string16] username
 *   [string16] unused (empty, was password in early tests)
 *   [long]     map seed (0, not used by client)
 *   [byte]     dimension (0, not used by client)
 *
 * Wire format (v1-v2):
 *   [int]      protocol version
 *   [string16] username
 *   [string16] unused
 */
public class LoginC2SPacket implements Packet {

    private int protocolVersion;
    private String username;
    private long mapSeed;
    private byte dimension;

    public LoginC2SPacket() {}

    public LoginC2SPacket(int protocolVersion, String username) {
        this.protocolVersion = protocolVersion;
        this.username = username;
    }

    @Override
    public int getPacketId() {
        return 0x01;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(protocolVersion);
        McDataTypes.writeJavaUTF(buf, username);
        McDataTypes.writeJavaUTF(buf, "");
        // v1-v2 (Alpha 1.0.17-1.1.2_01) don't send mapSeed/dimension
        if (protocolVersion >= 3) {
            buf.writeLong(mapSeed);
            buf.writeByte(dimension);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readInt();
        username = McDataTypes.readJavaUTF(buf);
        McDataTypes.readJavaUTF(buf); // unused
        // v1-v2 (Alpha 1.0.17-1.1.2_01) don't send mapSeed/dimension
        if (protocolVersion >= 3) {
            mapSeed = buf.readLong();
            dimension = buf.readByte();
        }
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getUsername() { return username; }
    public long getMapSeed() { return mapSeed; }
    public byte getDimension() { return dimension; }
}
