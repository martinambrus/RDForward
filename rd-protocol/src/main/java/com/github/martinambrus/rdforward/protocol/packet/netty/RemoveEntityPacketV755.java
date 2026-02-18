package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.17 Play state, S2C packet 0x3A: Remove Entity.
 *
 * In 1.17.0 (v755) the DestroyEntities packet was changed to single-entity
 * format (no count prefix, just a single VarInt entityId).
 * In 1.17.1 (v756) it was reverted back to the array format.
 *
 * Wire format:
 *   [VarInt] entityId
 */
public class RemoveEntityPacketV755 implements Packet {

    private int entityId;

    public RemoveEntityPacketV755() {}

    public RemoveEntityPacketV755(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int getPacketId() { return 0x3A; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
    }

    public int getEntityId() { return entityId; }
}
