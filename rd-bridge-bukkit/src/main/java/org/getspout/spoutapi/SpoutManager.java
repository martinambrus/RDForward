// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.getspout.spoutapi;

import org.getspout.spoutapi.packet.PacketManager;

/** Empty stub of SpoutPlugin's {@code SpoutManager} singleton accessor.
 *  Returns a no-op {@link PacketManager} so plugins (Administrate 1.x's
 *  {@code AdminPacketManager}) that walk
 *  {@code SpoutManager.getPacketManager().addListener(...)} chain
 *  through without NPE. */
public class SpoutManager {
    private static final PacketManager PACKET_MANAGER = new PacketManager();
    public static PacketManager getPacketManager() { return PACKET_MANAGER; }
}
