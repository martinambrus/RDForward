package com.github.martinambrus.rdforward.client;

/**
 * Represents a remote player visible to the local client.
 *
 * Stores the server-reported position and name. The renderer uses
 * this data to draw other players in the world. Positions are in
 * MC Classic fixed-point units (divide by 32 for block coordinates).
 */
public class RemotePlayer {

    /** Duration over which to lerp from previous to current position (nanoseconds). */
    private static final long LERP_DURATION_NS = 100_000_000L; // 100 ms

    private final byte playerId;
    private final String name;
    private volatile short x, y, z;
    private volatile byte yaw, pitch;

    // Time-based interpolation: previous position + arrival timestamp
    private short prevX, prevY, prevZ;
    private volatile long updateTimeNanos;

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
        this.updateTimeNanos = System.nanoTime();
    }

    /**
     * Update position from a server packet.
     * Saves the old position and records the arrival time for smooth
     * time-based interpolation on the render thread.
     */
    public void updatePosition(short x, short y, short z, byte yaw, byte pitch) {
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.updateTimeNanos = System.nanoTime();
    }

    /**
     * Get the render X position in block coordinates, smoothly interpolated
     * over {@link #LERP_DURATION_NS} since the last network update.
     */
    public float getRenderX() {
        float t = lerpFactor();
        return (prevX + (x - prevX) * t) / 32.0f;
    }

    public float getRenderY() {
        float t = lerpFactor();
        return (prevY + (y - prevY) * t) / 32.0f;
    }

    public float getRenderZ() {
        float t = lerpFactor();
        return (prevZ + (z - prevZ) * t) / 32.0f;
    }

    private float lerpFactor() {
        long elapsed = System.nanoTime() - updateTimeNanos;
        if (elapsed >= LERP_DURATION_NS) return 1.0f;
        if (elapsed <= 0) return 0.0f;
        return (float) elapsed / LERP_DURATION_NS;
    }

    public byte getPlayerId() { return playerId; }
    public String getName() { return name; }
    public short getX() { return x; }
    public short getY() { return y; }
    public short getZ() { return z; }
    public byte getYaw() { return yaw; }
    public byte getPitch() { return pitch; }
}
