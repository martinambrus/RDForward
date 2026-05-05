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

    /** Resolve the {@link org.bukkit.block.Block} at this location's
     *  block coordinates, via the wrapped {@link World}. CoreProtect's
     *  {@code BlockUtil.gravityScan} walks {@code location.getBlock()}
     *  to detect gravity-affected blocks above a placement; without this
     *  the listener {@link NoSuchMethodError}s on every block place. */
    public org.bukkit.block.Block getBlock() {
        if (world == null) return null;
        return world.getBlockAt(getBlockX(), getBlockY(), getBlockZ());
    }

    /** @return the chunk at this location. AuthMe's
     *  {@code ProcessSyncronousPlayerLogin.teleportBackFromSpawn} calls
     *  {@code location.getChunk()} during post-login teleport. */
    public Chunk getChunk() {
        if (world == null) return null;
        return world.getChunkAt(getBlockX() >> 4, getBlockZ() >> 4);
    }

    /** Snapshot of the (x,y,z) coordinates as a fresh {@link org.bukkit.util.Vector}.
     *  SimpleLogin's {@code LoginListener.onMove} compares vector deltas to
     *  detect movement after auth — it must not return {@code null}. */
    public org.bukkit.util.Vector toVector() {
        return new org.bukkit.util.Vector(x, y, z);
    }

    /** Unit vector pointing along the location's facing — Bukkit's
     *  standard yaw/pitch -> direction formula. Essentials's
     *  {@code Commandfireball} multiplies this by a speed to set the
     *  spawned fireball's velocity; without it /fireball throws
     *  {@link NoSuchMethodError}. Yaw here is Bukkit-convention
     *  (0 = South) — {@code BukkitPlayer.Handler.getLocation} already
     *  converts Classic yaw before constructing the Location. */
    public org.bukkit.util.Vector getDirection() {
        // RDForward's Vector stub has no-op setX/Y/Z (returns this without
        // mutating), so build the components first and hand them to the
        // 3-arg ctor instead of mutating after construction.
        double rotXrad = Math.toRadians(yaw);
        double rotYrad = Math.toRadians(pitch);
        double y = -Math.sin(rotYrad);
        double xz = Math.cos(rotYrad);
        double x = -xz * Math.sin(rotXrad);
        double z = xz * Math.cos(rotXrad);
        return new org.bukkit.util.Vector(x, y, z);
    }

    /** Inverse of {@link #getDirection()} — sets yaw/pitch so the
     *  location faces along {@code v}. Mirrors Bukkit's signature so
     *  plugins that chain {@code loc.setDirection(target.subtract(loc))}
     *  link cleanly. */
    public Location setDirection(org.bukkit.util.Vector v) {
        if (v == null) return this;
        double dx = v.getX(), dy = v.getY(), dz = v.getZ();
        if (dx == 0.0 && dz == 0.0) {
            this.pitch = dy > 0 ? -90.0f : 90.0f;
            return this;
        }
        double xz = Math.sqrt(dx * dx + dz * dz);
        this.yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        this.pitch = (float) Math.toDegrees(Math.atan2(-dy, xz));
        return this;
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
