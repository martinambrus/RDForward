package com.github.martinambrus.rdforward.protocol.packet;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Sent by the client as the first packet upon connecting.
 *
 * Contains the client's protocol version and the list of
 * capabilities it supports. The server uses this to:
 * 1. Determine which version translator pipeline to build
 * 2. Compute the active capability set (intersection)
 * 3. Respond with a HandshakeResponsePacket
 */
public class HandshakePacket implements Packet {

    private int protocolVersion;
    private String clientName;
    private List<Integer> capabilityIds;

    public HandshakePacket() {
        this.capabilityIds = new ArrayList<Integer>();
    }

    public HandshakePacket(int protocolVersion, String clientName, List<Integer> capabilityIds) {
        this.protocolVersion = protocolVersion;
        this.clientName = clientName;
        this.capabilityIds = capabilityIds;
    }

    @Override
    public PacketType getType() {
        return PacketType.HANDSHAKE;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(protocolVersion);

        // Write client name as length-prefixed UTF-8
        byte[] nameBytes = clientName.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(nameBytes.length);
        buf.writeBytes(nameBytes);

        // Write capability IDs
        buf.writeShort(capabilityIds.size());
        for (int capId : capabilityIds) {
            buf.writeInt(capId);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        protocolVersion = buf.readInt();

        // Read client name
        int nameLength = buf.readUnsignedShort();
        byte[] nameBytes = new byte[nameLength];
        buf.readBytes(nameBytes);
        clientName = new String(nameBytes, StandardCharsets.UTF_8);

        // Read capability IDs
        int capCount = buf.readUnsignedShort();
        capabilityIds = new ArrayList<Integer>(capCount);
        for (int i = 0; i < capCount; i++) {
            capabilityIds.add(buf.readInt());
        }
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public String getClientName() {
        return clientName;
    }

    public List<Integer> getCapabilityIds() {
        return capabilityIds;
    }
}
