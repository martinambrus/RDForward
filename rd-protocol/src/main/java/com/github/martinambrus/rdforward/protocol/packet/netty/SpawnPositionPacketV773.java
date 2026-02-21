package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.9 Play state, S2C: Set Default Spawn Position.
 *
 * Wire format changed from V755: now uses GlobalBlockPosition encoding.
 *
 * Wire format:
 *   [String]   dimension (e.g. "minecraft:overworld")
 *   [Position] location (packed long, 1.14 encoding)
 *   [float]    yaw
 *   [float]    pitch
 */
public class SpawnPositionPacketV773 implements Packet {

    private int x;
    private int y;
    private int z;

    public SpawnPositionPacketV773() {}

    public SpawnPositionPacketV773(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getPacketId() { return 0x5F; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, "minecraft:overworld");
        McDataTypes.writePositionV477(buf, x, y, z);
        buf.writeFloat(0f); // yaw
        buf.writeFloat(0f); // pitch
    }

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarIntString(buf); // dimension
        int[] pos = McDataTypes.readPositionV477(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        buf.readFloat(); // yaw
        buf.readFloat(); // pitch
    }
}
