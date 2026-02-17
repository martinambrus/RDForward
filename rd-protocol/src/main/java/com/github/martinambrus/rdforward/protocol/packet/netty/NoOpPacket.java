package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * A no-op packet that reads and writes nothing.
 *
 * Used to register C2S packet IDs for packets the server doesn't handle
 * (e.g., Recipe Book, Advancements). The frame-based decoder discards
 * any unread bytes in the frame, so empty read() is safe.
 */
public class NoOpPacket implements Packet {

    @Override
    public int getPacketId() { return -1; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {}
}
