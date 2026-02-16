package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0xCD (Client -> Server): Client Statuses.
 *
 * Replaces Login C2S (payload=0) and Respawn C2S (payload=1).
 *
 * Wire format:
 *   [byte] payload (0 = initial spawn / ready to play, 1 = respawn)
 */
public class ClientStatusPacket implements Packet {

    public static final byte INITIAL_SPAWN = 0;
    public static final byte RESPAWN = 1;

    private byte payload;

    public ClientStatusPacket() {}

    @Override
    public int getPacketId() {
        return 0xCD;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(payload);
    }

    @Override
    public void read(ByteBuf buf) {
        payload = buf.readByte();
    }

    public byte getPayload() { return payload; }
}
