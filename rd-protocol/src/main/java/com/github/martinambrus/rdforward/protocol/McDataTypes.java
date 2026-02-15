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

    /**
     * Thread-local flag controlling whether adaptive string methods use String16
     * (UTF-16BE) or Java Modified UTF-8 encoding. Set by RawPacketDecoder/Encoder
     * for Beta 1.5+ connections that use String16 encoding.
     */
    public static final ThreadLocal<Boolean> STRING16_MODE = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

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
    // Java Modified UTF-8 data types (Alpha protocol, a1.0.15 - a1.2.6)
    // ========================================================================

    /**
     * Read a Java Modified UTF-8 string (Alpha format: short byte-count prefix + UTF-8 bytes).
     * This matches Java's DataInputStream.readUTF() format used by the original MC Alpha client.
     * Wire format: [2 bytes: byte count] [N bytes: Modified UTF-8 encoded characters]
     */
    public static String readJavaUTF(ByteBuf buf) {
        short byteCount = buf.readShort();
        if (byteCount < 0) {
            throw new IllegalStateException("JavaUTF byte count is negative: " + byteCount);
        }
        byte[] bytes = new byte[byteCount];
        buf.readBytes(bytes);
        return new String(bytes, Charset.forName("UTF-8"));
    }

    /**
     * Write a Java Modified UTF-8 string (Alpha format: short byte-count prefix + UTF-8 bytes).
     * This matches Java's DataOutputStream.writeUTF() format used by the original MC Alpha client.
     * Wire format: [2 bytes: byte count] [N bytes: Modified UTF-8 encoded characters]
     */
    public static void writeJavaUTF(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(Charset.forName("UTF-8"));
        buf.writeShort(bytes.length);
        buf.writeBytes(bytes);
    }

    // ========================================================================
    // Adaptive string methods (auto-detect or delegate based on STRING16_MODE)
    // ========================================================================

    /**
     * Auto-detect string encoding format by peeking at the first data byte
     * after the 2-byte length prefix. For ASCII strings (all MC usernames),
     * String16's first byte is always 0x00 (high byte of UTF-16BE), while
     * writeUTF's first byte is the character itself (non-zero for ASCII).
     *
     * Used by HandshakeC2SPacket to detect the client's string format before
     * the protocol version is known.
     *
     * @return a two-element Object array: [0] = String value, [1] = Boolean isString16
     */
    public static Object[] readStringAuto(ByteBuf buf) {
        short length = buf.readShort();
        if (length <= 0) {
            return new Object[]{"", false};
        }
        // Peek at the first byte after the length prefix to determine format.
        // String16: length is char count, first byte is 0x00 (high byte of UTF-16BE for ASCII).
        // writeUTF: length is byte count, first byte is the character itself (non-zero for ASCII).
        byte firstByte = buf.getByte(buf.readerIndex());
        if (firstByte == 0x00) {
            // String16 format: length is char count, read length*2 bytes as UTF-16BE
            byte[] bytes = new byte[length * 2];
            buf.readBytes(bytes);
            return new Object[]{new String(bytes, UTF_16BE), true};
        } else {
            // writeUTF format: length is byte count, read length bytes as UTF-8
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);
            return new Object[]{new String(bytes, Charset.forName("UTF-8")), false};
        }
    }

    /**
     * Read a string using the format determined by STRING16_MODE ThreadLocal.
     * When STRING16_MODE is true, reads String16 (UTF-16BE). Otherwise reads
     * Java Modified UTF-8. Used by all C2S packets except HandshakeC2SPacket.
     */
    public static String readStringAdaptive(ByteBuf buf) {
        if (STRING16_MODE.get()) {
            return readString16(buf);
        } else {
            return readJavaUTF(buf);
        }
    }

    /**
     * Write a string using the format determined by STRING16_MODE ThreadLocal.
     * When STRING16_MODE is true, writes String16 (UTF-16BE). Otherwise writes
     * Java Modified UTF-8. Used by all S2C packets that contain strings.
     */
    public static void writeStringAdaptive(ByteBuf buf, String value) {
        if (STRING16_MODE.get()) {
            writeString16(buf, value);
        } else {
            writeJavaUTF(buf, value);
        }
    }

    // ========================================================================
    // NBT item slot helpers (Release 1.0.0+ / v22+)
    // ========================================================================

    /**
     * Check if an item ID corresponds to a damageable item in Release 1.0.0
     * that includes NBT tag data in its wire format. The R1.0.0 client's base
     * Packet class conditionally reads/writes NBT only for items where
     * Item.isDamageable() returns true (maxDamage > 0 and !hasSubtypes).
     * This includes tools, weapons, armor, bows, fishing rods, flint and steel,
     * and shears. Blocks (IDs 0-255) are never damageable.
     */
    public static boolean isNbtDamageableItem(int itemId) {
        return (itemId >= 256 && itemId <= 259)  // iron shovel/pick/axe + flint & steel
            || itemId == 261                      // bow
            || (itemId >= 267 && itemId <= 279)   // iron/wood/stone/diamond swords + tools
            || (itemId >= 283 && itemId <= 286)   // gold sword/shovel/pick/axe
            || (itemId >= 290 && itemId <= 294)   // hoes (wood/stone/iron/diamond/gold)
            || (itemId >= 298 && itemId <= 317)   // armor (leather/chain/iron/diamond/gold)
            || itemId == 346                      // fishing rod
            || itemId == 359;                     // shears
    }

    /**
     * Skip the NBT tag data appended to item slots in v22+ (Release 1.0.0).
     * Wire format: [short nbtLength] [if > 0: nbtLength bytes of gzipped NBT].
     * Our server never uses NBT, so we just skip past the data.
     */
    public static void skipNbtItemTag(ByteBuf buf) {
        short nbtLength = buf.readShort();
        if (nbtLength > 0) {
            buf.skipBytes(nbtLength);
        }
    }

    /**
     * Write an empty NBT tag for item slots in v22+ (Release 1.0.0).
     * Writes short -1 to indicate no NBT data.
     */
    public static void writeEmptyNbtItemTag(ByteBuf buf) {
        buf.writeShort(-1);
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
