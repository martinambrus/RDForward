package com.github.martinambrus.rdforward.client;

/**
 * Represents a remote player visible to the local client.
 *
 * Stores the server-reported position and name. The renderer uses
 * this data to draw other players in the world. Positions are in
 * MC Classic fixed-point units (divide by 32 for block coordinates).
 */
public class RemotePlayer {

    private final byte playerId;
    private final String name;
    private volatile short x, y, z;
    private volatile byte yaw, pitch;

    // Interpolation: previous position for smooth rendering
    private short prevX, prevY, prevZ;
    private byte prevYaw, prevPitch;

    public RemotePlayer(byte playerId, String name, short x, short y, short z, byte yaw, byte pitch) {
        this.playerId = playerId;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.prevYaw = yaw;
        this.prevPitch = pitch;
    }

    /**
     * Update position from a server teleport packet.
     * Saves the old position for interpolation.
     */
    public void updatePosition(short x, short y, short z, byte yaw, byte pitch) {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Get interpolated X position in block coordinates.
     * @param partialTick 0.0 = previous position, 1.0 = current position
     */
    public float getInterpolatedX(float partialTick) {
        return (prevX + (x - prevX) * partialTick) / 32.0f;
    }

    public float getInterpolatedY(float partialTick) {
        return (prevY + (y - prevY) * partialTick) / 32.0f;
    }

    public float getInterpolatedZ(float partialTick) {
        return (prevZ + (z - prevZ) * partialTick) / 32.0f;
    }

    public byte getPlayerId() { return playerId; }
    public String getName() { return name; }
    public short getX() { return x; }
    public short getY() { return y; }
    public short getZ() { return z; }
    public byte getYaw() { return yaw; }
    public byte getPitch() { return pitch; }
}
