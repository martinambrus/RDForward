package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic 0.0.15a protocol 0x09 (Server -> Client): Despawn Player.
 *
 * Same payload as the standard DespawnPlayerPacket (0x0C) but uses
 * packet ID 0x09 as used in 0.0.15a. In later Classic versions,
 * 0x09 was reassigned to PositionOrientationUpdate and DespawnPlayer
 * moved to 0x0C.
 *
 * Wire format (1 byte payload):
 *   [1 byte] player ID (signed)
 */
public class DespawnPlayerPacketV015a implements Packet {

    private int playerId;

    public DespawnPlayerPacketV015a() {}

    public DespawnPlayerPacketV015a(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public int getPacketId() {
        return 0x09;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte();
    }

    public int getPlayerId() { return playerId; }
}
