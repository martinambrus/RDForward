package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alphaver protocol 0x06 (Server -> Client): Spawn Position.
 *
 * Same as standard Alpha SpawnPositionPacket but with an extra long
 * field appended (seed/timestamp used by the Alphaver client).
 *
 * Wire format (20 bytes payload):
 *   [int]  x (block coordinate)
 *   [int]  y (block coordinate)
 *   [int]  z (block coordinate)
 *   [long] seed (0 for our purposes)
 */
public class SpawnPositionPacketAlphaver implements Packet {

    private int x;
    private int y;
    private int z;
    private long seed;

    public SpawnPositionPacketAlphaver() {}

    public SpawnPositionPacketAlphaver(int x, int y, int z, long seed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.seed = seed;
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
        buf.writeLong(seed);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        seed = buf.readLong();
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public long getSeed() { return seed; }
}
