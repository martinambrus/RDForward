package com.github.martinambrus.rdforward.protocol.packet.lce;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * LCE Chunk Visibility packet (ID 50/0x32, S2C).
 *
 * Tells the client to load or unload a chunk. Functionally equivalent to
 * pre-Netty Java's PreChunkPacket.
 *
 * Wire format (9 bytes):
 *   [int]     chunkX
 *   [int]     chunkZ
 *   [boolean] visible (true = load, false = unload)
 */
public class LCEChunkVisibilityPacket implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean visible;

    public LCEChunkVisibilityPacket() {}

    public LCEChunkVisibilityPacket(int chunkX, int chunkZ, boolean visible) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.visible = visible;
    }

    @Override
    public int getPacketId() { return 0x32; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(visible);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        visible = buf.readBoolean();
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public boolean isVisible() { return visible; }
}
