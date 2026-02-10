package com.github.martinambrus.rdforward.protocol.packet;

import io.netty.buffer.ByteBuf;

/**
 * Base interface for all network packets.
 *
 * Each packet knows how to serialize itself to and from a ByteBuf.
 * The packet type ID is written/read separately by the codec layer,
 * so implementations only handle their own fields.
 */
public interface Packet {

    /**
     * Returns the type of this packet.
     */
    PacketType getType();

    /**
     * Write this packet's fields to the buffer.
     */
    void write(ByteBuf buf);

    /**
     * Read this packet's fields from the buffer.
     */
    void read(ByteBuf buf);
}
