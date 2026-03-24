package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.Packet;

/**
 * Wrapper for chunk packets that may be computed asynchronously.
 *
 * When a chunk needs to be sent and the packet cache has no entry,
 * a FutureChunkPackets is created and placed in the cache immediately.
 * Serialization is submitted to the generation pool and sets the ready
 * flag + packets array on completion. Callers check {@link #isReady()}
 * before sending; non-ready entries stay in the player's pending queue
 * for the next tick.
 *
 * Thread safety: {@code ready} is volatile, ensuring the happens-before
 * relationship between the serialization thread writing {@code packets}
 * and the tick thread reading them after observing {@code ready == true}.
 */
public final class FutureChunkPackets {

    /** Sentinel instance for invalidated cache entries. */
    public static final FutureChunkPackets EMPTY = new FutureChunkPackets();

    private volatile boolean ready;
    private Packet[] packets;
    private volatile boolean invalidated;

    /** Create an empty future (not yet ready). */
    public FutureChunkPackets() {
        this.ready = false;
    }

    /** Create an already-ready future with the given packets. */
    public FutureChunkPackets(Packet[] packets) {
        this.packets = packets;
        this.ready = true;
    }

    /** Check if serialization is complete and packets are available. */
    public boolean isReady() { return ready; }

    /** Check if this entry was invalidated (e.g. by block change). */
    public boolean isInvalidated() { return invalidated; }

    /** Get the serialized packets. Only valid when {@link #isReady()} is true. */
    public Packet[] getPackets() { return packets; }

    /**
     * Complete this future with the serialized packets.
     * Sets packets first, then the volatile ready flag to ensure
     * visibility ordering.
     */
    public void complete(Packet[] packets) {
        this.packets = packets;
        this.ready = true; // volatile write — publishes packets array
    }

    /**
     * Mark this entry as invalidated. In-flight serialization will check
     * this flag and discard results.
     */
    public void invalidate() {
        this.invalidated = true;
    }
}
