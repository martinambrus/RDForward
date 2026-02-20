package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Login state, S2C packet 0x02: Login Success.
 *
 * Extends V759 format with a Boolean strictErrorHandling appended after
 * the property count.
 *
 * Wire format:
 *   [Long]    UUID most significant bits
 *   [Long]    UUID least significant bits
 *   [String]  username
 *   [VarInt]  property count (0 = empty)
 *   [Boolean] strictErrorHandling (false)
 */
public class LoginSuccessPacketV766 implements Packet {

    private long uuidMsb;
    private long uuidLsb;
    private String username;

    public LoginSuccessPacketV766() {}

    public LoginSuccessPacketV766(String uuid, String username) {
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
        buf.writeBoolean(false); // strictErrorHandling
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
        buf.readBoolean(); // strictErrorHandling
    }

    public String getUsername() { return username; }
}
