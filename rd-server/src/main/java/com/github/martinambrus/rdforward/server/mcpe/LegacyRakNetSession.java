package com.github.martinambrus.rdforward.server.mcpe;

import com.github.martinambrus.rdforward.server.api.ServerProperties;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-client RakNet session state for legacy MCPE (protocol 11).
 * Manages connection state, reliability sequencing, and fragment reassembly.
 */
public class LegacyRakNetSession {

    public enum State {
        /** RakNet handshake (Open Connection Request/Reply) complete, awaiting Connected handshake. */
        CONNECTING,
        /** Connected handshake (0x09/0x10/0x13) complete, game packets active. */
        CONNECTED,
        /** Session closed. */
        DISCONNECTED
    }

    private final InetSocketAddress address;
    private final long clientGuid;
    private final int mtu;
    private final long serverGuid;

    private State state = State.CONNECTING;

    /** Timestamp of last received packet (for timeout detection). */
    private long lastPacketTime = System.currentTimeMillis();

    /** Session timeout in ms — configurable via keep-alive-timeout in server.properties. */
    public static long getSessionTimeoutMs() {
        return ServerProperties.getKeepAliveTimeoutSeconds() * 1000L;
    }

    // Outgoing sequence number for data packets (thread-safe: sendEncapsulated
    // can be called from both the UDP event loop and TCP handler threads)
    private final AtomicInteger sendSequenceNumber = new AtomicInteger(0);
    // Outgoing reliable message index
    private final AtomicInteger sendReliableIndex = new AtomicInteger(0);
    // Outgoing ordering index (channel 0)
    private final AtomicInteger sendOrderingIndex = new AtomicInteger(0);
    // Split packet ID counter (separate from sequence numbers)
    private final AtomicInteger sendSplitId = new AtomicInteger(0);

    // Incoming: highest received sequence number (for ACKs)
    private int receiveSequenceNumber = -1;

    // Split packet reassembly: splitId -> (splitIndex -> data)
    private final Map<Integer, SplitAssembly> splitAssemblies = new HashMap<>();

    // Sent frame buffer for NACK retransmission: seqNum -> raw frame bytes.
    // ConcurrentHashMap because sends may come from TCP handler threads while
    // ACK/NACK processing happens on the UDP event loop.
    private final ConcurrentHashMap<Integer, byte[]> sentFrames = new ConcurrentHashMap<>();

    // MCPE protocol version (0 = unknown/pre-login, set to actual version during login)
    private int mcpeProtocolVersion = 0;

    // Game handler (set after connected handshake)
    private MCPELoginHandler loginHandler;
    private MCPEGameplayHandler gameplayHandler;

    // Cached ChannelHandlerContext from incoming data packets (for outgoing writes).
    // Volatile: read from broadcast threads (TCP handlers), written from UDP event loop.
    private volatile io.netty.channel.ChannelHandlerContext cachedCtx;

    public LegacyRakNetSession(InetSocketAddress address, long clientGuid, int mtu, long serverGuid) {
        this.address = address;
        this.clientGuid = clientGuid;
        this.mtu = mtu;
        this.serverGuid = serverGuid;
    }

    public InetSocketAddress getAddress() { return address; }
    public long getClientGuid() { return clientGuid; }
    public int getMtu() { return mtu; }
    public long getServerGuid() { return serverGuid; }
    public State getState() { return state; }

    public void setState(State state) { this.state = state; }

    /** Call on every received packet to reset the timeout timer. */
    public void touch() { lastPacketTime = System.currentTimeMillis(); }

    /** Check if session has timed out (no packets received within timeout window). */
    public boolean isTimedOut() {
        return System.currentTimeMillis() - lastPacketTime > getSessionTimeoutMs();
    }

    public MCPELoginHandler getLoginHandler() { return loginHandler; }
    public void setLoginHandler(MCPELoginHandler handler) { this.loginHandler = handler; }

    public MCPEGameplayHandler getGameplayHandler() { return gameplayHandler; }
    public void setGameplayHandler(MCPEGameplayHandler handler) { this.gameplayHandler = handler; }

    public int getMcpeProtocolVersion() { return mcpeProtocolVersion; }
    public void setMcpeProtocolVersion(int version) { this.mcpeProtocolVersion = version; }

    public io.netty.channel.ChannelHandlerContext getCachedCtx() { return cachedCtx; }
    public void setCachedCtx(io.netty.channel.ChannelHandlerContext ctx) { this.cachedCtx = ctx; }

    /** Allocate the next send sequence number. */
    public int nextSendSequenceNumber() {
        return sendSequenceNumber.getAndIncrement();
    }

    /** Allocate the next reliable message index. */
    public int nextReliableIndex() {
        return sendReliableIndex.getAndIncrement();
    }

    /** Allocate the next ordering index. */
    public int nextOrderingIndex() {
        return sendOrderingIndex.getAndIncrement();
    }

    /** Allocate the next split packet ID. */
    public int nextSplitId() {
        return sendSplitId.getAndIncrement();
    }

    /** Update the highest received sequence number and return it for ACK. */
    public int acknowledgeSequence(int seqNum) {
        if (seqNum > receiveSequenceNumber) {
            receiveSequenceNumber = seqNum;
        }
        return seqNum;
    }

    /** Store a sent frame for potential NACK retransmission. */
    public void storeSentFrame(int seqNum, byte[] frameBytes) {
        sentFrames.put(seqNum, frameBytes);
        // Limit buffer size to prevent unbounded growth — keep last 512 frames
        if (sentFrames.size() > 512) {
            int oldest = seqNum - 512;
            for (int i = oldest - 16; i <= oldest; i++) {
                sentFrames.remove(i);
            }
        }
    }

    /** Retrieve a stored frame for retransmission. Returns null if not found. */
    public byte[] getSentFrame(int seqNum) {
        return sentFrames.get(seqNum);
    }

    /** Remove acknowledged frames from the retransmission buffer. */
    public void acknowledgeSentFrame(int seqNum) {
        sentFrames.remove(seqNum);
    }

    /** Get all sequence numbers that need retransmission from a NACK range. */
    public List<Integer> getNackedSequences(int start, int end) {
        List<Integer> result = new ArrayList<>();
        for (int seq = start; seq <= end; seq++) {
            if (sentFrames.containsKey(seq)) {
                result.add(seq);
            }
        }
        return result;
    }

    /**
     * Process a split packet fragment. Returns the reassembled ByteBuf when all
     * fragments are received, or null if still waiting for more.
     */
    public ByteBuf handleSplitPacket(int splitId, int splitIndex, int splitCount, ByteBuf fragment) {
        SplitAssembly assembly = splitAssemblies.computeIfAbsent(splitId,
                k -> new SplitAssembly(splitCount));

        byte[] data = new byte[fragment.readableBytes()];
        fragment.readBytes(data);
        assembly.fragments.put(splitIndex, data);

        if (assembly.fragments.size() == assembly.splitCount) {
            splitAssemblies.remove(splitId);
            // Reassemble in order
            ByteBuf result = Unpooled.buffer();
            for (int i = 0; i < assembly.splitCount; i++) {
                byte[] part = assembly.fragments.get(i);
                if (part != null) {
                    result.writeBytes(part);
                }
            }
            return result;
        }
        return null;
    }

    public void close() {
        state = State.DISCONNECTED;
        splitAssemblies.clear();
        sentFrames.clear();
    }

    private static class SplitAssembly {
        final int splitCount;
        final Map<Integer, byte[]> fragments = new HashMap<>();

        SplitAssembly(int splitCount) {
            this.splitCount = splitCount;
        }
    }
}
