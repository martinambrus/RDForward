package com.github.martinambrus.rdforward.api.world;

/**
 * Immutable world location with position and rotation.
 *
 * <p>Y is in the RDForward internal convention (eye-level for players).
 * Yaw/pitch in degrees: yaw 0 = North (Classic convention), pitch 0 = horizontal.
 *
 * @param world  world name this location belongs to (may be null for world-less positions)
 * @param x      block-space x coordinate
 * @param y      block-space y coordinate (eye-level for players)
 * @param z      block-space z coordinate
 * @param yaw    rotation around y-axis in degrees
 * @param pitch  rotation around x-axis in degrees (-90..90)
 */
public record Location(String world, double x, double y, double z, float yaw, float pitch) {

    public Location(double x, double y, double z) {
        this(null, x, y, z, 0f, 0f);
    }

    public Location(double x, double y, double z, float yaw, float pitch) {
        this(null, x, y, z, yaw, pitch);
    }

    public int blockX() { return (int) Math.floor(x); }
    public int blockY() { return (int) Math.floor(y); }
    public int blockZ() { return (int) Math.floor(z); }
}
