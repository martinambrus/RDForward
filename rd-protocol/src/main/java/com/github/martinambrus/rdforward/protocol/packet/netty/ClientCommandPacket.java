package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x16: Client Status.
 *
 * Wire format: [byte] actionId (0=respawn, 1=request stats, 2=open achievement)
 */
public class ClientCommandPacket implements Packet {

    public static final int RESPAWN = 0;

    private int actionId;

    public ClientCommandPacket() {}

    @Override
    public int getPacketId() { return 0x16; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(actionId);
    }

    @Override
    public void read(ByteBuf buf) {
        actionId = buf.readByte();
    }

    public int getActionId() { return actionId; }
}
