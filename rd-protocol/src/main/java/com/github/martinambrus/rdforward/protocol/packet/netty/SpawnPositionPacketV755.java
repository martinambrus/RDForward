package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.17 Play state, S2C packet 0x4B: Spawn Position.
 *
 * Same as V477 but with a float angle field added after position.
 *
 * Wire format:
 *   [Position] location (packed long, 1.14 encoding)
 *   [float]    angle (yaw angle for compass)
 */
public class SpawnPositionPacketV755 implements Packet {

    private int x;
    private int y;
    private int z;

    public SpawnPositionPacketV755() {}

    public SpawnPositionPacketV755(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getPacketId() { return 0x4B; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePositionV477(buf, x, y, z);
        buf.writeFloat(0f); // angle
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPositionV477(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        buf.readFloat(); // angle
    }
}
