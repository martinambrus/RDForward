package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x0C (Server -> Client): Despawn Player.
 *
 * Sent when a player disconnects or leaves the visible area.
 * The client should remove the player from the world.
 *
 * Wire format (1 byte payload):
 *   [1 byte] player ID (signed)
 */
public class DespawnPlayerPacket implements Packet {

    private int playerId;

    public DespawnPlayerPacket() {}

    public DespawnPlayerPacket(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public int getPacketId() {
        return 0x0C;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte(); // signed
    }

    public int getPlayerId() { return playerId; }
}
