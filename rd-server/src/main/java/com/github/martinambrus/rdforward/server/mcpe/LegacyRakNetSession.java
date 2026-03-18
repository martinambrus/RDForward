package com.github.martinambrus.rdforward.server.mcpe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

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

    /** Session timeout in ms — disconnect if no packets received for this long. */
    public static final long SESSION_TIMEOUT_MS = 10_000;

    // Outgoing sequence number for data packets
    private int sendSequenceNumber = 0;
    // Outgoing reliable message index
    private int sendReliableIndex = 0;
    // Outgoing ordering index (channel 0)
    private int sendOrderingIndex = 0;
    // Split packet ID counter (separate from sequence numbers)
    private int sendSplitId = 0;

    // Incoming: highest received sequence number (for ACKs)
    private int receiveSequenceNumber = -1;

    // Split packet reassembly: splitId -> (splitIndex -> data)
    private final Map<Integer, SplitAssembly> splitAssemblies = new HashMap<>();

    // Game handler (set after connected handshake)
    private MCPELoginHandler loginHandler;
    private MCPEGameplayHandler gameplayHandler;

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
        return System.currentTimeMillis() - lastPacketTime > SESSION_TIMEOUT_MS;
    }

    public MCPELoginHandler getLoginHandler() { return loginHandler; }
    public void setLoginHandler(MCPELoginHandler handler) { this.loginHandler = handler; }

    public MCPEGameplayHandler getGameplayHandler() { return gameplayHandler; }
    public void setGameplayHandler(MCPEGameplayHandler handler) { this.gameplayHandler = handler; }

    /** Allocate the next send sequence number. */
    public int nextSendSequenceNumber() {
        return sendSequenceNumber++;
    }

    /** Allocate the next reliable message index. */
    public int nextReliableIndex() {
        return sendReliableIndex++;
    }

    /** Allocate the next ordering index. */
    public int nextOrderingIndex() {
        return sendOrderingIndex++;
    }

    /** Allocate the next split packet ID. */
    public int nextSplitId() {
        return sendSplitId++;
    }

    /** Update the highest received sequence number and return it for ACK. */
    public int acknowledgeSequence(int seqNum) {
        if (seqNum > receiveSequenceNumber) {
            receiveSequenceNumber = seqNum;
        }
        return seqNum;
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
    }

    private static class SplitAssembly {
        final int splitCount;
        final Map<Integer, byte[]> fragments = new HashMap<>();

        SplitAssembly(int splitCount) {
            this.splitCount = splitCount;
        }
    }
}
