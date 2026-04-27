// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.event.server;

/**
 * Hand-tuned (was auto-generated) {@code ServerListPingEvent} backed by
 * a live mutable player-name list. The Bukkit bridge constructs it from
 * the rd-api {@link com.github.martinambrus.rdforward.api.event.server.ServerListPingHook.PingContext}
 * and shares the SAME list, so VanishNoPacket-style plugins that call
 * {@code event.iterator()} and {@code remove()} on each vanished player
 * actually drop those names from the host's ping count.
 *
 * <p>Iterator semantics match Bukkit: {@link #iterator()} returns the
 * underlying list's iterator, supporting {@link java.util.Iterator#remove()}.
 * {@link #setMotd}, {@link #setMaxPlayers} write back to the same fields
 * the bridge hands to the host.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ServerListPingEvent extends org.bukkit.event.server.ServerEvent
        implements java.lang.Iterable<org.bukkit.entity.Player> {

    private final java.lang.String hostname;
    private final java.net.InetAddress address;
    private java.lang.String motd;
    private int maxPlayers;
    private final java.util.List<java.lang.String> playerNames;

    /** Bridge-only ctor: shares the {@code playerNames} list with the
     *  caller so {@link java.util.Iterator#remove()} on this event's
     *  iterator mutates the same list the host reads back. */
    public ServerListPingEvent(java.net.InetAddress address,
                               java.util.List<java.lang.String> playerNames,
                               int maxPlayers,
                               java.lang.String motd) {
        this.hostname = address == null ? null : address.getHostAddress();
        this.address = address;
        this.playerNames = playerNames != null ? playerNames : new java.util.ArrayList<>();
        this.maxPlayers = maxPlayers;
        this.motd = motd != null ? motd : "";
    }

    public ServerListPingEvent(java.lang.String hostname, java.net.InetAddress address,
                               java.lang.String motd, int numPlayers, int maxPlayers) {
        this.hostname = hostname;
        this.address = address;
        this.motd = motd != null ? motd : "";
        this.maxPlayers = maxPlayers;
        this.playerNames = new java.util.ArrayList<>();
        // Pre-seed with synthetic placeholder names so iterator-based plugins
        // that iterate AND remove (Vanish) still see numPlayers entries.
        for (int i = 0; i < numPlayers; i++) {
            this.playerNames.add("Player" + (i + 1));
        }
    }

    protected ServerListPingEvent(java.lang.String hostname, java.net.InetAddress address,
                                  java.lang.String motd, int numPlayers) {
        this(hostname, address, motd, numPlayers, numPlayers);
    }

    public ServerListPingEvent(java.net.InetAddress address,
                               net.kyori.adventure.text.Component motd,
                               int numPlayers, int maxPlayers) {
        this(null, address, motd == null ? "" : motd.toString(), numPlayers, maxPlayers);
    }

    public ServerListPingEvent(java.lang.String hostname, java.net.InetAddress address,
                               net.kyori.adventure.text.Component motd,
                               int numPlayers, int maxPlayers) {
        this(hostname, address, motd == null ? "" : motd.toString(), numPlayers, maxPlayers);
    }

    protected ServerListPingEvent(java.net.InetAddress address,
                                  net.kyori.adventure.text.Component motd, int numPlayers) {
        this(null, address, motd == null ? "" : motd.toString(), numPlayers, numPlayers);
    }

    protected ServerListPingEvent(java.lang.String hostname, java.net.InetAddress address,
                                  net.kyori.adventure.text.Component motd, int numPlayers) {
        this(hostname, address, motd == null ? "" : motd.toString(), numPlayers, numPlayers);
    }

    public ServerListPingEvent() {
        this(null, null, "", 0, 0);
    }

    public java.lang.String getHostname() {
        return hostname;
    }

    public java.net.InetAddress getAddress() {
        return address;
    }

    public net.kyori.adventure.text.Component motd() {
        return null;
    }

    public void motd(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null,
                "org.bukkit.event.server.ServerListPingEvent.motd(Lnet/kyori/adventure/text/Component;)V");
    }

    public java.lang.String getMotd() {
        return motd;
    }

    public void setMotd(java.lang.String arg0) {
        this.motd = arg0 != null ? arg0 : "";
    }

    public int getNumPlayers() {
        return playerNames.size();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int arg0) {
        this.maxPlayers = arg0;
    }

    public boolean shouldSendChatPreviews() {
        return false;
    }

    public void setServerIcon(org.bukkit.util.CachedServerIcon arg0)
            throws java.lang.IllegalArgumentException, java.lang.UnsupportedOperationException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null,
                "org.bukkit.event.server.ServerListPingEvent.setServerIcon(Lorg/bukkit/util/CachedServerIcon;)V");
    }

    /** Iterator over the live player-name list, backed by a transient
     *  {@code Player} proxy view. Removing through this iterator drops
     *  the corresponding name from the underlying list — VanishNoPacket
     *  walks the iterator, calls {@code Player.hasPermission("vanish.see")}
     *  on each entry, and removes vanished players one by one. */
    public java.util.Iterator<org.bukkit.entity.Player> iterator()
            throws java.lang.UnsupportedOperationException {
        java.util.Iterator<java.lang.String> backing = playerNames.iterator();
        return new java.util.Iterator<>() {
            @Override public boolean hasNext() { return backing.hasNext(); }
            @Override public org.bukkit.entity.Player next() {
                return com.github.martinambrus.rdforward.bridge.bukkit.BukkitPlayer.create(backing.next());
            }
            @Override public void remove() { backing.remove(); }
        };
    }

    public org.bukkit.event.HandlerList getHandlers() {
        return null;
    }

    public static org.bukkit.event.HandlerList getHandlerList() {
        return null;
    }
}
