package com.github.martinambrus.rdforward.protocol.packet.classic;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Classic protocol 0x03 (Server -> Client): Level Data Chunk.
 *
 * Contains a 1024-byte chunk of GZip-compressed world data.
 * The world data is the block array in XZY order, prefixed with
 * a 4-byte big-endian int (total block count), then GZip compressed,
 * and split into 1024-byte chunks sent via multiple packets.
 *
 * Wire format (1027 bytes payload):
 *   [2 bytes] chunk length (number of valid bytes in the data array)
 *   [1024 bytes] chunk data (null-padded if less than 1024 bytes valid)
 *   [1 byte] percent complete (0-100)
 */
public class LevelDataChunkPacket implements Packet {

    private int chunkLength;
    private byte[] chunkData;
    private int percentComplete;

    public LevelDataChunkPacket() {}

    public LevelDataChunkPacket(int chunkLength, byte[] chunkData, int percentComplete) {
        this.chunkLength = chunkLength;
        this.chunkData = chunkData;
        this.percentComplete = percentComplete;
    }

    @Override
    public int getPacketId() {
        return 0x03;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(chunkLength);
        McDataTypes.writeClassicByteArray(buf, chunkData);
        buf.writeByte(percentComplete);
    }

    @Override
    public void read(ByteBuf buf) {
        chunkLength = buf.readShort();
        chunkData = McDataTypes.readClassicByteArray(buf);
        percentComplete = buf.readUnsignedByte();
    }

    public int getChunkLength() { return chunkLength; }
    public byte[] getChunkData() { return chunkData; }
    public int getPercentComplete() { return percentComplete; }
}
