package org.bukkit.entity;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

/**
 * Bukkit-shaped Player facade. Instances carry a name plus an optional
 * backing rd-api {@link com.github.martinambrus.rdforward.api.player.Player}
 * wired in by
 * {@link com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayerAdapter}.
 *
 * <p>When a backing rd-api player is present, {@link #sendMessage(String)},
 * {@link #kickPlayer(String)}, {@link #teleport(Location)} and location
 * reads flow through. Otherwise they silently noop — matches the stub
 * contract documented on the bridge.
 */
public class Player implements CommandSender {

    private final String name;
    private com.github.martinambrus.rdforward.api.player.Player backing;
    private World world;

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, com.github.martinambrus.rdforward.api.player.Player backing, World world) {
        this.name = name;
        this.backing = backing;
        this.world = world;
    }

    @Override public String getName() { return name; }
    public String getDisplayName() { return name; }

    public World getWorld() { return world; }
    public void setWorld(World world) { this.world = world; }

    /** @return backing rd-api player (may be null in unit tests / synthetic events). */
    public com.github.martinambrus.rdforward.api.player.Player backing() { return backing; }

    public Location getLocation() {
        if (backing == null) return new Location(world, 0, 0, 0);
        com.github.martinambrus.rdforward.api.world.Location loc = backing.getLocation();
        if (loc == null) return new Location(world, 0, 0, 0);
        return new Location(world, loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch());
    }

    public boolean teleport(Location location) {
        if (backing == null || location == null) return false;
        backing.teleport(new com.github.martinambrus.rdforward.api.world.Location(
                location.getWorld() == null ? null : location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch()));
        return true;
    }

    @Override
    public void sendMessage(String message) {
        if (backing != null) backing.sendMessage(message);
    }

    public void kickPlayer(String reason) {
        if (backing != null) backing.kick(reason);
    }

    @Override public boolean isOp() { return backing != null && backing.isOp(); }

    /** Stub — RDForward does not expose health state. Always returns {@code 20.0}. */
    public double getHealth() { return 20.0; }

    /** Stub — RDForward does not expose inventory. Returns null. */
    public Object getInventory() { return null; }

    @Override public String toString() { return "Player[" + name + "]"; }
}
