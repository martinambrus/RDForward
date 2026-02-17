package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x16: Entity Look.
 *
 * 1.8 changed entityId from int to VarInt and added boolean onGround.
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [byte]    yaw
 *   [byte]    pitch
 *   [boolean] onGround
 */
public class EntityLookPacketV47 implements Packet {

    private int entityId;
    private int yaw;
    private int pitch;

    public EntityLookPacketV47() {}

    public EntityLookPacketV47(int entityId, int yaw, int pitch) {
        this.entityId = entityId;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() { return 0x16; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeBoolean(true); // onGround
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
        buf.readBoolean();
    }
}
