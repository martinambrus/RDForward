package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x08 (Both directions): Position and Orientation / Teleport.
 *
 * Client -> Server: sent by the client to report its position and orientation.
 *   The player ID is always 0xFF (unused, server knows who sent it).
 *
 * Server -> Client: sent to set/teleport a player's absolute position.
 *   The player ID identifies which player is being moved.
 *   Player ID -1 means "you" (the receiving client).
 *
 * Coordinates are fixed-point shorts: divide by 32 for block coordinates.
 *
 * Wire format (9 bytes payload):
 *   [1 byte]  player ID (signed byte; -1=self in S->C, 0xFF in C->S)
 *   [2 bytes] X position (fixed-point)
 *   [2 bytes] Y position (fixed-point)
 *   [2 bytes] Z position (fixed-point)
 *   [1 byte]  yaw (0-255)
 *   [1 byte]  pitch (0-255)
 */
public class PlayerTeleportPacket implements Packet {

    private int playerId;
    private short x;
    private short y;
    private short z;
    private int yaw;
    private int pitch;

    public PlayerTeleportPacket() {}

    public PlayerTeleportPacket(int playerId, short x, short y, short z, int yaw, int pitch) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x08;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
        buf.writeShort(x);
        buf.writeShort(y);
        buf.writeShort(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte(); // signed
        x = buf.readShort();
        y = buf.readShort();
        z = buf.readShort();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
    }

    public int getPlayerId() { return playerId; }
    public short getX() { return x; }
    public short getY() { return y; }
    public short getZ() { return z; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
}
