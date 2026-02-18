package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.16 Login state, S2C packet 0x02: Login Success.
 *
 * In 1.16 the UUID field changed from a VarIntString to a binary UUID
 * (two longs: most-significant bits, least-significant bits).
 *
 * Wire format:
 *   [long] uuid MSB
 *   [long] uuid LSB
 *   [String] username
 */
public class LoginSuccessPacketV735 implements Packet {

    private long uuidMsb;
    private long uuidLsb;
    private String username;

    public LoginSuccessPacketV735() {}

    public LoginSuccessPacketV735(String uuid, String username) {
        String hex = uuid.replace("-", "");
        this.uuidMsb = Long.parseUnsignedLong(hex.substring(0, 16), 16);
        this.uuidLsb = Long.parseUnsignedLong(hex.substring(16, 32), 16);
        this.username = username;
    }

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);
        McDataTypes.writeVarIntString(buf, username);
    }

    @Override
    public void read(ByteBuf buf) {
        uuidMsb = buf.readLong();
        uuidLsb = buf.readLong();
        username = McDataTypes.readVarIntString(buf);
    }

    public String getUsername() { return username; }
}
