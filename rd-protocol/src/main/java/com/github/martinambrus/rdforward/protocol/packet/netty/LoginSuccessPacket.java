package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Login state, S2C packet 0x02: Login Success.
 *
 * Wire format:
 *   [String] uuid (hyphenated)
 *   [String] username
 */
public class LoginSuccessPacket implements Packet {

    private String uuid;
    private String username;

    public LoginSuccessPacket() {}

    public LoginSuccessPacket(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarIntString(buf, uuid);
        McDataTypes.writeVarIntString(buf, username);
    }

    @Override
    public void read(ByteBuf buf) {
        uuid = McDataTypes.readVarIntString(buf);
        username = McDataTypes.readVarIntString(buf);
    }

    public String getUuid() { return uuid; }
    public String getUsername() { return username; }
}
