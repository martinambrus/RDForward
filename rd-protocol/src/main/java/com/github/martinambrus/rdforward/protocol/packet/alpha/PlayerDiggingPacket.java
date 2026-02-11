package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0E (Client -> Server): Player Digging.
 *
 * Sent when the player starts/finishes mining or drops items.
 *
 * Status values:
 *   0 = started digging
 *   1 = cancelled digging
 *   2 = finished digging (block should break)
 *   4 = drop item
 *
 * Face values: 0=bottom(-Y), 1=top(+Y), 2=-Z, 3=+Z, 4=-X, 5=+X
 *
 * Wire format (11 bytes payload):
 *   [byte] status
 *   [int]  x (block position)
 *   [byte] y (block position, 0-127)
 *   [int]  z (block position)
 *   [byte] face (0-5)
 */
public class PlayerDiggingPacket implements Packet {

    public static final int STATUS_STARTED = 0;
    public static final int STATUS_CANCELLED = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_DROP_ITEM = 4;

    private int status;
    private int x;
    private int y;
    private int z;
    private int face;

    public PlayerDiggingPacket() {}

    public PlayerDiggingPacket(int status, int x, int y, int z, int face) {
        this.status = status;
        this.x = x;
        this.y = y;
        this.z = z;
        this.face = face;
    }

    @Override
    public int getPacketId() {
        return 0x0E;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(status);
        buf.writeInt(x);
        buf.writeByte(y);
        buf.writeInt(z);
        buf.writeByte(face);
    }

    @Override
    public void read(ByteBuf buf) {
        status = buf.readUnsignedByte();
        x = buf.readInt();
        y = buf.readByte();
        z = buf.readInt();
        face = buf.readUnsignedByte();
    }

    public int getStatus() { return status; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public int getFace() { return face; }
}
