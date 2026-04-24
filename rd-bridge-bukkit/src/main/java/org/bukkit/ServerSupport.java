// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;

/**
 * Holder for process-wide defaults shared by {@link Server}'s default
 * methods. Exists because an interface cannot hold non-constant instance
 * state, and the stub {@link SimpleServicesManager} carries no useful
 * state so one singleton is sufficient for every adapter.
 */
final class ServerSupport {

    static final ServicesManager SERVICES = new SimpleServicesManager();

    private ServerSupport() {}
}
