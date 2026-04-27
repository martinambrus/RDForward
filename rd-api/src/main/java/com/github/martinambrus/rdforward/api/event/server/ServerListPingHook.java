package com.github.martinambrus.rdforward.api.event.server;

import java.net.InetAddress;
import java.util.List;

/**
 * Fired before the host serialises a server-list ping (banner / status)
 * response so listeners can mutate what the pinging client sees:
 * {@link PingContext#playerNames} drives the displayed player count,
 * {@link PingContext#maxPlayers}, {@link PingContext#motd}.
 *
 * <p>The Bukkit bridge installs the canonical listener that wraps the
 * context in a Bukkit-shaped {@code ServerListPingEvent} and dispatches
 * it. VanishNoPacket and similar plugins iterate
 * {@code event.iterator()} and call {@code remove()} on each vanished
 * player; the iterator is backed by {@link PingContext#playerNames} so
 * removals are visible here when the listener returns.
 *
 * <p>Listener chain semantics: every listener sees the same mutable
 * context. There is no return value — the final state of the context
 * after the last listener returns is what the host serialises.
 */
@FunctionalInterface
public interface ServerListPingHook {
    void onPing(PingContext ctx);

    /**
     * Mutable holder threaded through every {@link ServerListPingHook}
     * listener. The host owns the list/holders; listeners read and mutate.
     */
    final class PingContext {
        public final InetAddress address;
        /** Mutable list of online player display names. Listeners may
         *  remove entries (e.g. vanished players). The host uses
         *  {@code playerNames.size()} as the final ping count. */
        public final List<String> playerNames;
        /** Initial value is the host's configured slot count. Listeners
         *  may overwrite. */
        public int maxPlayers;
        /** Initial value is the host's MOTD. Listeners may rewrite. */
        public String motd;

        public PingContext(InetAddress address, List<String> playerNames,
                           int maxPlayers, String motd) {
            this.address = address;
            this.playerNames = playerNames;
            this.maxPlayers = maxPlayers;
            this.motd = motd;
        }
    }
}
