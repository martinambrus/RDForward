// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.vault;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Default Vault {@code Permission} provider registered by the Bukkit
 * bridge when no real Vault.jar (and no real perms-Vault adapter like
 * LuckPerms) is present. The concrete {@code has(CommandSender, ...)}
 * and {@code has(Player, ...)} dispatchers in {@link Permission} already
 * delegate to {@link org.bukkit.command.CommandSender#hasPermission}
 * which routes through rd-api's permission registry, so OP and per-node
 * checks (Bananas's {@code permission.has(player, "bananas.bypass")})
 * answer correctly without any extra wiring.
 *
 * <p>The {@code playerHas(String world, String player, String permission)}
 * surface is best-effort: if the player is online we delegate to their
 * {@code hasPermission}, otherwise we report {@code false} (no offline
 * permission store on the bridge). Group / world / transient methods
 * report a {@link StubCallLog#logOnce} once per signature and return
 * {@code false}.
 */
public final class BridgeStubPermission extends Permission {

    private static final String PLUGIN = "Vault";

    public BridgeStubPermission() {
        this.name = "RDForwardStubPermission";
    }

    @Override public String getName() { return name; }
    @Override public boolean isEnabled() { return true; }
    @Override public boolean hasSuperPermsCompat() { return true; }
    @Override public boolean hasGroupSupport() { return false; }

    @Override
    public boolean playerHas(String world, String player, String permission) {
        if (player == null || permission == null) return false;
        Player online = Bukkit.getServer() == null ? null : Bukkit.getServer().getPlayer(player);
        return online != null && online.hasPermission(permission);
    }

    @Override
    public boolean playerHas(String world, OfflinePlayer player, String permission) {
        if (player == null || permission == null) return false;
        Player online = Bukkit.getServer() == null ? null : Bukkit.getServer().getPlayer(player.getName());
        return online != null && online.hasPermission(permission);
    }

    @Override public boolean playerAdd(String world, String player, String permission) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerAdd"); return false;
    }
    @Override public boolean playerAdd(String world, OfflinePlayer player, String permission) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerAdd"); return false;
    }

    @Override public boolean playerRemove(String world, String player, String permission) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerRemove"); return false;
    }
    @Override public boolean playerRemove(String world, OfflinePlayer player, String permission) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerRemove"); return false;
    }

    @Override public boolean groupHas(String world, String group, String permission) { return false; }
    @Override public boolean groupAdd(String world, String group, String permission) {
        StubCallLog.logOnce(PLUGIN, "Permission.groupAdd"); return false;
    }
    @Override public boolean groupRemove(String world, String group, String permission) {
        StubCallLog.logOnce(PLUGIN, "Permission.groupRemove"); return false;
    }

    @Override public boolean playerInGroup(String world, String player, String group) { return false; }
    @Override public boolean playerInGroup(String world, OfflinePlayer player, String group) { return false; }

    @Override public boolean playerAddGroup(String world, String player, String group) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerAddGroup"); return false;
    }
    @Override public boolean playerAddGroup(String world, OfflinePlayer player, String group) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerAddGroup"); return false;
    }
    @Override public boolean playerRemoveGroup(String world, String player, String group) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerRemoveGroup"); return false;
    }
    @Override public boolean playerRemoveGroup(String world, OfflinePlayer player, String group) {
        StubCallLog.logOnce(PLUGIN, "Permission.playerRemoveGroup"); return false;
    }

    @Override public String[] getPlayerGroups(String world, String player) { return new String[0]; }
    @Override public String[] getPlayerGroups(String world, OfflinePlayer player) { return new String[0]; }

    @Override public String getPrimaryGroup(String world, String player) { return null; }
    @Override public String getPrimaryGroup(String world, OfflinePlayer player) { return null; }

    @Override public String[] getGroups() { return new String[0]; }
}
