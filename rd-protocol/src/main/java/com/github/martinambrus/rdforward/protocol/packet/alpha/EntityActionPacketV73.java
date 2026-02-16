package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.6.1+ protocol 0x13 (Client -> Server): Entity Action.
 *
 * Added int jumpBoost field for horse riding (horses introduced in 1.6.1).
 * Sent when the player crouches, uncrouches, leaves a bed, starts/stops
 * sprinting, or controls a horse jump.
 *
 * Wire format (9 bytes payload):
 *   [int]  entity ID
 *   [byte] action ID (1=crouch, 2=uncrouch, 3=leave bed, 4=start sprint, 5=stop sprint)
 *   [int]  jump boost (horse jump strength, 0 when not applicable)
 */
public class EntityActionPacketV73 implements Packet {

    private int entityId;
    private byte actionId;
    private int jumpBoost;

    public EntityActionPacketV73() {}

    @Override
    public int getPacketId() {
        return 0x13;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(actionId);
        buf.writeInt(jumpBoost);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        actionId = buf.readByte();
        jumpBoost = buf.readInt();
    }

    public int getEntityId() { return entityId; }
    public byte getActionId() { return actionId; }
    public int getJumpBoost() { return jumpBoost; }
}
