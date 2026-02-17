package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x15: Entity Relative Move.
 *
 * 1.8 changed entityId from int to VarInt and added boolean onGround.
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [byte]    dx
 *   [byte]    dy
 *   [byte]    dz
 *   [boolean] onGround
 */
public class EntityRelativeMovePacketV47 implements Packet {

    private int entityId;
    private int dx;
    private int dy;
    private int dz;

    public EntityRelativeMovePacketV47() {}

    public EntityRelativeMovePacketV47(int entityId, int dx, int dy, int dz) {
        this.entityId = entityId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    @Override
    public int getPacketId() { return 0x15; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeByte(dx);
        buf.writeByte(dy);
        buf.writeByte(dz);
        buf.writeBoolean(true); // onGround
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        dx = buf.readByte();
        dy = buf.readByte();
        dz = buf.readByte();
        buf.readBoolean();
    }
}
