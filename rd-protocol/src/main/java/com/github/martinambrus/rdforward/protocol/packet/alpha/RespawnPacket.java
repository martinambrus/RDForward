package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x09 (Client -> Server): Respawn.
 *
 * Sent by the client when the player clicks "Respawn" on the death screen.
 * Empty payload in Beta 1.0.
 */
public class RespawnPacket implements Packet {

    public RespawnPacket() {}

    @Override
    public int getPacketId() {
        return 0x09;
    }

    @Override
    public void write(ByteBuf buf) {
        // empty
    }

    @Override
    public void read(ByteBuf buf) {
        // empty
    }
}
