// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.ban;

import com.github.martinambrus.rdforward.server.api.BanManager;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.BanList$Type;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link BanList} backed by RDForward's {@link BanManager}. Returned by
 * {@code Server.getBanList(BanList.Type)} so legacy plugins (Essentials's
 * {@code /banip}, {@code /unbanip}, {@code /ban}, {@code /pardon}) link
 * against modern Bukkit's ban API and route through the same flat-file
 * store the server already uses for join-time enforcement.
 *
 * <p>The {@link BanList$Type#PROFILE PROFILE} type — added in 1.20+ for
 * UUID-keyed bans — is treated as {@link BanList$Type#NAME NAME} since
 * RDForward does not model offline UUIDs separately from names.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class BridgeBanList implements BanList {

    private final BanList$Type type;

    public BridgeBanList(BanList$Type type) {
        this.type = type;
    }

    @Override
    public BanEntry getBanEntry(String target) {
        if (target == null || !contains(target)) return null;
        return new BridgeBanEntry(type, target);
    }

    @Override
    public BanEntry getBanEntry(Object target) {
        return getBanEntry(toKey(target));
    }

    @Override
    public BanEntry addBan(String target, String reason, Date expires, String source) {
        addInternal(target);
        return new BridgeBanEntry(type, target);
    }

    @Override
    public BanEntry addBan(Object target, String reason, Date expires, String source) {
        return addBan(toKey(target), reason, expires, source);
    }

    @Override
    public BanEntry addBan(Object target, String reason, Instant expires, String source) {
        return addBan(toKey(target), reason, (Date) null, source);
    }

    @Override
    public BanEntry addBan(Object target, String reason, Duration duration, String source) {
        return addBan(toKey(target), reason, (Date) null, source);
    }

    @Override
    public Set getBanEntries() {
        Set<BanEntry> out = new HashSet<>();
        for (String t : currentTargets()) out.add(new BridgeBanEntry(type, t));
        return out;
    }

    @Override
    public Set getEntries() {
        return getBanEntries();
    }

    @Override
    public boolean isBanned(Object target) {
        return contains(toKey(target));
    }

    @Override
    public boolean isBanned(String target) {
        return contains(target);
    }

    @Override
    public void pardon(Object target) {
        pardon(toKey(target));
    }

    @Override
    public void pardon(String target) {
        if (target == null) return;
        if (type == BanList$Type.IP) {
            BanManager.unbanIp(target);
        } else {
            BanManager.unbanPlayer(target);
        }
    }

    private boolean contains(String target) {
        if (target == null) return false;
        if (type == BanList$Type.IP) return BanManager.isIpBanned(target);
        return BanManager.isPlayerBanned(target);
    }

    private void addInternal(String target) {
        if (target == null) return;
        if (type == BanList$Type.IP) BanManager.banIp(target);
        else BanManager.banPlayer(target);
    }

    private Set<String> currentTargets() {
        return type == BanList$Type.IP
                ? BanManager.getBannedIps()
                : BanManager.getBannedPlayers();
    }

    /** Resolve the {@code Object}-typed target overloads down to the
     *  string key our BanManager stores. Bukkit's modern overloads accept
     *  {@code OfflinePlayer}, {@code PlayerProfile}, {@code InetAddress},
     *  or {@code String}; we read whichever is most likely to match the
     *  flat-file representation. */
    private static String toKey(Object target) {
        if (target == null) return null;
        if (target instanceof String s) return s;
        if (target instanceof org.bukkit.OfflinePlayer p) return p.getName();
        if (target instanceof java.net.InetAddress a) return a.getHostAddress();
        return target.toString();
    }
}
