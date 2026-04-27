// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.player;

import org.bukkit.entity.Player;

import java.net.InetAddress;

/**
 * Stub of Bukkit's {@code PlayerLoginEvent}. Hand-tuned to round-trip
 * its fields so plugin code that reads them — notably LuckPerms's
 * {@code BukkitConnectionListener.onPlayerLogin} which calls
 * {@code e.getPlayer()} and gates on {@code e.getResult()} before
 * injecting its {@code LuckPermsPermissible} into the player — sees
 * real values.
 *
 * <p>Default result is {@link PlayerLoginEvent$Result#ALLOWED}: by the
 * time RDForward fires this event the player has already authenticated
 * and joined the world; plugins can still call {@link #disallow} to
 * deny the connection (LP does so when its data load failed).
 */
public class PlayerLoginEvent extends PlayerEvent {

    private final InetAddress address;
    private final InetAddress realAddress;
    private final String hostname;
    private PlayerLoginEvent$Result result = PlayerLoginEvent$Result.ALLOWED;
    private String kickMessage = "";

    public PlayerLoginEvent(Player player, String hostname, InetAddress address) {
        this(player, hostname, address, address);
    }

    public PlayerLoginEvent(Player player, String hostname, InetAddress address, InetAddress realAddress) {
        super(player);
        this.hostname = hostname == null ? "" : hostname;
        this.address = address;
        this.realAddress = realAddress == null ? address : realAddress;
    }

    public PlayerLoginEvent(Player player, String hostname, InetAddress address,
                            PlayerLoginEvent$Result result, String message,
                            InetAddress realAddress) {
        this(player, hostname, address, realAddress);
        this.result = result == null ? PlayerLoginEvent$Result.ALLOWED : result;
        this.kickMessage = message == null ? "" : message;
    }

    public PlayerLoginEvent(Player player, String hostname, InetAddress address,
                            PlayerLoginEvent$Result result,
                            net.kyori.adventure.text.Component message,
                            InetAddress realAddress) {
        this(player, hostname, address, realAddress);
        this.result = result == null ? PlayerLoginEvent$Result.ALLOWED : result;
    }

    public PlayerLoginEvent() {
        super((Player) null);
        this.address = null;
        this.realAddress = null;
        this.hostname = "";
    }

    public String getHostname() { return hostname; }
    public InetAddress getAddress() { return address; }
    public InetAddress getRealAddress() { return realAddress; }

    public PlayerLoginEvent$Result getResult() { return result; }
    public void setResult(PlayerLoginEvent$Result newResult) {
        this.result = newResult == null ? PlayerLoginEvent$Result.ALLOWED : newResult;
    }

    public String getKickMessage() { return kickMessage; }
    public void setKickMessage(String message) { this.kickMessage = message == null ? "" : message; }

    public net.kyori.adventure.text.Component kickMessage() { return null; }
    public void kickMessage(net.kyori.adventure.text.Component component) { /* component facade is a stub */ }

    public void allow() {
        this.result = PlayerLoginEvent$Result.ALLOWED;
        this.kickMessage = "";
    }

    public void disallow(PlayerLoginEvent$Result newResult, String message) {
        this.result = newResult == null ? PlayerLoginEvent$Result.KICK_OTHER : newResult;
        this.kickMessage = message == null ? "" : message;
    }

    public void disallow(PlayerLoginEvent$Result newResult, net.kyori.adventure.text.Component message) {
        this.result = newResult == null ? PlayerLoginEvent$Result.KICK_OTHER : newResult;
    }

    public org.bukkit.event.HandlerList getHandlers() { return null; }
    public static org.bukkit.event.HandlerList getHandlerList() { return null; }
}
