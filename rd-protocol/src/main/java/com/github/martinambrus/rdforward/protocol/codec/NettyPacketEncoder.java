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
 */
public class NettyPacketEncoder extends MessageToByteEncoder<Packet> {

    private volatile ConnectionState connectionState;

    public NettyPacketEncoder(ConnectionState initialState) {
        this.connectionState = initialState;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
        int packetId;
        try {
            packetId = NettyPacketRegistry.getPacketId(connectionState,
                    PacketDirection.SERVER_TO_CLIENT, packet.getClass());
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
}
