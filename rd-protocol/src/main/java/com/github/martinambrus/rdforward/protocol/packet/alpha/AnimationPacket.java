package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x12 (bidirectional): Animation.
 *
 * Sent when a player swings their arm or performs other animations.
 *
 * Wire format:
 *   [int]  entity ID
 *   [byte] animation (0 = no animation, 1 = swing arm)
 */
public class AnimationPacket implements Packet {

    private int entityId;
    private byte animation;

    public AnimationPacket() {}

    public AnimationPacket(int entityId, byte animation) {
        this.entityId = entityId;
        this.animation = animation;
    }

    @Override
    public int getPacketId() {
        return 0x12;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeByte(animation);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        animation = buf.readByte();
    }

    public int getEntityId() { return entityId; }
    public byte getAnimation() { return animation; }
}
