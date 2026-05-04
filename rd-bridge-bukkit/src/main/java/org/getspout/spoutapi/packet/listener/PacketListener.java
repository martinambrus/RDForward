// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.getspout.spoutapi.packet.listener;

/**
 * Empty stub of the discontinued SpoutPlugin SDK's {@code PacketListener}
 * interface. Plugins compiled against SpoutPlugin (Administrate 1.x's
 * {@code AdminPacketListener}) implement this; RDForward never invokes
 * the callback, but the interface must exist so plugin bytecode resolves
 * at link time.
 */
public interface PacketListener {
    boolean checkPacket(org.bukkit.entity.Player player,
                        org.getspout.spoutapi.packet.standard.MCPacket packet);
}
