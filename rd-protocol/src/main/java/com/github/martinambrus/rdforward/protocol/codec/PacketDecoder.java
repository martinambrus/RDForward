package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.packet.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Decodes bytes from the network into Packet objects.
 *
 * Expects the wire format produced by PacketEncoder:
 *   [4 bytes] total packet length
 *   [1 byte]  packet type ID
 *   [N bytes] packet payload
 *
 * Unknown packet type IDs are logged and skipped (forward compat).
 */
public class PacketDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // Need at least 4 bytes for the length prefix
        if (in.readableBytes() < 4) {
            return;
        }

        in.markReaderIndex();
        int length = in.readInt();

        // Wait until the full packet has arrived
        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        // Read the packet type ID
        int packetTypeId = in.readUnsignedByte();
        PacketType type = PacketType.fromId(packetTypeId);

        if (type == null) {
            // Unknown packet type — skip it (forward compatibility)
            in.skipBytes(length - 1);
            return;
        }

        Packet packet = createPacket(type);
        if (packet == null) {
            in.skipBytes(length - 1);
            return;
        }

        // Slice the payload so the packet only reads its own bytes
        ByteBuf payload = in.readSlice(length - 1);
        packet.read(payload);
        out.add(packet);
    }

    /**
     * Factory method to instantiate the correct Packet class
     * based on the packet type.
     */
    private Packet createPacket(PacketType type) {
        switch (type) {
            case HANDSHAKE:
                return new HandshakePacket();
            case HANDSHAKE_RESPONSE:
                return new HandshakeResponsePacket();
            case BLOCK_CHANGE:
                return new BlockChangePacket();
            case PLAYER_POSITION:
                return new PlayerPositionPacket();
            case CHAT_MESSAGE:
                return new ChatMessagePacket();
            default:
                return null;
        }
    }
}
