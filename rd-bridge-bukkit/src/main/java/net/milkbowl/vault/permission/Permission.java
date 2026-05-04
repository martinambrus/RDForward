// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.milkbowl.vault.permission;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Logger;

/**
 * Vault 1.7 Permission contract. Mirrors the upstream abstract class so
 * any plugin compiled against Vault links cleanly: protected
 * {@code plugin}, {@code log}, {@code name} fields, the abstract
 * provider-method set, and the concrete dispatch helpers
 * ({@code has(CommandSender, ...)}, {@code has(Player, ...)}) that
 * upstream implements once.
 *
 * <p>Concrete subclass {@code BridgeStubPermission} delegates the
 * {@code has(CommandSender)} path to {@link CommandSender#hasPermission}
 * which already routes through rd-api's permission registry. Group /
 * world / offline-player methods default to {@code false} or empty —
 * the bridge has no group model to consult.
 */
public abstract class Permission {

    protected Plugin plugin = null;
    public Logger log = null;
    public String name = "?";

    public abstract String getName();
    public abstract boolean isEnabled();
    public abstract boolean hasSuperPermsCompat();

    public abstract boolean playerHas(String world, String player, String permission);
    public abstract boolean playerHas(String world, OfflinePlayer player, String permission);

    public abstract boolean playerAdd(String world, String player, String permission);
    public abstract boolean playerAdd(String world, OfflinePlayer player, String permission);

    public abstract boolean playerRemove(String world, String player, String permission);
    public abstract boolean playerRemove(String world, OfflinePlayer player, String permission);

    public abstract boolean groupHas(String world, String group, String permission);
    public abstract boolean groupAdd(String world, String group, String permission);
    public abstract boolean groupRemove(String world, String group, String permission);

    public abstract boolean playerInGroup(String world, String player, String group);
    public abstract boolean playerInGroup(String world, OfflinePlayer player, String group);

    public abstract boolean playerAddGroup(String world, String player, String group);
    public abstract boolean playerAddGroup(String world, OfflinePlayer player, String group);

    public abstract boolean playerRemoveGroup(String world, String player, String group);
    public abstract boolean playerRemoveGroup(String world, OfflinePlayer player, String group);

    public abstract String[] getPlayerGroups(String world, String player);
    public abstract String[] getPlayerGroups(String world, OfflinePlayer player);

    public abstract String getPrimaryGroup(String world, String player);
    public abstract String getPrimaryGroup(String world, OfflinePlayer player);

    public abstract String[] getGroups();
    public abstract boolean hasGroupSupport();

    public boolean has(World world, String player, String permission) {
        return playerHas(world == null ? null : world.getName(), player, permission);
    }

    public boolean has(String world, String player, String permission) {
        return playerHas(world, player, permission);
    }

    public boolean has(CommandSender sender, String permission) {
        return sender != null && sender.hasPermission(permission);
    }

    public boolean has(Player player, String permission) {
        return player != null && player.hasPermission(permission);
    }

    public boolean playerHas(Player player, String permission) {
        return player != null && player.hasPermission(permission);
    }

    public boolean playerHas(World world, String player, String permission) {
        return playerHas(world == null ? null : world.getName(), player, permission);
    }

    public boolean playerHas(World world, OfflinePlayer player, String permission) {
        return playerHas(world == null ? null : world.getName(), player, permission);
    }

    public boolean playerAdd(Player player, String permission) {
        return player != null && playerAdd((String) null, player.getName(), permission);
    }

    public boolean playerAdd(World world, String player, String permission) {
        return playerAdd(world == null ? null : world.getName(), player, permission);
    }

    public boolean playerAdd(World world, OfflinePlayer player, String permission) {
        return playerAdd(world == null ? null : world.getName(), player, permission);
    }

    public boolean playerRemove(Player player, String permission) {
        return player != null && playerRemove((String) null, player.getName(), permission);
    }

    public boolean playerRemove(World world, String player, String permission) {
        return playerRemove(world == null ? null : world.getName(), player, permission);
    }

    public boolean playerRemove(World world, OfflinePlayer player, String permission) {
        return playerRemove(world == null ? null : world.getName(), player, permission);
    }

    public boolean playerAddTransient(OfflinePlayer player, String permission) { return false; }
    public boolean playerAddTransient(Player player, String permission) { return false; }
    public boolean playerAddTransient(String worldName, OfflinePlayer player, String permission) { return false; }
    public boolean playerAddTransient(String worldName, Player player, String permission) { return false; }
    public boolean playerRemoveTransient(OfflinePlayer player, String permission) { return false; }
    public boolean playerRemoveTransient(Player player, String permission) { return false; }
    public boolean playerRemoveTransient(String worldName, OfflinePlayer player, String permission) { return false; }
    public boolean playerRemoveTransient(String worldName, Player player, String permission) { return false; }

    public boolean playerInGroup(Player player, String group) {
        return player != null && playerInGroup((String) null, player.getName(), group);
    }

    public boolean playerInGroup(World world, String player, String group) {
        return playerInGroup(world == null ? null : world.getName(), player, group);
    }

    public boolean playerInGroup(World world, OfflinePlayer player, String group) {
        return playerInGroup(world == null ? null : world.getName(), player, group);
    }

    public boolean playerAddGroup(Player player, String group) {
        return player != null && playerAddGroup((String) null, player.getName(), group);
    }

    public boolean playerAddGroup(World world, String player, String group) {
        return playerAddGroup(world == null ? null : world.getName(), player, group);
    }

    public boolean playerAddGroup(World world, OfflinePlayer player, String group) {
        return playerAddGroup(world == null ? null : world.getName(), player, group);
    }

    public boolean playerRemoveGroup(Player player, String group) {
        return player != null && playerRemoveGroup((String) null, player.getName(), group);
    }

    public boolean playerRemoveGroup(World world, String player, String group) {
        return playerRemoveGroup(world == null ? null : world.getName(), player, group);
    }

    public boolean playerRemoveGroup(World world, OfflinePlayer player, String group) {
        return playerRemoveGroup(world == null ? null : world.getName(), player, group);
    }

    public String[] getPlayerGroups(Player player) {
        return player == null ? new String[0] : getPlayerGroups((String) null, player.getName());
    }

    public String[] getPlayerGroups(World world, String player) {
        return getPlayerGroups(world == null ? null : world.getName(), player);
    }

    public String[] getPlayerGroups(World world, OfflinePlayer player) {
        return getPlayerGroups(world == null ? null : world.getName(), player);
    }

    public String getPrimaryGroup(Player player) {
        return player == null ? null : getPrimaryGroup((String) null, player.getName());
    }

    public String getPrimaryGroup(World world, String player) {
        return getPrimaryGroup(world == null ? null : world.getName(), player);
    }

    public String getPrimaryGroup(World world, OfflinePlayer player) {
        return getPrimaryGroup(world == null ? null : world.getName(), player);
    }

    @Override
    public String toString() {
        return getName();
    }
}
