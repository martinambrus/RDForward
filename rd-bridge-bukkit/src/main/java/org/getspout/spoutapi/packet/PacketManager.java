// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.getspout.spoutapi.packet;

import org.getspout.spoutapi.packet.listener.PacketListener;

/** Empty stub of SpoutPlugin's {@code PacketManager}. RDForward has no
 *  Spout integration; both register methods are no-ops so plugins
 *  (Administrate 1.x's {@code AdminPacketManager}) that subscribe to
 *  packet ids load cleanly without their hooks ever firing. */
public class PacketManager {
    public boolean addListenerInbound(int packetId, PacketListener listener) { return false; }
    public boolean addListenerOutbound(int packetId, PacketListener listener) { return false; }
    public void removeListener(int packetId, PacketListener listener) {}
}
