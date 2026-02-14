package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x01 (Client -> Server): Login Request for v1/v2.
 *
 * Alpha 1.0.17-1.1.2_01 (protocol v1-v2) login packets do NOT include
 * the mapSeed and dimension fields that were added in v3 (Alpha 1.2.0).
 *
 * Wire format:
 *   [int]      protocol version
 *   [string16] username
 *   [string16] unused (empty, was password in early tests)
 */
public class LoginC2SPacketV2 implements Packet {

    private int protocolVersion;
    private String username;

    public LoginC2SPacketV2() {}

    public LoginC2SPacketV2(int protocolVersion, String username) {
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
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readInt();
        username = McDataTypes.readJavaUTF(buf);
        McDataTypes.readJavaUTF(buf); // unused
    }

    public int getProtocolVersion() { return protocolVersion; }
    public String getUsername() { return username; }
}
