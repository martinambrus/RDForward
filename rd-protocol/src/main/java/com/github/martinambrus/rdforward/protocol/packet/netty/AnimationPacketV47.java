package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x0A: Animation.
 *
 * 1.8 changed this to an empty packet (no entityId or animationType).
 */
public class AnimationPacketV47 implements Packet {

    public AnimationPacketV47() {}

    @Override
    public int getPacketId() { return 0x0A; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {}
}
