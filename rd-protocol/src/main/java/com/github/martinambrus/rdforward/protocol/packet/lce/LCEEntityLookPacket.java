package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Entity Look packet (ID 0x20, S2C).
 *
 * LCE uses short entity ID (not int like Java).
 *
 * Wire format:
 *   [short] entity ID
 *   [byte]  yRot
 *   [byte]  xRot (pitch)
 */
public class LCEEntityLookPacket implements Packet {

    private int entityId;
    private int yRot, xRot;

    public LCEEntityLookPacket() {}

    public LCEEntityLookPacket(int entityId, int yRot, int xRot) {
        this.entityId = entityId;
        this.yRot = yRot;
        this.xRot = xRot;
    }

    @Override
    public int getPacketId() { return 0x20; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(entityId);
        buf.writeByte(yRot);
        buf.writeByte(xRot);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readShort();
        yRot = buf.readUnsignedByte();
        xRot = buf.readUnsignedByte();
    }
}
