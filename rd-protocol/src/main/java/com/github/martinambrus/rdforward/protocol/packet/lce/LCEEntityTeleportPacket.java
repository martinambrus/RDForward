package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Entity Teleport packet (ID 0x22, S2C).
 *
 * LCE uses short entity ID (not int like Java), and int coordinates
 * when _LARGE_WORLDS is defined (which it is for Windows64).
 *
 * Wire format:
 *   [short] entity ID
 *   [int]   x (fixed-point, position * 32)
 *   [int]   y (fixed-point, position * 32)
 *   [int]   z (fixed-point, position * 32)
 *   [byte]  yRot
 *   [byte]  xRot (pitch)
 */
public class LCEEntityTeleportPacket implements Packet {

    private int entityId;
    private int x, y, z;
    private int yRot, xRot;

    public LCEEntityTeleportPacket() {}

    public LCEEntityTeleportPacket(int entityId, int x, int y, int z, int yRot, int xRot) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
    }

    @Override
    public int getPacketId() { return 0x22; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(entityId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yRot);
        buf.writeByte(xRot);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readShort();
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yRot = buf.readUnsignedByte();
        xRot = buf.readUnsignedByte();
    }
}
