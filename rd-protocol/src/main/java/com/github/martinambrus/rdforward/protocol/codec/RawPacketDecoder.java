package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.PacketRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private volatile boolean useString16 = false;
    private volatile boolean skipUnknownPackets = false;

    // Temporary debug flag — set true to log all inbound packet IDs.
    private volatile boolean debugLog = false;

    /** Per-connection packet factory overrides, keyed by packet ID. */
    private final Map<Integer, PacketRegistry.PacketFactory> packetOverrides = new HashMap<>();

    public RawPacketDecoder(PacketDirection readDirection, ProtocolVersion protocolVersion) {
        this.readDirection = readDirection;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        McDataTypes.STRING16_MODE.set(useString16);
        try {
        while (in.readableBytes() > 0) {
            in.markReaderIndex();

            int packetId = in.readUnsignedByte();

            // Check per-connection overrides first, then fall back to global registry
            PacketRegistry.PacketFactory override = packetOverrides.get(packetId);
            Packet packet = override != null ? override.create()
                    : PacketRegistry.createPacket(protocolVersion, readDirection, packetId);
            if (packet == null) {
                if (skipUnknownPackets) {
                    in.skipBytes(in.readableBytes());
                    return;
                }
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

            if (debugLog) {
                System.err.println("[C2S] 0x" + Integer.toHexString(packetId)
                        + " " + packet.getClass().getSimpleName());
            }

            try {
                packet.read(in);
            } catch (IndexOutOfBoundsException e) {
                // Not enough data yet — wait for more bytes
                in.resetReaderIndex();
                return;
            } catch (Exception e) {
                // Dump hex context for debugging
                in.resetReaderIndex();
                int readable = Math.min(in.readableBytes(), 64);
                StringBuilder hex = new StringBuilder();
                for (int i = 0; i < readable; i++) {
                    hex.append(String.format("%02x ", in.getByte(in.readerIndex() + i)));
                }
                System.err.println("RawPacketDecoder: error reading "
                        + packet.getClass().getSimpleName() + ": " + e.getMessage()
                        + " | hex: " + hex.toString().trim());
                ctx.close();
                return;
            }

            out.add(packet);
        }
        } finally {
            McDataTypes.STRING16_MODE.remove();
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

    public void setUseString16(boolean useString16) {
        this.useString16 = useString16;
    }

    /**
     * When true, unknown packet IDs are silently skipped instead of closing the
     * connection. Only safe when used after a frame decoder (e.g. LengthFieldBasedFrameDecoder)
     * that guarantees each decode() call processes exactly one complete packet.
     */
    public void setSkipUnknownPackets(boolean skip) {
        this.skipUnknownPackets = skip;
    }

    public void setDebugLog(boolean debugLog) {
        this.debugLog = debugLog;
    }

    /**
     * Override the packet factory for a specific packet ID on this connection only.
     * Takes priority over the global PacketRegistry.
     */
    public void overridePacket(int packetId, PacketRegistry.PacketFactory factory) {
        packetOverrides.put(packetId, factory);
    }
}
