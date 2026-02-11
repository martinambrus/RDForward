package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0A (Client -> Server): Player On Ground.
 *
 * Sent by the client every tick when only the on-ground state changes
 * (no position or look change). Acts as a lightweight keep-alive
 * for the server's player movement timeout.
 *
 * Wire format (1 byte payload):
 *   [boolean] on ground
 */
public class PlayerOnGroundPacket implements Packet {

    private boolean onGround;

    public PlayerOnGroundPacket() {}

    public PlayerOnGroundPacket(boolean onGround) {
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x0A;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        onGround = buf.readBoolean();
    }

    public boolean isOnGround() { return onGround; }
}
