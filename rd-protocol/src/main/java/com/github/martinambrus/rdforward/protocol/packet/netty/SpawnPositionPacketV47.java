package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x05: Spawn Position.
 *
 * 1.8 changed 3 ints to a packed Position long.
 *
 * Wire format: [Position] location (packed long)
 */
public class SpawnPositionPacketV47 implements Packet {

    private int x;
    private int y;
    private int z;

    public SpawnPositionPacketV47() {}

    public SpawnPositionPacketV47(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getPacketId() { return 0x05; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePosition(buf, x, y, z);
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPosition(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
    }
}
