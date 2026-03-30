package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alphaver protocol 0x32 (Server -> Client): Pre-Chunk.
 *
 * Same as standard Alpha PreChunkPacket, but when mode=true (load),
 * the Alphaver client expects additional heightmap data:
 *   [float]     min height
 *   [float]     max height
 *   [byte[1536]] normalized height values (16x16x6, decoded via min/max)
 *
 * When mode=false (unload), the format is identical to standard Alpha.
 *
 * Wire format (mode=true, 1553 bytes payload):
 *   [int]        chunk X
 *   [int]        chunk Z
 *   [byte]       mode (1)
 *   [float]      min height
 *   [float]      max height
 *   [byte[1536]] height data
 *
 * Wire format (mode=false, 9 bytes payload):
 *   [int]        chunk X
 *   [int]        chunk Z
 *   [byte]       mode (0)
 */
public class PreChunkPacketAlphaver implements Packet {

    private int chunkX;
    private int chunkZ;
    private boolean load;
    private float minHeight;
    private float maxHeight;
    private byte[] heightData;

    public PreChunkPacketAlphaver() {}

    public PreChunkPacketAlphaver(int chunkX, int chunkZ, boolean load) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.load = load;
    }

    public PreChunkPacketAlphaver(int chunkX, int chunkZ, boolean load,
                                  float minHeight, float maxHeight, byte[] heightData) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.load = load;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.heightData = heightData;
    }

    @Override
    public int getPacketId() {
        return 0x32;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeBoolean(load);
        if (load) {
            buf.writeFloat(minHeight);
            buf.writeFloat(maxHeight);
            if (heightData != null && heightData.length == 1536) {
                buf.writeBytes(heightData);
            } else {
                // Fallback: write 1536 zero bytes (flat terrain)
                buf.writeZero(1536);
            }
        }
    }

    @Override
    public void read(ByteBuf buf) {
        chunkX = buf.readInt();
        chunkZ = buf.readInt();
        load = buf.readBoolean();
        if (load) {
            minHeight = buf.readFloat();
            maxHeight = buf.readFloat();
            heightData = new byte[1536];
            buf.readBytes(heightData);
        }
    }

    public int getChunkX() { return chunkX; }
    public int getChunkZ() { return chunkZ; }
    public boolean isLoad() { return load; }
    public float getMinHeight() { return minHeight; }
    public float getMaxHeight() { return maxHeight; }
    public byte[] getHeightData() { return heightData; }
}
