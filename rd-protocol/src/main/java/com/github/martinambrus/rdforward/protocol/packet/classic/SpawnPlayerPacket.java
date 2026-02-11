package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x07 (Server -> Client): Spawn Player.
 *
 * Sent when a player joins or when the client first connects
 * (player ID -1 means "this is you" / self-spawn).
 *
 * Coordinates are fixed-point shorts: divide by 32 for block coordinates.
 * Yaw/pitch are 0-255 mapping to 0-360 degrees.
 *
 * Wire format (73 bytes payload):
 *   [1 byte]  player ID (signed, -1 = self)
 *   [64 bytes] player name (space-padded US-ASCII)
 *   [2 bytes] X position (fixed-point: value / 32 = block coord)
 *   [2 bytes] Y position (fixed-point: value / 32 = block coord)
 *   [2 bytes] Z position (fixed-point: value / 32 = block coord)
 *   [1 byte]  yaw (0-255, 0 = -Z/North)
 *   [1 byte]  pitch (0-255, 0 = level)
 */
public class SpawnPlayerPacket implements Packet {

    public static final int SELF_ID = -1;

    private int playerId;
    private String playerName;
    private short x;
    private short y;
    private short z;
    private int yaw;
    private int pitch;

    public SpawnPlayerPacket() {}

    public SpawnPlayerPacket(int playerId, String playerName, short x, short y, short z, int yaw, int pitch) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public int getPacketId() {
        return 0x07;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(playerId);
        McDataTypes.writeClassicString(buf, playerName);
        buf.writeShort(x);
        buf.writeShort(y);
        buf.writeShort(z);
        buf.writeByte(yaw);
        buf.writeByte(pitch);
    }

    @Override
    public void read(ByteBuf buf) {
        playerId = buf.readByte(); // signed
        playerName = McDataTypes.readClassicString(buf);
        x = buf.readShort();
        y = buf.readShort();
        z = buf.readShort();
        yaw = buf.readUnsignedByte();
        pitch = buf.readUnsignedByte();
    }

    public int getPlayerId() { return playerId; }
    public String getPlayerName() { return playerName; }
    public short getX() { return x; }
    public short getY() { return y; }
    public short getZ() { return z; }
    public int getYaw() { return yaw; }
    public int getPitch() { return pitch; }
}
