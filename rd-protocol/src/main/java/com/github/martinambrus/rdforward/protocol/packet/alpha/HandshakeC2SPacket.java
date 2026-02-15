package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x02 (Client -> Server): Handshake.
 *
 * The very first packet sent by an Alpha client, containing the username.
 *
 * Wire format:
 *   [string16] username
 */
public class HandshakeC2SPacket implements Packet {

    private String username;
    private boolean detectedString16;

    public HandshakeC2SPacket() {}

    public HandshakeC2SPacket(String username) {
        this.username = username;
    }

    @Override
    public int getPacketId() {
        return 0x02;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeStringAdaptive(buf, username);
    }

    @Override
    public void read(ByteBuf buf) {
        Object[] result = McDataTypes.readStringAuto(buf);
        username = (String) result[0];
        detectedString16 = (Boolean) result[1];
    }

    public String getUsername() { return username; }
    public boolean isDetectedString16() { return detectedString16; }
}
