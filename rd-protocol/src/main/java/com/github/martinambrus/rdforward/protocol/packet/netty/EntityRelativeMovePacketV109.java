package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, S2C packet 0x25: Entity Relative Move.
 *
 * 1.9 changed deltas from byte to short. Scale: 1 unit = 1/4096 block (was 1/32).
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [short]   dx
 *   [short]   dy
 *   [short]   dz
 *   [boolean] onGround
 */
public class EntityRelativeMovePacketV109 implements Packet {

    private int entityId;
    private short dx;
    private short dy;
    private short dz;

    public EntityRelativeMovePacketV109() {}

    public EntityRelativeMovePacketV109(int entityId, short dx, short dy, short dz) {
        this.entityId = entityId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    @Override
    public int getPacketId() { return 0x25; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeShort(dx);
        buf.writeShort(dy);
        buf.writeShort(dz);
        buf.writeBoolean(true); // onGround
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        dx = buf.readShort();
        dy = buf.readShort();
        dz = buf.readShort();
        buf.readBoolean();
    }
}
