package com.github.martinambrus.rdforward.server.hytale;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Read/write helper for Hytale's little-endian wire format.
 *
 * Hytale packets use a split fixed/variable block design:
 * - Fixed block: fields at known byte offsets (all LE)
 * - Variable block: strings, byte arrays, nested structs referenced by
 *   int32LE offset pointers in the fixed block
 * - Null bits: leading byte(s) as bitmask indicating which nullable fields
 *   are present
 *
 * All multi-byte integers/floats are little-endian except UUIDs (big-endian).
 * VarInts are 7-bit unsigned encoding (max 5 bytes, no negative values).
 */
public class HytalePacketBuffer {

    private final ByteBuf buf;
    private final int packetId;

    public HytalePacketBuffer(int packetId, ByteBuf buf) {
        this.packetId = packetId;
        this.buf = buf;
    }

    /** Create a writable buffer for building an outbound packet. */
    public static HytalePacketBuffer create(int packetId, ByteBufAllocator alloc) {
        return new HytalePacketBuffer(packetId, alloc.buffer());
    }

    /** Create a writable buffer with initial capacity. */
    public static HytalePacketBuffer create(int packetId, ByteBufAllocator alloc, int initialCapacity) {
        return new HytalePacketBuffer(packetId, alloc.buffer(initialCapacity));
    }

    /** Create a writable buffer backed by an unpooled heap buffer. */
    public static HytalePacketBuffer create(int packetId) {
        return new HytalePacketBuffer(packetId, Unpooled.buffer());
    }

    public int getPacketId() { return packetId; }
    public ByteBuf getBuf() { return buf; }
    public int readableBytes() { return buf.readableBytes(); }
    public int readerIndex() { return buf.readerIndex(); }
    public void readerIndex(int index) { buf.readerIndex(index); }
    public int writerIndex() { return buf.writerIndex(); }

    // -- Read helpers (little-endian) --

    public byte readByte() { return buf.readByte(); }
    public int readUnsignedByte() { return buf.readUnsignedByte(); }
    public short readShortLE() { return buf.readShortLE(); }
    public int readIntLE() { return buf.readIntLE(); }
    public long readLongLE() { return buf.readLongLE(); }
    public float readFloatLE() { return Float.intBitsToFloat(buf.readIntLE()); }
    public double readDoubleLE() { return Double.longBitsToDouble(buf.readLongLE()); }
    public boolean readBoolean() { return buf.readBoolean(); }

    /** Read IEEE 754 half-precision float (2 bytes LE). */
    public float readHalfFloatLE() {
        int bits = buf.readUnsignedShortLE();
        int sign = (bits >> 15) & 1;
        int exp = (bits >> 10) & 0x1F;
        int mantissa = bits & 0x3FF;

        if (exp == 0) {
            // Subnormal or zero
            return (float) ((sign == 0 ? 1 : -1) * Math.pow(2, -14) * (mantissa / 1024.0));
        } else if (exp == 0x1F) {
            // Inf or NaN
            return mantissa == 0
                    ? (sign == 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY)
                    : Float.NaN;
        }
        return (float) ((sign == 0 ? 1 : -1) * Math.pow(2, exp - 15) * (1 + mantissa / 1024.0));
    }

    /** Read UUID (big-endian, exception to LE convention). */
    public UUID readUUID() {
        long msb = buf.readLong(); // big-endian
        long lsb = buf.readLong(); // big-endian
        return new UUID(msb, lsb);
    }

    /**
     * Read unsigned VarInt (7-bit encoding, max 5 bytes).
     * Hytale VarInts are unsigned-only — negative values are not supported.
     */
    public int readVarInt() {
        int value = 0;
        int shift = 0;
        byte b;
        do {
            if (shift >= 35) {
                throw new RuntimeException("VarInt too long (>5 bytes)");
            }
            b = buf.readByte();
            value |= (b & 0x7F) << shift;
            shift += 7;
        } while ((b & 0x80) != 0);
        return value;
    }

    /** Read a VarInt-length-prefixed UTF-8 string. */
    public String readString() {
        int length = readVarInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Read a VarInt-length-prefixed ASCII string. */
    public String readAsciiString() {
        int length = readVarInt();
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /** Read fixed-length null-padded ASCII string. */
    public String readFixedAscii(int length) {
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        // Find null terminator
        int end = length;
        for (int i = 0; i < length; i++) {
            if (bytes[i] == 0) { end = i; break; }
        }
        return new String(bytes, 0, end, StandardCharsets.US_ASCII);
    }

    /** Read VarInt-length-prefixed byte array. */
    public byte[] readByteArray() {
        int length = readVarInt();
        byte[] data = new byte[length];
        buf.readBytes(data);
        return data;
    }

    /** Read raw bytes into a new array. */
    public byte[] readBytes(int count) {
        byte[] data = new byte[count];
        buf.readBytes(data);
        return data;
    }

    // -- Write helpers (little-endian) --

    public void writeByte(int value) { buf.writeByte(value); }
    public void writeShortLE(int value) { buf.writeShortLE(value); }
    public void writeIntLE(int value) { buf.writeIntLE(value); }
    public void writeLongLE(long value) { buf.writeLongLE(value); }
    public void writeFloatLE(float value) { buf.writeIntLE(Float.floatToRawIntBits(value)); }
    public void writeDoubleLE(double value) { buf.writeLongLE(Double.doubleToRawLongBits(value)); }
    public void writeBoolean(boolean value) { buf.writeBoolean(value); }

    /** Write IEEE 754 half-precision float (2 bytes LE). */
    public void writeHalfFloatLE(float value) {
        int fbits = Float.floatToRawIntBits(value);
        int sign = (fbits >> 16) & 0x8000;
        int val = (fbits & 0x7FFFFFFF) + 0x1000; // round

        if (val >= 0x47800000) {
            // Overflow to infinity
            buf.writeShortLE(sign | 0x7C00);
        } else if (val < 0x38800000) {
            // Subnormal
            int mant = (fbits & 0x007FFFFF) | 0x00800000;
            int shift = 113 - (val >> 23);
            mant >>= shift;
            buf.writeShortLE(sign | (mant >> 13));
        } else {
            buf.writeShortLE(sign | ((val - 0x38000000) >> 13));
        }
    }

    /** Write UUID (big-endian). */
    public void writeUUID(UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());  // big-endian
        buf.writeLong(uuid.getLeastSignificantBits()); // big-endian
    }

    /** Write unsigned VarInt (7-bit encoding). */
    public void writeVarInt(int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    /** Write VarInt-length-prefixed UTF-8 string. */
    public void writeString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        buf.writeBytes(bytes);
    }

    /** Write VarInt-length-prefixed ASCII string. */
    public void writeAsciiString(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        writeVarInt(bytes.length);
        buf.writeBytes(bytes);
    }

    /** Write fixed-length null-padded ASCII string. */
    public void writeFixedAscii(String value, int length) {
        byte[] bytes = value.getBytes(StandardCharsets.US_ASCII);
        int toCopy = Math.min(bytes.length, length);
        buf.writeBytes(bytes, 0, toCopy);
        // Pad with nulls
        for (int i = toCopy; i < length; i++) {
            buf.writeByte(0);
        }
    }

    /** Write VarInt-length-prefixed byte array. */
    public void writeByteArray(byte[] data) {
        writeVarInt(data.length);
        buf.writeBytes(data);
    }

    /** Write raw bytes from array. */
    public void writeBytes(byte[] data) {
        buf.writeBytes(data);
    }

    /** Write raw bytes from another ByteBuf. */
    public void writeBytes(ByteBuf src) {
        buf.writeBytes(src);
    }

    /** Write zero bytes for padding. */
    public void writeZeroes(int count) {
        buf.writeZero(count);
    }

    /** Get the current writer index (for offset backpatching). */
    public int markWriterIndex() {
        return buf.writerIndex();
    }

    /** Backpatch an int32LE at a previously saved position. */
    public void setIntLE(int index, int value) {
        buf.setIntLE(index, value);
    }

    /** Release the underlying buffer. */
    public void release() {
        buf.release();
    }

    /** Retain the underlying buffer. */
    public HytalePacketBuffer retain() {
        buf.retain();
        return this;
    }

    public int refCnt() {
        return buf.refCnt();
    }
}
