package com.github.martinambrus.rdforward.protocol.packet;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Sent by the server in response to a client's HandshakePacket.
 *
 * Contains the server's protocol version and the negotiated
 * set of active capabilities (the intersection of what both
 * client and server support).
 */
public class HandshakeResponsePacket implements Packet {

    private int serverProtocolVersion;
    private List<Integer> activeCapabilityIds;

    public HandshakeResponsePacket() {
        this.activeCapabilityIds = new ArrayList<Integer>();
    }

    public HandshakeResponsePacket(int serverProtocolVersion, List<Integer> activeCapabilityIds) {
        this.serverProtocolVersion = serverProtocolVersion;
        this.activeCapabilityIds = activeCapabilityIds;
    }

    @Override
    public PacketType getType() {
        return PacketType.HANDSHAKE_RESPONSE;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(serverProtocolVersion);
        buf.writeShort(activeCapabilityIds.size());
        for (int capId : activeCapabilityIds) {
            buf.writeInt(capId);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        serverProtocolVersion = buf.readInt();
        int capCount = buf.readUnsignedShort();
        activeCapabilityIds = new ArrayList<Integer>(capCount);
        for (int i = 0; i < capCount; i++) {
            activeCapabilityIds.add(buf.readInt());
        }
    }

    public int getServerProtocolVersion() {
        return serverProtocolVersion;
    }

    public List<Integer> getActiveCapabilityIds() {
        return activeCapabilityIds;
    }
}
