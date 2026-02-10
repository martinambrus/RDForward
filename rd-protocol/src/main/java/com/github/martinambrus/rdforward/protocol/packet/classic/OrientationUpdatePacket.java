package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x0B (Server -> Client): Orientation Update.
 *
 * Sent when a player changes their look direction without moving.
 *
 * Wire format (3 bytes payload):
 *   [1 byte] player ID (signed)
 *   [1 byte] yaw (0-255)
 *   [1 byte] pitch (0-255)
 */
public class OrientationUpdatePacket implements Packet {

    private int playerId;
    private int yaw;
    private int pitch;

    public OrientationUpdatePacket() {}

    public OrientationUpdatePacket(int playerId, int yaw, int pitch) {
        this.playerId = playerId;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x0B;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte(); // signed
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
    }

    public int getPlayerId() { return playerId; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
}
