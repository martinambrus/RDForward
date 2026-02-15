package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x13 (Client -> Server): Entity Action.
 *
 * Sent when the player crouches, uncrouches, or leaves a bed.
 *
 * Wire format (5 bytes payload):
 *   [int]  entity ID
 *   [byte] action ID (1 = crouch, 2 = uncrouch, 3 = leave bed)
 */
public class EntityActionPacket implements Packet {

    private int entityId;
    private byte actionId;

    public EntityActionPacket() {}

    @Override
    public int getPacketId() {
        return 0x13;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(actionId);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        actionId = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public byte getActionId() { return actionId; }
}
