package com.github.martinambrus.rdforward.protocol;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

/**
 * Minecraft-compatible data type read/write utilities.
 *
 * Supports all three wire format eras:
 * - Classic: fixed-size strings (64 bytes, US-ASCII, space-padded),
 *            short coordinates (fixed-point, divide by 32 for block coords),
 *            fixed-size byte arrays (1024 bytes, null-padded)
 * - Alpha/Beta (pre-Netty): string16 (short length prefix + UCS-2/UTF-16BE)
 * - Modern (1.7+): VarInt length prefixes, UTF-8 strings
 *
 * Using the real Minecraft data types ensures our protocol is structurally
 * compatible and can be upgraded to real MC protocol versions.
 */
public final class McDataTypes {

    private static final Charset US_ASCII = Charset.forName("US-ASCII");
    private static final Charset UTF_16BE = Charset.forName("UTF-16BE");

    private McDataTypes() {}

    // ========================================================================
    // Classic data types (c0.0.15a - c0.30, protocol version 7)
    // ========================================================================

    /** Classic strings are always 64 bytes, US-ASCII, padded with spaces (0x20). */
    public static final int CLASSIC_STRING_LENGTH = 64;

    /** Classic byte arrays are always 1024 bytes, padded with null (0x00). */
    public static final int CLASSIC_BYTE_ARRAY_LENGTH = 1024;

    /**
     * Read a Classic fixed-length string (64 bytes, space-padded, US-ASCII).
     */
    public static String readClassicString(ByteBuf buf) {
        byte[] bytes = new byte[CLASSIC_STRING_LENGTH];
        buf.readBytes(bytes);
        // Trim trailing spaces
        int end = CLASSIC_STRING_LENGTH;
        while (end > 0 && bytes[end - 1] == 0x20) {
            end--;
        }
        return new String(bytes, 0, end, US_ASCII);
    }

    /**
     * Write a Classic fixed-length string (64 bytes, space-padded, US-ASCII).
     */
    public static void writeClassicString(ByteBuf buf, String value) {
        byte[] bytes = new byte[CLASSIC_STRING_LENGTH];
        // Fill with spaces
        for (int i = 0; i < CLASSIC_STRING_LENGTH; i++) {
            bytes[i] = 0x20;
        }
        // Copy string bytes
        byte[] strBytes = value.getBytes(US_ASCII);
        int len = Math.min(strBytes.length, CLASSIC_STRING_LENGTH);
        System.arraycopy(strBytes, 0, bytes, 0, len);
        buf.writeBytes(bytes);
    }

    /**
     * Read a Classic fixed-length byte array (1024 bytes, null-padded).
     */
    public static byte[] readClassicByteArray(ByteBuf buf) {
        byte[] bytes = new byte[CLASSIC_BYTE_ARRAY_LENGTH];
        buf.readBytes(bytes);
        return bytes;
    }

    /**
     * Write a Classic fixed-length byte array (1024 bytes, null-padded).
     */
    public static void writeClassicByteArray(ByteBuf buf, byte[] data) {
        byte[] bytes = new byte[CLASSIC_BYTE_ARRAY_LENGTH];
        int len = Math.min(data.length, CLASSIC_BYTE_ARRAY_LENGTH);
        System.arraycopy(data, 0, bytes, 0, len);
        buf.writeBytes(bytes);
    }

    // ========================================================================
    // Alpha/Beta data types (pre-Netty, a1.0.15 - 1.6.4)
    // ========================================================================

    /**
     * Read a string16 (Alpha/Beta format: short length prefix + UCS-2/UTF-16BE chars).
     * Wire format: [2 bytes: char count] [N*2 bytes: UTF-16BE encoded characters]
     */
    public static String readString16(ByteBuf buf) {
        short charCount = buf.readShort();
        if (charCount < 0) {
            throw new IllegalStateException("String16 char count is negative: " + charCount);
        }
        byte[] bytes = new byte[charCount * 2];
        buf.readBytes(bytes);
        return new String(bytes, UTF_16BE);
    }

    /**
     * Write a string16 (Alpha/Beta format: short length prefix + UCS-2/UTF-16BE chars).
     * Wire format: [2 bytes: char count] [N*2 bytes: UTF-16BE encoded characters]
     */
    public static void writeString16(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(UTF_16BE);
        buf.writeShort(bytes.length / 2); // char count, not byte count
        buf.writeBytes(bytes);
    }

    // ========================================================================
    // VarInt (1.7+ / post-Netty format, for future use)
    // ========================================================================

    /**
     * Read a VarInt (LEB128-like encoding, max 5 bytes).
     * Used in MC 1.7+ for packet IDs, lengths, and many fields.
     */
    public static int readVarInt(ByteBuf buf) {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = buf.readByte();
            value |= (currentByte & 0x7F) << position;

            if ((currentByte & 0x80) == 0) {
                break;
            }

            position += 7;
            if (position >= 32) {
                throw new RuntimeException("VarInt is too big");
            }
        }

        return value;
    }

    /**
     * Write a VarInt (LEB128-like encoding, max 5 bytes).
     */
    public static void writeVarInt(ByteBuf buf, int value) {
        while (true) {
            if ((value & ~0x7F) == 0) {
                buf.writeByte(value);
                return;
            }
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    /**
     * Calculate the byte size of a VarInt without writing it.
     */
    public static int varIntSize(int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) return 1;
        if ((value & (0xFFFFFFFF << 14)) == 0) return 2;
        if ((value & (0xFFFFFFFF << 21)) == 0) return 3;
        if ((value & (0xFFFFFFFF << 28)) == 0) return 4;
        return 5;
    }
}
