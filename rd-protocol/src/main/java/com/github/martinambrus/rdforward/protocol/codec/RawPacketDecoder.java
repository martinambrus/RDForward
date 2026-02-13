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
 * Decodes raw Minecraft protocol packets (no length prefix).
 *
 * Real MC Classic/Alpha clients send packets as [1 byte packetId][payload]
 * with no length prefix. Each packet type has a fixed or self-describing
 * layout, so the decoder relies on {@link Packet#read(ByteBuf)} to consume
 * exactly the right number of bytes.
 *
 * If not enough bytes are available, the read will throw
 * {@link IndexOutOfBoundsException}, and we reset the reader index to
 * wait for more data.
 */
public class RawPacketDecoder extends ByteToMessageDecoder {

    private final PacketDirection readDirection;
    private volatile ProtocolVersion protocolVersion;

    public RawPacketDecoder(PacketDirection readDirection, ProtocolVersion protocolVersion) {
        this.readDirection = readDirection;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (in.readableBytes() > 0) {
            in.markReaderIndex();

            int packetId = in.readUnsignedByte();

            Packet packet = PacketRegistry.createPacket(protocolVersion, readDirection, packetId);
            if (packet == null) {
                // Unknown packet ID — dump context bytes for debugging
                int readable = Math.min(in.readableBytes(), 32);
                StringBuilder hex = new StringBuilder();
                for (int i = 0; i < readable; i++) {
                    hex.append(String.format("%02x ", in.getByte(in.readerIndex() + i)));
                }
                System.err.println("RawPacketDecoder: unknown packet ID 0x"
                        + Integer.toHexString(packetId) + " for " + protocolVersion
                        + " " + readDirection + ", next " + readable
                        + " bytes: " + hex.toString().trim()
                        + ", closing connection");
                in.resetReaderIndex();
                ctx.close();
                return;
            }

            try {
                packet.read(in);
            } catch (IndexOutOfBoundsException e) {
                // Not enough data yet — wait for more bytes
                in.resetReaderIndex();
                return;
            } catch (Exception e) {
                System.err.println("RawPacketDecoder: error reading "
                        + packet.getClass().getSimpleName() + ": " + e.getMessage());
                in.resetReaderIndex();
                ctx.close();
                return;
            }

            out.add(packet);
        }
    }

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
