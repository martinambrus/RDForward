package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.19 Login state, S2C packet 0x02: Login Success.
 *
 * Same as V735 but with an empty property array appended after the username.
 *
 * Wire format:
 *   [Long] UUID most significant bits
 *   [Long] UUID least significant bits
 *   [String] username
 *   [VarInt] property count (0 = empty)
 */
public class LoginSuccessPacketV759 implements Packet {

    private long uuidMsb;
    private long uuidLsb;
    private String username;

    public LoginSuccessPacketV759() {}

    public LoginSuccessPacketV759(String uuid, String username) {
        String noDashes = uuid.replace("-", "");
        this.uuidMsb = Long.parseUnsignedLong(noDashes.substring(0, 16), 16);
        this.uuidLsb = Long.parseUnsignedLong(noDashes.substring(16, 32), 16);
        this.username = username;
    }

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeLong(uuidMsb);
        buf.writeLong(uuidLsb);
        McDataTypes.writeVarIntString(buf, username);
        McDataTypes.writeVarInt(buf, 0); // empty property array
    }

    @Override
    public void read(ByteBuf buf) {
        uuidMsb = buf.readLong();
        uuidLsb = buf.readLong();
        username = McDataTypes.readVarIntString(buf);
        int propCount = McDataTypes.readVarInt(buf);
        for (int i = 0; i < propCount; i++) {
            McDataTypes.readVarIntString(buf); // name
            McDataTypes.readVarIntString(buf); // value
            if (buf.readBoolean()) { // has signature
                McDataTypes.readVarIntString(buf); // signature
            }
        }
    }

    public String getUsername() { return username; }
}
