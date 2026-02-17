package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.14 Play state, C2S packet 0x1A: Player Digging.
 *
 * Same wire format as V47 but uses 1.14 Position encoding
 * (x<<38 | z<<12 | y instead of x<<38 | y<<26 | z).
 *
 * Wire format:
 *   [byte]     status
 *   [Position] location (packed long, 1.14 encoding)
 *   [byte]     face
 */
public class PlayerDiggingPacketV477 implements Packet {

    public static final int STATUS_STARTED = 0;
    public static final int STATUS_CANCELLED = 1;
    public static final int STATUS_FINISHED = 2;

    private int status;
    private int x;
    private int y;
    private int z;
    private int face;

    public PlayerDiggingPacketV477() {}

    public PlayerDiggingPacketV477(int status, int x, int y, int z, int face) {
        this.status = status;
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }

    @Override
    public int getPacketId() { return 0x1A; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(status);
        McDataTypes.writePositionV477(buf, x, y, z);
        buf.writeByte(face);
    }

    @Override
    public void read(ByteBuf buf) {
        status = buf.readUnsignedByte();
        int[] pos = McDataTypes.readPositionV477(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        face = buf.readUnsignedByte();
    }

    public int getStatus() { return status; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getFace() { return face; }
}
