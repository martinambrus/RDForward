package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.ConnectionState;
import com.github.martinambrus.rdforward.protocol.packet.NettyPacketRegistry;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Decodes VarInt-framed packets for the 1.7.2+ Netty protocol.
 *
 * Receives complete frames from VarIntFrameDecoder. Reads the VarInt packet ID,
 * creates the packet via NettyPacketRegistry based on the current connection state,
 * and calls packet.read().
 *
 * The connection state is updated by NettyConnectionHandler during state transitions.
 */
public class NettyPacketDecoder extends MessageToMessageDecoder<ByteBuf> {

    private volatile ConnectionState connectionState;
    private final PacketDirection direction;
    private volatile int protocolVersion = 4; // Default to 1.7.2
    private boolean lenient; // When true, skip read errors instead of closing connection

    public NettyPacketDecoder(ConnectionState initialState) {
        this(initialState, PacketDirection.CLIENT_TO_SERVER);
    }

    public NettyPacketDecoder(ConnectionState initialState, PacketDirection direction) {
        this.connectionState = initialState;
        this.direction = direction;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        if (msg.readableBytes() < 1) return;

        int packetId = McDataTypes.readVarInt(msg);

        Packet packet = NettyPacketRegistry.createPacket(connectionState,
                direction, packetId, protocolVersion);

        if (packet == null) {
            // Unknown packet — log and skip (frame is already bounded)
            int readable = Math.min(msg.readableBytes(), 32);
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < readable; i++) {
                hex.append(String.format("%02x ", msg.getByte(msg.readerIndex() + i)));
            }
            System.err.println("NettyPacketDecoder: unknown packet ID 0x"
                    + Integer.toHexString(packetId) + " in state " + connectionState
                    + ", next " + readable + " bytes: " + hex.toString().trim());
            return;
        }

        try {
            packet.read(msg);
        } catch (Exception e) {
            System.err.println("NettyPacketDecoder: error reading "
                    + packet.getClass().getSimpleName() + " (0x"
                    + Integer.toHexString(packetId) + "): " + e.getMessage());
            if (!lenient) {
                ctx.close();
            }
            return;
        }

        out.add(packet);
    }

    public void setConnectionState(ConnectionState state) {
        this.connectionState = state;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    public void setProtocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public int getProtocolVersion() {
        return protocolVersion;
    }

    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }
}
