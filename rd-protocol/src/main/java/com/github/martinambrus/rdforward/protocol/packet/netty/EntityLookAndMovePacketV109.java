package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, S2C packet 0x26: Entity Look and Relative Move.
 *
 * 1.9 changed deltas from byte to short. Scale: 1 unit = 1/4096 block (was 1/32).
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [short]   dx
 *   [short]   dy
 *   [short]   dz
 *   [byte]    yaw
 *   [byte]    pitch
 *   [boolean] onGround
 */
public class EntityLookAndMovePacketV109 implements Packet {

    private int entityId;
    private short dx;
    private short dy;
    private short dz;
    private int yaw;
    private int pitch;

    public EntityLookAndMovePacketV109() {}

    public EntityLookAndMovePacketV109(int entityId, short dx, short dy, short dz,
                                        int yaw, int pitch) {
        this.entityId = entityId;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() { return 0x26; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeShort(dx);
        buf.writeShort(dy);
        buf.writeShort(dz);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeBoolean(true); // onGround
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        dx = buf.readShort();
        dy = buf.readShort();
        dz = buf.readShort();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
        buf.readBoolean();
    }
}
