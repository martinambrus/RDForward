package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x04 (Server -> Client): Level Finalize.
 *
 * Sent after all LevelDataChunk packets to indicate the world
 * transfer is complete. Contains the world dimensions.
 *
 * Wire format (6 bytes payload):
 *   [2 bytes] X size (world width)
 *   [2 bytes] Y size (world height)
 *   [2 bytes] Z size (world depth)
 */
public class LevelFinalizePacket implements Packet {

    private int xSize;
    private int ySize;
    private int zSize;

    public LevelFinalizePacket() {}

    public LevelFinalizePacket(int xSize, int ySize, int zSize) {
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
    }

    @Override
    public int getPacketId() {
        return 0x04;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(xSize);
        buf.writeShort(ySize);
        buf.writeShort(zSize);
    }

    @Override
    public void read(ByteBuf buf) {
        xSize = buf.readShort();
        ySize = buf.readShort();
        zSize = buf.readShort();
    }

    public int getXSize() { return xSize; }
    public int getYSize() { return ySize; }
    public int getZSize() { return zSize; }
}
