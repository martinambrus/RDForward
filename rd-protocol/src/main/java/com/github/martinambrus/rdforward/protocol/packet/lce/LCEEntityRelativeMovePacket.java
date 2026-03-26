package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Entity Relative Move packet (ID 0x1F, S2C).
 *
 * LCE uses short entity ID (not int like Java).
 *
 * Wire format:
 *   [short] entity ID
 *   [byte]  dx (fixed-point delta)
 *   [byte]  dy (fixed-point delta)
 *   [byte]  dz (fixed-point delta)
 */
public class LCEEntityRelativeMovePacket implements Packet {

    private int entityId;
    private int dx, dy, dz;

    public LCEEntityRelativeMovePacket() {}

    public LCEEntityRelativeMovePacket(int entityId, int dx, int dy, int dz) {
        this.entityId = entityId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    @Override
    public int getPacketId() { return 0x1F; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(entityId);
        buf.writeByte(dx);
        buf.writeByte(dy);
        buf.writeByte(dz);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readShort();
        dx = buf.readByte();
        dy = buf.readByte();
        dz = buf.readByte();
    }
}
