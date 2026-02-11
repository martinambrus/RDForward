package com.github.martinambrus.rdforward.protocol.packet;

import io.netty.buffer.ByteBuf;

/**
 * Base interface for all network packets.
 *
 * Each packet knows its own ID, which matches the real Minecraft protocol
 * for the version it belongs to. The packet ID is written/read separately
 * by the codec layer — implementations only handle their own fields.
 *
 * Packet IDs are version-specific: the same ID (e.g., 0x00) can mean
 * different things in Classic vs Alpha. The PacketRegistry resolves
 * the correct Packet class based on (version, direction, id).
 */
public interface Packet {

    /**
     * The packet ID as defined by the Minecraft protocol.
     * For Classic packets, this matches wiki.vg Classic Protocol IDs.
     * For Alpha packets, this matches wiki.vg Protocol History IDs.
     */
    int getPacketId();

    /**
     * Write this packet's fields to the buffer.
     * Does NOT write the packet ID — that's the codec's job.
     */
    void write(ByteBuf buf);

    /**
     * Read this packet's fields from the buffer.
     * Does NOT read the packet ID — that's the codec's job.
     */
    void read(ByteBuf buf);
}
