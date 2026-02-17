package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x18: Entity Teleport.
 *
 * 1.8 changed entityId from int to VarInt and added boolean onGround.
 *
 * Wire format:
 *   [VarInt]  entityId
 *   [int]     x (fixed-point)
 *   [int]     y (fixed-point)
 *   [int]     z (fixed-point)
 *   [byte]    yaw
 *   [byte]    pitch
 *   [boolean] onGround
 */
public class EntityTeleportPacketV47 implements Packet {

    private int entityId;
    private int x;
    private int y;
    private int z;
    private int yaw;
    private int pitch;

    public EntityTeleportPacketV47() {}

    public EntityTeleportPacketV47(int entityId, int x, int y, int z, int yaw, int pitch) {
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() { return 0x18; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
        buf.writeBoolean(true); // onGround
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
        buf.readBoolean();
    }
}
