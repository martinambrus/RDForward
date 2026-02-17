package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, S2C packet 0x4D: Spawn Position.
 *
 * Same as V47 but uses 1.14 Position encoding (x:26|z:26|y:12 instead of x:26|y:12|z:26).
 *
 * Wire format: [Position] location (packed long, 1.14 encoding)
 */
public class SpawnPositionPacketV477 implements Packet {

    private int x;
    private int y;
    private int z;

    public SpawnPositionPacketV477() {}

    public SpawnPositionPacketV477(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getPacketId() { return 0x4D; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePositionV477(buf, x, y, z);
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPositionV477(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
    }
}
