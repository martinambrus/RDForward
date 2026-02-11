package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Decodes bytes from the network into Packet objects.
 *
 * Uses the PacketRegistry to resolve the correct Packet class based on
 * the connection's protocol version, read direction, and packet ID.
 * This is necessary because Classic and Alpha share packet IDs with
 * different meanings.
 *
 * The protocol version can be updated after handshake if the server
 * negotiates a different version.
 *
 * Wire format (Nati framing):
 *   [4 bytes] total packet length
 *   [1 byte]  packet ID (MC-compatible)
 *   [N bytes] packet payload (MC-compatible)
 */
public class PacketDecoder extends ByteToMessageDecoder {

    /** Direction of packets we're reading (opposite of what we'd send). */
    private final PacketDirection readDirection;

    /** Protocol version of the connection (can be updated post-handshake). */
    private volatile ProtocolVersion protocolVersion;

    /**
     * Create a decoder for a specific direction and version.
     *
     * @param readDirection the direction of packets we'll be decoding
     *   (CLIENT_TO_SERVER for server-side decoder, SERVER_TO_CLIENT for client-side)
     * @param protocolVersion the initial protocol version (can be updated later)
     */
    public PacketDecoder(PacketDirection readDirection, ProtocolVersion protocolVersion) {
        this.readDirection = readDirection;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Need at least 4 bytes for the length prefix
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();
        int length = in.readInt();

        if (length <= 0 || length > 1048576) {
            // Invalid length — skip and close
            in.resetReaderIndex();
            ctx.close();
            return;
        }

        // Wait until the full packet has arrived
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // Read the packet ID
        int packetId = in.readUnsignedByte();

        // Look up the packet in the registry
        Packet packet = PacketRegistry.createPacket(protocolVersion, readDirection, packetId);

        if (packet == null) {
            // Unknown packet — skip it (forward compatibility)
            in.skipBytes(length - 1);
            return;
        }

        // Slice the payload so the packet only reads its own bytes
        ByteBuf payload = in.readSlice(length - 1);
        packet.read(payload);
        out.add(packet);
    }

    /**
     * Update the protocol version for this decoder.
     * Called after handshake when the connection version is determined.
     */
    public void setProtocolVersion(ProtocolVersion version) {
        this.protocolVersion = version;
    }

    public ProtocolVersion getProtocolVersion() {
        return protocolVersion;
    }

    public PacketDirection getReadDirection() {
        return readDirection;
    }
}
