package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x01 (Server -> Client): Login Response for v1/v2.
 *
 * Alpha 1.0.17-1.1.2_01 (protocol v1-v2) login packets do NOT include
 * the mapSeed and dimension fields that were added in v3 (Alpha 1.2.0).
 *
 * Wire format:
 *   [int]      entity ID (player's EID)
 *   [string16] unused ("")
 *   [string16] unused ("")
 */
public class LoginS2CPacketV2 implements Packet {

    private int entityId;

    public LoginS2CPacketV2() {}

    public LoginS2CPacketV2(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int getPacketId() {
        return 0x01;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        McDataTypes.writeJavaUTF(buf, "");
        McDataTypes.writeJavaUTF(buf, "");
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        McDataTypes.readJavaUTF(buf); // unused
        McDataTypes.readJavaUTF(buf); // unused
    }

    public int getEntityId() { return entityId; }
}
