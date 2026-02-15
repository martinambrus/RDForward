package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x07 (Client -> Server): Use Entity.
 *
 * Sent when the player right-clicks or attacks another entity.
 *
 * Wire format (9 bytes payload):
 *   [int]  player entity ID
 *   [int]  target entity ID
 *   [byte] action (0 = right-click, 1 = attack)
 */
public class UseEntityPacket implements Packet {

    private int entityId;
    private int targetId;
    private byte action;

    public UseEntityPacket() {}

    @Override
    public int getPacketId() {
        return 0x07;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(targetId);
        buf.writeByte(action);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        targetId = buf.readInt();
        action = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public int getTargetId() { return targetId; }
    public byte getAction() { return action; }
}
