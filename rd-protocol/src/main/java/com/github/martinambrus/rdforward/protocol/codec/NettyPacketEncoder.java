package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.NettyPacketRegistry;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Encodes Packet objects for the 1.7.2+ Netty protocol.
 *
 * Looks up the packet ID via NettyPacketRegistry reverse map, writes
 * VarInt packetId + packet payload into the output buffer. The buffer
 * is then framed by VarIntFrameEncoder.
 *
 * Version-aware: for v107+ (1.9) clients, uses V109 reverse map where
 * all Play state S2C packet IDs are remapped.
 */
public class NettyPacketEncoder extends MessageToByteEncoder<Packet> {

    private volatile ConnectionState connectionState;
    private final PacketDirection direction;
    private volatile int protocolVersion = 4; // Default to 1.7.2

    public NettyPacketEncoder(ConnectionState initialState) {
        this(initialState, PacketDirection.SERVER_TO_CLIENT);
    }

    public NettyPacketEncoder(ConnectionState initialState, PacketDirection direction) {
        this.connectionState = initialState;
        this.direction = direction;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        int packetId;
        try {
            packetId = NettyPacketRegistry.getPacketId(connectionState,
                    direction, packet.getClass(), protocolVersion);
        } catch (IllegalArgumentException e) {
            System.err.println("NettyPacketEncoder: " + e.getMessage());
            return;
        }

        McDataTypes.writeVarInt(out, packetId);
        packet.write(out);
    }

    public void setConnectionState(ConnectionState state) {
        this.connectionState = state;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }
}
