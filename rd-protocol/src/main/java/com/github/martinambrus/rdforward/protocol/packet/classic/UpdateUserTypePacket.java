package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x0F (Server -> Client): Update User Type.
 *
 * Updates the client's user type (normal or op). This affects
 * whether the client can break bedrock and use certain features.
 *
 * Wire format (1 byte payload):
 *   [1 byte] user type (0x00 = normal, 0x64 = op)
 */
public class UpdateUserTypePacket implements Packet {

    public static final int USER_TYPE_NORMAL = 0x00;
    public static final int USER_TYPE_OP = 0x64;

    private int userType;

    public UpdateUserTypePacket() {}

    public UpdateUserTypePacket(int userType) {
        this.userType = userType;
    }

    @Override
    public int getPacketId() {
        return 0x0F;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(userType);
    }

    @Override
    public void read(ByteBuf buf) {
        userType = buf.readUnsignedByte();
    }

    public int getUserType() { return userType; }
}
