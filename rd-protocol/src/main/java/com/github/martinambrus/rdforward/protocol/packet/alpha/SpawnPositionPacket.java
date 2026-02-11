package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x06 (Server -> Client): Spawn Position.
 *
 * Sent once after login to set the world's spawn point (compass target).
 *
 * Wire format (12 bytes payload):
 *   [int] x (block coordinate)
 *   [int] y (block coordinate)
 *   [int] z (block coordinate)
 */
public class SpawnPositionPacket implements Packet {

    private int x;
    private int y;
    private int z;

    public SpawnPositionPacket() {}

    public SpawnPositionPacket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int getPacketId() {
        return 0x06;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
}
