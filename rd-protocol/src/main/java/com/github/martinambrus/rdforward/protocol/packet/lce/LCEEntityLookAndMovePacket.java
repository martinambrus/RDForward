package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Entity Look and Relative Move packet (ID 0x21, S2C).
 *
 * LCE uses short entity ID (not int like Java).
 *
 * Wire format:
 *   [short] entity ID
 *   [byte]  dx (fixed-point delta)
 *   [byte]  dy (fixed-point delta)
 *   [byte]  dz (fixed-point delta)
 *   [byte]  yRot
 *   [byte]  xRot (pitch)
 */
public class LCEEntityLookAndMovePacket implements Packet {

    private int entityId;
    private int dx, dy, dz;
    private int yRot, xRot;

    public LCEEntityLookAndMovePacket() {}

    public LCEEntityLookAndMovePacket(int entityId, int dx, int dy, int dz, int yRot, int xRot) {
        this.entityId = entityId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.yRot = yRot;
        this.xRot = xRot;
    }

    @Override
    public int getPacketId() { return 0x21; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(entityId);
        buf.writeByte(dx);
        buf.writeByte(dy);
        buf.writeByte(dz);
        buf.writeByte(yRot);
        buf.writeByte(xRot);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readShort();
        dx = buf.readByte();
        dy = buf.readByte();
        dz = buf.readByte();
        yRot = buf.readUnsignedByte();
        xRot = buf.readUnsignedByte();
    }
}
