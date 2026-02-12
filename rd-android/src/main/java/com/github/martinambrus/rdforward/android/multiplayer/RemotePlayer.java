package com.github.martinambrus.rdforward.android.multiplayer;

/**
 * Represents a remote player visible to the local client.
 * Positions are in MC Classic fixed-point units (divide by 32 for block coordinates).
 */
public class RemotePlayer {

    private final byte playerId;
    private final String name;
    private volatile short x, y, z;
    private volatile byte yaw, pitch;
    private short prevX, prevY, prevZ;

    public RemotePlayer(byte playerId, String name, short x, short y, short z, byte yaw, byte pitch) {
        this.playerId = playerId;
        this.name = name;
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
        this.prevX = x; this.prevY = y; this.prevZ = z;
    }

    public void updatePosition(short x, short y, short z, byte yaw, byte pitch) {
        this.prevX = this.x; this.prevY = this.y; this.prevZ = this.z;
        this.x = x; this.y = y; this.z = z;
        this.yaw = yaw; this.pitch = pitch;
    }

    public byte getPlayerId() { return playerId; }
    public String getName() { return name; }
    public short getX() { return x; }
    public short getY() { return y; }
    public short getZ() { return z; }
    public byte getYaw() { return yaw; }
    public byte getPitch() { return pitch; }
}
