package com.github.martinambrus.rdforward.server.mcpe;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

/**
 * Read/write helper for MCPE 0.7.0 packet wire format.
 * All multi-byte integers are Big-Endian unless noted otherwise.
 * Metadata values use Little-Endian.
 */
public class MCPEPacketBuffer {

    private final ByteBuf buf;

    public MCPEPacketBuffer(ByteBuf buf) {
        this.buf = buf;
    }

    public MCPEPacketBuffer() {
        this(Unpooled.buffer(256));
    }

    public ByteBuf getBuf() {
        return buf;
    }

    // --- Writers (Big-Endian) ---

    public MCPEPacketBuffer writeByte(int value) {
        buf.writeByte(value);
        return this;
    }

    public MCPEPacketBuffer writeShort(int value) {
        buf.writeShort(value);
        return this;
    }

    public MCPEPacketBuffer writeInt(int value) {
        buf.writeInt(value);
        return this;
    }

    /** Write an unsigned VarInt (1-5 bytes, LEB128). */
    public MCPEPacketBuffer writeUnsignedVarInt(int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value & 0x7F);
        return this;
    }

    public MCPEPacketBuffer writeLong(long value) {
        buf.writeLong(value);
        return this;
    }

    public MCPEPacketBuffer writeFloat(float value) {
        buf.writeFloat(value);
        return this;
    }

    public MCPEPacketBuffer writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
        return this;
    }

    public MCPEPacketBuffer writeBytes(byte[] data) {
        buf.writeBytes(data);
        return this;
    }

    public MCPEPacketBuffer writeBytes(ByteBuf src) {
        buf.writeBytes(src);
        return this;
    }

    public MCPEPacketBuffer writeBytes(ByteBuf src, int length) {
        buf.writeBytes(src, length);
        return this;
    }

    /** Write a RakNet 3-byte Little-Endian "triad" (24-bit unsigned int). */
    public MCPEPacketBuffer writeTriad(int value) {
        buf.writeByte(value & 0xFF);
        buf.writeByte((value >> 8) & 0xFF);
        buf.writeByte((value >> 16) & 0xFF);
        return this;
    }

    /** Write a Little-Endian short (for metadata values). */
    public MCPEPacketBuffer writeLShort(int value) {
        buf.writeShortLE(value);
        return this;
    }

    /** Write a Little-Endian int (for metadata values). */
    public MCPEPacketBuffer writeLInt(int value) {
        buf.writeIntLE(value);
        return this;
    }

    /** Write a Little-Endian float (for metadata values). */
    public MCPEPacketBuffer writeLFloat(float value) {
        buf.writeFloatLE(value);
        return this;
    }

    /** Write an IPv4 address in RakNet format: family(1) + inverted IP(4) + port(2 BE). */
    public MCPEPacketBuffer writeAddress(java.net.InetSocketAddress address) {
        buf.writeByte(4); // AF_INET
        byte[] ip = address.getAddress().getAddress();
        for (byte b : ip) {
            buf.writeByte(~b & 0xFF); // RakNet inverts IP bytes
        }
        buf.writeShort(address.getPort());
        return this;
    }

    /** Write a "null" system address (0.0.0.0:0). */
    public MCPEPacketBuffer writeNullAddress() {
        buf.writeByte(4); // AF_INET
        buf.writeInt(0);  // 0.0.0.0 inverted = 0xFF.0xFF.0xFF.0xFF, but convention uses 0
        buf.writeShort(0);
        return this;
    }

    // --- Metadata writers ---

    /**
     * Write a single entity metadata entry.
     * Header byte = (type << 5) | (index & 0x1F).
     */
    public MCPEPacketBuffer writeMetaByte(int index, byte value) {
        buf.writeByte((MCPEConstants.META_TYPE_BYTE << 5) | (index & 0x1F));
        buf.writeByte(value);
        return this;
    }

    public MCPEPacketBuffer writeMetaShort(int index, short value) {
        buf.writeByte((MCPEConstants.META_TYPE_SHORT << 5) | (index & 0x1F));
        buf.writeShortLE(value);
        return this;
    }

    public MCPEPacketBuffer writeMetaString(int index, String value) {
        buf.writeByte((MCPEConstants.META_TYPE_STRING << 5) | (index & 0x1F));
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        buf.writeShortLE(bytes.length); // Little-Endian (metadata values are LE per PocketMine Binary::writeMetadata)
        buf.writeBytes(bytes);
        return this;
    }

    public MCPEPacketBuffer writeMetaPosition(int index, int x, int y, int z) {
        buf.writeByte((MCPEConstants.META_TYPE_POSITION << 5) | (index & 0x1F));
        buf.writeIntLE(x);
        buf.writeIntLE(y);
        buf.writeIntLE(z);
        return this;
    }

    public MCPEPacketBuffer writeMetaEnd() {
        buf.writeByte(MCPEConstants.META_TERMINATOR);
        return this;
    }

    // --- Readers (Big-Endian) ---

    public byte readByte() {
        return buf.readByte();
    }

    public int readUnsignedByte() {
        return buf.readUnsignedByte();
    }

    public short readShort() {
        return buf.readShort();
    }

    public int readUnsignedShort() {
        return buf.readUnsignedShort();
    }

    public int readInt() {
        return buf.readInt();
    }

    /** Read a Little-Endian int. */
    public int readLInt() {
        return buf.readIntLE();
    }

    public long readLong() {
        return buf.readLong();
    }

    public float readFloat() {
        return buf.readFloat();
    }

    public String readString() {
        int length = buf.readUnsignedShort();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Read a RakNet 3-byte Little-Endian "triad". */
    public int readTriad() {
        return (buf.readUnsignedByte())
             | (buf.readUnsignedByte() << 8)
             | (buf.readUnsignedByte() << 16);
    }

    /** Read an IPv4 address in RakNet format. */
    public java.net.InetSocketAddress readAddress() {
        int family = buf.readUnsignedByte();
        if (family == 4) {
            byte[] ip = new byte[4];
            for (int i = 0; i < 4; i++) {
                ip[i] = (byte) (~buf.readByte() & 0xFF);
            }
            int port = buf.readUnsignedShort();
            try {
                return new java.net.InetSocketAddress(
                        java.net.InetAddress.getByAddress(ip), port);
            } catch (java.net.UnknownHostException e) {
                return new java.net.InetSocketAddress(port);
            }
        }
        // IPv6 - skip 26 bytes (not used in MCPE 0.7)
        buf.skipBytes(26);
        return new java.net.InetSocketAddress(0);
    }

    public void skipBytes(int count) {
        buf.skipBytes(count);
    }

    public int readableBytes() {
        return buf.readableBytes();
    }

    public boolean isReadable() {
        return buf.isReadable();
    }

    public int readerIndex() {
        return buf.readerIndex();
    }

    public MCPEPacketBuffer readerIndex(int index) {
        buf.readerIndex(index);
        return this;
    }

    public void release() {
        buf.release();
    }

    /** Copy remaining readable bytes into a new byte array. */
    public byte[] readRemainingBytes() {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return data;
    }
}
