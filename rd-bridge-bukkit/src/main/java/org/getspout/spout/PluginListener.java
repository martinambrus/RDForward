// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.getspout.spout;

/**
 * Empty stub of the discontinued Spout server's {@code PluginListener}
 * base class. Plugins compiled against the SpoutPlugin SDK (e.g.
 * Administrate 1.x's {@code AdminPluginListener}) extend this for
 * Spout-side plugin enable/disable hooks. RDForward has no Spout
 * integration; the class exists only so plugin bytecode resolves at
 * link time. Subclass methods that actually fire are still wired
 * through Bukkit's {@code PluginEnableEvent} / {@code PluginDisableEvent}.
 */
public class PluginListener {
}
