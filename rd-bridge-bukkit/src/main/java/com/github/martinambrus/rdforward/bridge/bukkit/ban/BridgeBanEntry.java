// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.ban;

import com.github.martinambrus.rdforward.server.api.BanManager;
import org.bukkit.BanEntry;
import org.bukkit.BanList$Type;

import java.util.Date;

/**
 * Minimal {@link BanEntry} carrying just the target string. RDForward
 * stores bans as flat lines (no reason / expiration / source metadata),
 * so the metadata accessors return defaults and the mutators no-op —
 * matches the experience of running an unmodded vanilla server with a
 * plain {@code banned-ips.txt} / {@code banned-players.txt}.
 */
public final class BridgeBanEntry implements BanEntry {

    private final BanList$Type type;
    private final String target;

    BridgeBanEntry(BanList$Type type, String target) {
        this.type = type;
        this.target = target;
    }

    @Override public String getTarget() { return target; }
    @Override public Object getBanTarget() { return target; }
    @Override public Date getCreated() { return null; }
    @Override public void setCreated(Date arg0) {}
    @Override public String getSource() { return ""; }
    @Override public void setSource(String arg0) {}
    @Override public Date getExpiration() { return null; }
    @Override public void setExpiration(Date arg0) {}
    @Override public String getReason() { return ""; }
    @Override public void setReason(String arg0) {}
    @Override public void save() {}

    /** Pardon == remove from the underlying flat file. Essentials only
     *  invokes this through {@link org.bukkit.BanList#pardon}, but the
     *  contract is documented on BanEntry too. */
    @Override
    public void remove() {
        if (target == null) return;
        if (type == BanList$Type.IP) BanManager.unbanIp(target);
        else BanManager.unbanPlayer(target);
    }
}
