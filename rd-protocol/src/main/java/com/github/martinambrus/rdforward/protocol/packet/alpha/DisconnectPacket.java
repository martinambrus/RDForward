package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0xFF (bidirectional): Disconnect / Kick.
 *
 * Sent to terminate the connection with a reason message.
 * Uses string16 encoding (vs Classic 0x0E's 64-byte fixed ASCII).
 *
 * Wire format:
 *   [string16] reason
 */
public class DisconnectPacket implements Packet {

    private String reason;

    public DisconnectPacket() {}

    public DisconnectPacket(String reason) {
        this.reason = reason;
    }

    @Override
    public int getPacketId() {
        return 0xFF;
    }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeJavaUTF(buf, reason);
    }

    @Override
    public void read(ByteBuf buf) {
        reason = McDataTypes.readJavaUTF(buf);
    }

    public String getReason() { return reason; }
}
