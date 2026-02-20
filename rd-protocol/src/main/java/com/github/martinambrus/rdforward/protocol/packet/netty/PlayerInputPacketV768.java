package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.21.2 Play state, C2S packet 0x28: Player Input.
 *
 * Replaces the old Steer Vehicle packet. Wire format changed from
 * float(sideways) + float(forward) + byte(flags) = 9 bytes
 * to just byte(flags) = 1 byte.
 *
 * Flags: 0x01=forward, 0x02=backward, 0x04=left, 0x08=right,
 *        0x10=jump, 0x20=sneak, 0x40=sprint
 */
public class PlayerInputPacketV768 implements Packet {

    public PlayerInputPacketV768() {}

    @Override
    public int getPacketId() { return 0x28; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        buf.skipBytes(1); // flags byte
    }
}
