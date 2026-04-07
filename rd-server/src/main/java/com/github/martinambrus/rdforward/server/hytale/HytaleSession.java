package com.github.martinambrus.rdforward.server.hytale;

import io.netty.buffer.ByteBuf;
import io.netty.incubator.codec.quic.QuicChannel;
import io.netty.incubator.codec.quic.QuicStreamChannel;
import io.netty.util.AttributeKey;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Low-level session state for a single Hytale QUIC connection.
 *
 * Shared across all QUIC streams of the same connection (parent QuicChannel).
 * Manages stream routing, connection state, keep-alive, and entity ID assignment.
 */
public class HytaleSession {

    /** Attribute key for storing the session on the parent QuicChannel. */
    public static final AttributeKey<HytaleSession> SESSION_KEY =
            AttributeKey.valueOf("hytaleSession");

    /** Connection lifecycle states. */
    public enum State {
        AWAITING_CONNECT,
        AWAITING_AUTH_TOKEN,
        SENDING_REGISTRIES,
        CONNECTED,
        DISCONNECTED
    }

    private final QuicChannel quicChannel;
    private final ConcurrentHashMap<Long, QuicStreamChannel> streams = new ConcurrentHashMap<>();
    private volatile State state = State.AWAITING_CONNECT;
    private volatile String username;
    private volatile java.util.UUID playerUUID;
    private volatile int entityNetworkId;

    /** Maps Hytale channel IDs (0=Default, 1=Chunks, 2=WorldMap) to QUIC streams. */
    private final ConcurrentHashMap<Integer, QuicStreamChannel> channelStreams = new ConcurrentHashMap<>();

    /** Entity ID counter for this session. */
    private static final AtomicInteger entityIdCounter = new AtomicInteger(1);

    /** Last keep-alive ping ID sent by server. */
    private volatile int lastPingId;

    /** Timestamp of last received pong (for timeout detection). */
    private volatile long lastPongTime = System.currentTimeMillis();

    public HytaleSession(QuicChannel quicChannel) {
        this.quicChannel = quicChannel;
        this.entityNetworkId = entityIdCounter.getAndIncrement();
    }

    // -- State management --

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public java.util.UUID getPlayerUUID() { return playerUUID; }
    public void setPlayerUUID(java.util.UUID uuid) { this.playerUUID = uuid; }

    public int getEntityNetworkId() { return entityNetworkId; }

    public QuicChannel getQuicChannel() { return quicChannel; }

    // -- Stream management --

    /** Register a QUIC stream by its stream ID. */
    public void registerStream(long streamId, QuicStreamChannel channel) {
        streams.put(streamId, channel);
    }

    /** Register a QUIC stream as a specific Hytale channel (0=Default, 1=Chunks, 2=WorldMap). */
    public void setChannelStream(int channel, QuicStreamChannel stream) {
        channelStreams.put(channel, stream);
    }

    /** Get the QUIC stream assigned to a specific Hytale channel. */
    public QuicStreamChannel getChannelStream(int channel) {
        return channelStreams.get(channel);
    }

    /** Get the first available stream for sending (Default stream). */
    public QuicStreamChannel getDefaultStream() {
        // Return any registered stream — in practice, the first bidirectional stream
        // opened by the client becomes the default stream
        for (QuicStreamChannel ch : streams.values()) {
            if (ch.isActive()) return ch;
        }
        return null;
    }

    /**
     * Send a packet on the default stream.
     * Creates an HytalePacketBuffer, encodes via the pipeline's HytaleFrameCodec.
     */
    public void sendPacket(HytalePacketBuffer packet) {
        QuicStreamChannel stream = getDefaultStream();
        if (stream != null && stream.isActive()) {
            stream.writeAndFlush(packet.retain());
        } else {
            packet.release();
        }
    }

    /**
     * Buffer a packet on the default stream without flushing.
     * Call {@link #flush()} after the last packet to send the batch.
     */
    public void sendPacketBuffered(HytalePacketBuffer packet) {
        QuicStreamChannel stream = getDefaultStream();
        if (stream != null && stream.isActive()) {
            stream.write(packet.retain());
        } else {
            packet.release();
        }
    }

    /** Flush all buffered packets on the default stream. */
    public void flush() {
        QuicStreamChannel stream = getDefaultStream();
        if (stream != null && stream.isActive()) {
            stream.flush();
        }
    }

    /**
     * Send a packet on a specific Hytale channel (0=Default, 1=Chunks, 2=WorldMap).
     * Falls back to default stream if the channel's stream is not available.
     */
    public void sendPacketOnChannel(HytalePacketBuffer packet, int channel) {
        QuicStreamChannel stream = channelStreams.get(channel);
        if (stream == null || !stream.isActive()) {
            stream = getDefaultStream();
        }
        if (stream != null && stream.isActive()) {
            stream.writeAndFlush(packet.retain());
        } else {
            packet.release();
        }
    }

    /**
     * Send a packet on a specific stream.
     * Falls back to default stream if the requested stream is not available.
     */
    public void sendPacketOnStream(HytalePacketBuffer packet, long streamId) {
        QuicStreamChannel stream = streams.get(streamId);
        if (stream == null || !stream.isActive()) {
            stream = getDefaultStream();
        }
        if (stream != null && stream.isActive()) {
            stream.writeAndFlush(packet.retain());
        } else {
            packet.release();
        }
    }

    // -- Keep-alive --

    public int getLastPingId() { return lastPingId; }
    public void setLastPingId(int id) { this.lastPingId = id; }
    public long getLastPongTime() { return lastPongTime; }
    public void setLastPongTime(long time) { this.lastPongTime = time; }

    // -- Disconnect --

    /**
     * Send a ServerDisconnect (ID 2) and close the QUIC connection.
     *
     * ServerDisconnect wire format:
     *   [0]  byte    nullBits (bit0 = reason FormattedMessage present)
     *   [1]  byte    DisconnectType (0=Disconnect, 1=Crash)
     *   Inline: FormattedMessage (if bit0 set)
     *
     * For simplicity, we send without a reason (nullBits=0) to avoid
     * the FormattedMessage complexity on disconnect.
     */
    public void disconnect(String reason) {
        if (state == State.DISCONNECTED) return;
        state = State.DISCONNECTED;

        QuicStreamChannel stream = getDefaultStream();
        if (stream != null && stream.isActive()) {
            HytalePacketBuffer pkt = HytalePacketBuffer.create(
                    HytaleProtocolConstants.PACKET_SERVER_DISCONNECT, stream.alloc());
            pkt.writeByte(0); // nullBits: no reason (avoids FormattedMessage serialization)
            pkt.writeByte(0); // DisconnectType: Disconnect (0)
            stream.writeAndFlush(pkt).addListener(f -> quicChannel.close());
        } else {
            quicChannel.close();
        }
    }

    /** Called when the QUIC connection drops. */
    public void onDisconnect() {
        state = State.DISCONNECTED;
        streams.clear();
    }

    public boolean isActive() {
        return state != State.DISCONNECTED && quicChannel.isActive();
    }
}
