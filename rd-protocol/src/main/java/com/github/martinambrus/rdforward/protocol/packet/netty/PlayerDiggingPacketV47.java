package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x07: Player Digging.
 *
 * 1.8 changed separate x/y/z to a packed Position long.
 *
 * Wire format:
 *   [byte]     status
 *   [Position] location (packed long)
 *   [byte]     face
 */
public class PlayerDiggingPacketV47 implements Packet {

    public static final int STATUS_STARTED = 0;
    public static final int STATUS_CANCELLED = 1;
    public static final int STATUS_FINISHED = 2;

    private int status;
    private int x;
    private int y;
    private int z;
    private int face;

    public PlayerDiggingPacketV47() {}

    @Override
    public int getPacketId() { return 0x07; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(status);
        McDataTypes.writePosition(buf, x, y, z);
        buf.writeByte(face);
    }

    @Override
    public void read(ByteBuf buf) {
        status = buf.readUnsignedByte();
        int[] pos = McDataTypes.readPosition(buf);
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
