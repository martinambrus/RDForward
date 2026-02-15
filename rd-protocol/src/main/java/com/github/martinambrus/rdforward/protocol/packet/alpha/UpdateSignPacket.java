package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x82 (bidirectional): Update Sign.
 *
 * Sent when a player finishes editing a sign, or by the server to
 * set sign text for the client.
 *
 * Wire format:
 *   [int]      x
 *   [short]    y
 *   [int]      z
 *   [string16] line 1
 *   [string16] line 2
 *   [string16] line 3
 *   [string16] line 4
 */
public class UpdateSignPacket implements Packet {

    private int x;
    private short y;
    private int z;
    private String line1;
    private String line2;
    private String line3;
    private String line4;

    public UpdateSignPacket() {}

    @Override
    public int getPacketId() {
        return 0x82;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeShort(y);
        buf.writeInt(z);
        McDataTypes.writeString16(buf, line1);
        McDataTypes.writeString16(buf, line2);
        McDataTypes.writeString16(buf, line3);
        McDataTypes.writeString16(buf, line4);
    }

    @Override
    public void read(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readShort();
        z = buf.readInt();
        line1 = McDataTypes.readString16(buf);
        line2 = McDataTypes.readString16(buf);
        line3 = McDataTypes.readString16(buf);
        line4 = McDataTypes.readString16(buf);
    }

    public int getX() { return x; }
    public short getY() { return y; }
    public int getZ() { return z; }
    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public String getLine3() { return line3; }
    public String getLine4() { return line4; }
}
