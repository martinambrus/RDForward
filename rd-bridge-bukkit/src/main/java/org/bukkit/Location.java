// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

/**
 * Bukkit-shaped location. Wraps a world reference and six mutable
 * coordinate components — plugins routinely mutate {@code x/y/z} on a
 * {@code Location} object so this class is mutable to match.
 *
 * <p>Conversion to/from rd-api {@code Location} is handled by
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitWorldAdapter}.
 */
public class Location implements Cloneable {

    private World world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public Location(World world, double x, double y, double z) {
        this(world, x, y, z, 0f, 0f);
    }

    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public void setYaw(float yaw) { this.yaw = yaw; }
    public void setPitch(float pitch) { this.pitch = pitch; }

    public int getBlockX() { return (int) Math.floor(x); }
    public int getBlockY() { return (int) Math.floor(y); }
    public int getBlockZ() { return (int) Math.floor(z); }

    /** Snapshot of the (x,y,z) coordinates as a fresh {@link org.bukkit.util.Vector}.
     *  SimpleLogin's {@code LoginListener.onMove} compares vector deltas to
     *  detect movement after auth — it must not return {@code null}. */
    public org.bukkit.util.Vector toVector() {
        return new org.bukkit.util.Vector(x, y, z);
    }

    /** Real Bukkit's {@code Location.add(double,double,double)} mutates
     *  this instance and returns it. VanishNoPacket's
     *  {@code VanishManager.toggleVanishQuiet} calls
     *  {@code player.getLocation().add(0,1,0)} to compute the unvanish
     *  trigger above the player's head; without this overload the
     *  toggle disconnects the client with NoSuchMethodError. */
    public Location add(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
        return this;
    }

    public Location add(Location other) {
        if (other != null) {
            this.x += other.x;
            this.y += other.y;
            this.z += other.z;
        }
        return this;
    }

    public Location add(org.bukkit.util.Vector v) {
        if (v != null) {
            this.x += v.getX();
            this.y += v.getY();
            this.z += v.getZ();
        }
        return this;
    }

    public Location subtract(double dx, double dy, double dz) {
        this.x -= dx;
        this.y -= dy;
        this.z -= dz;
        return this;
    }

    public Location subtract(Location other) {
        if (other != null) {
            this.x -= other.x;
            this.y -= other.y;
            this.z -= other.z;
        }
        return this;
    }

    public Location subtract(org.bukkit.util.Vector v) {
        if (v != null) {
            this.x -= v.getX();
            this.y -= v.getY();
            this.z -= v.getZ();
        }
        return this;
    }

    public double distance(Location other) {
        return Math.sqrt(distanceSquared(other));
    }

    public double distanceSquared(Location other) {
        if (other == null) return 0.0d;
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        double dz = this.z - other.z;
        return dx * dx + dy * dy + dz * dz;
    }

    @Override
    public Location clone() {
        return new Location(world, x, y, z, yaw, pitch);
    }

    @Override
    public String toString() {
        return "Location{world=" + (world == null ? "null" : world.getName())
                + ",x=" + x + ",y=" + y + ",z=" + z + ",yaw=" + yaw + ",pitch=" + pitch + "}";
    }
}
