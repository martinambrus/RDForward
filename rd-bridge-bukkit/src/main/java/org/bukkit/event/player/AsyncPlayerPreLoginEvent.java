// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Stub of Bukkit's {@code AsyncPlayerPreLoginEvent}. Hand-tuned to
 * actually round-trip its fields so plugin code that reads them
 * (notably LuckPerms's {@code BukkitConnectionListener.onPlayerPreLogin}
 * which calls {@code e.getUniqueId()}, {@code e.getName()},
 * {@code e.getLoginResult()}) sees real values.
 *
 * <p>The result enum lives in {@link AsyncPlayerPreLoginEvent$Result}
 * (auto-generated under the {@code $}-bridged binary name so plugin
 * bytecode that references {@code AsyncPlayerPreLoginEvent$Result}
 * directly links). We reuse it instead of declaring a nested
 * {@code Result} enum to avoid a duplicate-class definition.
 *
 * <p>The default result is {@link AsyncPlayerPreLoginEvent$Result#ALLOWED}
 * — RDForward has already authenticated the player by the time the
 * bridge fires this event, so the pre-login check should not block by
 * default. Plugins can still call {@link #disallow} to deny the
 * connection (LuckPerms sets {@code KICK_OTHER} on a database load
 * failure).
 */
public class AsyncPlayerPreLoginEvent extends org.bukkit.event.Event {

    private final String name;
    private final InetAddress address;
    private final UUID uniqueId;
    private AsyncPlayerPreLoginEvent$Result result = AsyncPlayerPreLoginEvent$Result.ALLOWED;
    private String kickMessage = "";

    public AsyncPlayerPreLoginEvent(String name, InetAddress address) {
        this(name, address, null);
    }

    public AsyncPlayerPreLoginEvent(String name, InetAddress address, UUID uniqueId) {
        this.name = name;
        this.address = address;
        this.uniqueId = uniqueId;
    }

    public AsyncPlayerPreLoginEvent(String name, InetAddress address, UUID uniqueId, boolean unused) {
        this(name, address, uniqueId);
    }

    public AsyncPlayerPreLoginEvent(String name, InetAddress address, UUID uniqueId, boolean unused,
                                    com.destroystokyo.paper.profile.PlayerProfile unused2) {
        this(name, address, uniqueId);
    }

    public AsyncPlayerPreLoginEvent(String name, InetAddress address, InetAddress rawAddress,
                                    UUID uniqueId, boolean unused,
                                    com.destroystokyo.paper.profile.PlayerProfile unused2) {
        this(name, address, uniqueId);
    }

    public AsyncPlayerPreLoginEvent(String name, InetAddress address, InetAddress rawAddress,
                                    UUID uniqueId, boolean unused,
                                    com.destroystokyo.paper.profile.PlayerProfile unused2,
                                    String hostname,
                                    io.papermc.paper.connection.PlayerLoginConnection unused3) {
        this(name, address, uniqueId);
    }

    public AsyncPlayerPreLoginEvent() {
        this(null, null, null);
    }

    public String getName() { return name; }
    public InetAddress getAddress() { return address; }
    public InetAddress getRawAddress() { return address; }
    public UUID getUniqueId() { return uniqueId; }

    public AsyncPlayerPreLoginEvent$Result getLoginResult() { return result; }

    public PlayerPreLoginEvent$Result getResult() {
        if (result == null) return null;
        try { return PlayerPreLoginEvent$Result.valueOf(result.name()); }
        catch (IllegalArgumentException e) { return null; }
    }

    public void setLoginResult(AsyncPlayerPreLoginEvent$Result newResult) {
        this.result = newResult == null ? AsyncPlayerPreLoginEvent$Result.ALLOWED : newResult;
    }

    public void setResult(PlayerPreLoginEvent$Result newResult) {
        if (newResult == null) { this.result = AsyncPlayerPreLoginEvent$Result.ALLOWED; return; }
        try { this.result = AsyncPlayerPreLoginEvent$Result.valueOf(newResult.name()); }
        catch (IllegalArgumentException e) { this.result = AsyncPlayerPreLoginEvent$Result.KICK_OTHER; }
    }

    public String getKickMessage() { return kickMessage; }
    public void setKickMessage(String message) { this.kickMessage = message == null ? "" : message; }

    public net.kyori.adventure.text.Component kickMessage() { return null; }
    public void kickMessage(net.kyori.adventure.text.Component component) { /* component facade is a stub */ }

    public void allow() {
        this.result = AsyncPlayerPreLoginEvent$Result.ALLOWED;
        this.kickMessage = "";
    }

    public void disallow(AsyncPlayerPreLoginEvent$Result newResult, String message) {
        this.result = newResult == null ? AsyncPlayerPreLoginEvent$Result.KICK_OTHER : newResult;
        this.kickMessage = message == null ? "" : message;
    }

    public void disallow(AsyncPlayerPreLoginEvent$Result newResult, net.kyori.adventure.text.Component message) {
        this.result = newResult == null ? AsyncPlayerPreLoginEvent$Result.KICK_OTHER : newResult;
    }

    public void disallow(PlayerPreLoginEvent$Result newResult, String message) {
        setResult(newResult);
        this.kickMessage = message == null ? "" : message;
    }

    public void disallow(PlayerPreLoginEvent$Result newResult, net.kyori.adventure.text.Component message) {
        setResult(newResult);
    }

    public com.destroystokyo.paper.profile.PlayerProfile getPlayerProfile() { return null; }
    public void setPlayerProfile(com.destroystokyo.paper.profile.PlayerProfile profile) { /* no-op */ }
    public String getHostname() { return ""; }
    public boolean isTransferred() { return false; }
    public io.papermc.paper.connection.PlayerLoginConnection getConnection() { return null; }

    public org.bukkit.event.HandlerList getHandlers() { return null; }
    public static org.bukkit.event.HandlerList getHandlerList() { return null; }
}
