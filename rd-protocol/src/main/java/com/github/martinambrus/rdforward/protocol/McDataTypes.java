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
     * This matches Java's DataInputStream.readUTF() format used by the original MC Alpha client,
     * which encodes the length as an UNSIGNED 16-bit value (0..65535). Using a signed read here
     * caused spurious "byte count is negative" errors when junk/garbage bytes (e.g. 0xFFFF from
     * an abruptly closing client) were interpreted as -1 instead of 65535. With the unsigned
     * read, garbage lengths simply trigger an IndexOutOfBoundsException in readBytes(), which
     * the frame decoder handles gracefully by waiting for more data until the channel closes.
     * Wire format: [2 bytes: unsigned byte count] [N bytes: Modified UTF-8 encoded characters]
     */
    public static String readJavaUTF(ByteBuf buf) {
        int byteCount = buf.readUnsignedShort();
        byte[] bytes = new byte[byteCount];
        buf.readBytes(bytes);
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * Write a Java Modified UTF-8 string (Alpha format: short byte-count prefix + UTF-8 bytes).
     * This matches Java's DataOutputStream.writeUTF() format used by the original MC Alpha client.
     * Wire format: [2 bytes: byte count] [N bytes: Modified UTF-8 encoded characters]
     */
    public static void writeJavaUTF(ByteBuf buf, String value) {
        byte[] bytes = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
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
            return new Object[]{new String(bytes, java.nio.charset.StandardCharsets.UTF_8), false};
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
    // Netty-era string and slot helpers (1.7.2+)
    // ========================================================================

    private static final java.nio.charset.Charset UTF_8 = java.nio.charset.Charset.forName("UTF-8");

    /**
     * Read a VarInt-prefixed UTF-8 string (1.7.2+ format).
     * Wire format: [VarInt byteLength] [byteLength bytes UTF-8]
     */
    public static String readVarIntString(ByteBuf buf) {
        int length = readVarInt(buf);
        if (length < 0) {
            throw new IllegalStateException("VarIntString byte count is negative: " + length);
        }
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);
        return new String(bytes, UTF_8);
    }

    /**
     * Write a VarInt-prefixed UTF-8 string (1.7.2+ format).
     * Wire format: [VarInt byteLength] [byteLength bytes UTF-8]
     *
     * Uses ByteBufUtil.utf8Bytes for length calculation and writeCharSequence
     * for writing, avoiding an intermediate byte[] allocation.
     */
    public static void writeVarIntString(ByteBuf buf, String value) {
        int utf8Bytes = io.netty.buffer.ByteBufUtil.utf8Bytes(value);
        writeVarInt(buf, utf8Bytes);
        buf.writeCharSequence(value, UTF_8);
    }

    /**
     * Skip a Netty-era item slot in C2S packets (1.7.2+ format).
     * Wire format: [short itemId] [if >= 0: byte count, short damage,
     *   short nbtLength (-1 = no NBT, else nbtLength bytes of gzipped NBT)]
     */
    public static void skipNettySlotData(ByteBuf buf) {
        short itemId = buf.readShort();
        if (itemId >= 0) {
            buf.skipBytes(1); // count
            buf.skipBytes(2); // damage
            short nbtLength = buf.readShort();
            if (nbtLength > 0) {
                buf.skipBytes(nbtLength);
            }
        }
    }

    /**
     * Write an empty Netty-era item slot (1.7.2+ format).
     * Wire format: short(-1)
     */
    public static void writeEmptyNettySlot(ByteBuf buf) {
        buf.writeShort(-1);
    }

    /**
     * Write a Netty-era item slot with item data (1.7.2+ format).
     * Wire format: [short itemId, byte count, short damage, short nbtLength (-1 = no NBT)]
     */
    public static void writeNettySlotItem(ByteBuf buf, int itemId, int count, int damage) {
        buf.writeShort(itemId);
        buf.writeByte(count);
        buf.writeShort(damage);
        buf.writeShort(-1); // no NBT data
    }

    /**
     * Write an NBT TAG_String payload: [short byteLength] [Modified UTF-8 bytes].
     * Uses Java's Modified UTF-8 encoding (DataOutputStream.writeUTF format),
     * which encodes supplementary characters (emojis, etc.) as surrogate pairs —
     * required by the Minecraft client's NBT decoder.
     */
    public static void writeNbtStringPayload(ByteBuf buf, String value) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try {
            new java.io.DataOutputStream(baos).writeUTF(value);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Modified UTF-8 encoding failed", e);
        }
        // DataOutputStream.writeUTF writes [short len][bytes] — write the whole thing
        buf.writeBytes(baos.toByteArray());
    }

    /** Maximum NBT nesting depth to prevent stack overflow from malformed data. */
    private static final int MAX_NBT_DEPTH = 512;

    /**
     * Skip one root-level named NBT tag: reads the type byte, tag name, and payload.
     * Used to skip root-level named tags in network NBT (e.g., dimension codec).
     */
    public static void skipNbtRootTag(ByteBuf buf) {
        byte type = buf.readByte();
        if (type == 0) return; // TAG_End
        int nameLen = buf.readUnsignedShort();
        buf.skipBytes(nameLen);
        skipNbtPayload(buf, type, 0);
    }

    /**
     * Skip the body of an NBT compound tag (starting after the 0x0A type byte
     * and name have already been read). Reads children until TAG_End (0x00).
     */
    public static void skipNbtCompound(ByteBuf buf) {
        skipNbtCompound(buf, 0);
    }

    private static void skipNbtCompound(ByteBuf buf, int depth) {
        if (depth > MAX_NBT_DEPTH) {
            throw new IllegalStateException("NBT nesting depth exceeds " + MAX_NBT_DEPTH);
        }
        while (true) {
            byte tagType = buf.readByte();
            if (tagType == 0) return; // TAG_End
            int nameLen = buf.readUnsignedShort();
            buf.skipBytes(nameLen);
            skipNbtPayload(buf, tagType, depth + 1);
        }
    }

    /**
     * Skip the payload of an NBT tag by type.
     *
     * @param buf  the buffer to read from
     * @param type NBT tag type ID (1=Byte through 12=Long_Array)
     */
    public static void skipNbtPayload(ByteBuf buf, byte type) {
        skipNbtPayload(buf, type, 0);
    }

    private static void skipNbtPayload(ByteBuf buf, byte type, int depth) {
        if (depth > MAX_NBT_DEPTH) {
            throw new IllegalStateException("NBT nesting depth exceeds " + MAX_NBT_DEPTH);
        }
        switch (type) {
            case 1: buf.skipBytes(1); break; // TAG_Byte
            case 2: buf.skipBytes(2); break; // TAG_Short
            case 3: buf.skipBytes(4); break; // TAG_Int
            case 4: buf.skipBytes(8); break; // TAG_Long
            case 5: buf.skipBytes(4); break; // TAG_Float
            case 6: buf.skipBytes(8); break; // TAG_Double
            case 7: { // TAG_Byte_Array
                int baLen = buf.readInt();
                if (baLen < 0) throw new IllegalStateException("Negative NBT byte array length: " + baLen);
                buf.skipBytes(baLen);
                break;
            }
            case 8: // TAG_String
                int strLen = buf.readUnsignedShort();
                buf.skipBytes(strLen);
                break;
            case 9: { // TAG_List
                byte listType = buf.readByte();
                int listLen = buf.readInt();
                if (listLen < 0) throw new IllegalStateException("Negative NBT list length: " + listLen);
                for (int i = 0; i < listLen; i++) {
                    skipNbtPayload(buf, listType, depth + 1);
                }
                break;
            }
            case 10: // TAG_Compound
                skipNbtCompound(buf, depth + 1);
                break;
            case 11: { // TAG_Int_Array
                int iaLen = buf.readInt();
                if (iaLen < 0) throw new IllegalStateException("Negative NBT int array length: " + iaLen);
                buf.skipBytes(iaLen * 4);
                break;
            }
            case 12: { // TAG_Long_Array
                int laLen = buf.readInt();
                if (laLen < 0) throw new IllegalStateException("Negative NBT long array length: " + laLen);
                buf.skipBytes(laLen * 8);
                break;
            }
            default:
                throw new IllegalStateException("Unknown NBT tag type: " + type);
        }
    }

    /**
     * Read an NBT text component (TAG_Compound body) and extract the plain text.
     * Concatenates all "text" and "translate" string values, recursing into
     * nested compounds and "extra" lists. The compound body starts after the
     * 0x0A type byte and name have already been read.
     *
     * @param buf the buffer positioned at the start of compound children
     * @return the extracted plain text
     */
    public static String readNbtTextComponent(ByteBuf buf) {
        StringBuilder sb = new StringBuilder();
        readNbtCompoundText(buf, sb, 0);
        return sb.toString();
    }

    private static void readNbtCompoundText(ByteBuf buf, StringBuilder sb, int depth) {
        if (depth > MAX_NBT_DEPTH) return;
        while (buf.readableBytes() > 0) {
            int entryType = buf.readByte();
            if (entryType == 0x00) break; // TAG_End

            int nameLen = buf.readUnsignedShort();
            byte[] nameBytes = new byte[nameLen];
            buf.readBytes(nameBytes);
            String name = new String(nameBytes, java.nio.charset.StandardCharsets.UTF_8);

            switch (entryType) {
                case 0x01: buf.readByte(); break;
                case 0x02: buf.readShort(); break;
                case 0x03: buf.readInt(); break;
                case 0x04: buf.readLong(); break;
                case 0x05: buf.readFloat(); break;
                case 0x06: buf.readDouble(); break;
                case 0x07: { int len = buf.readInt(); if (len > 0) buf.skipBytes(len); break; }
                case 0x08: {
                    int len = buf.readUnsignedShort();
                    byte[] bytes = new byte[len];
                    buf.readBytes(bytes);
                    String value = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                    if ("text".equals(name) || "translate".equals(name)) {
                        sb.append(value);
                    }
                    break;
                }
                case 0x09: {
                    byte elemType = buf.readByte();
                    int count = buf.readInt();
                    if (count < 0) break;
                    if (elemType == 0x0A && "extra".equals(name)) {
                        for (int i = 0; i < count; i++) {
                            readNbtCompoundText(buf, sb, depth + 1);
                        }
                    } else {
                        // Skip list entries using McDataTypes (handles all types including nested lists)
                        for (int i = 0; i < count; i++) {
                            skipNbtPayload(buf, elemType, depth + 1);
                        }
                    }
                    break;
                }
                case 0x0A: readNbtCompoundText(buf, sb, depth + 1); break;
                case 0x0B: { int len = buf.readInt(); if (len > 0) buf.skipBytes(len * 4); break; }
                case 0x0C: { int len = buf.readInt(); if (len > 0) buf.skipBytes(len * 8); break; }
                default: return;
            }
        }
    }

    // ========================================================================
    // 1.8+ (v47) Position and Slot helpers
    // ========================================================================

    /**
     * Write a packed Position long (1.8+ format).
     * Bit layout: x (26 bits, signed) | y (12 bits, unsigned) | z (26 bits, signed)
     * Encoded as: ((x & 0x3FFFFFF) << 38) | ((y & 0xFFF) << 26) | (z & 0x3FFFFFF)
     */
    public static void writePosition(ByteBuf buf, int x, int y, int z) {
        long val = ((long)(x & 0x3FFFFFF) << 38) | ((long)(y & 0xFFF) << 26) | (z & 0x3FFFFFF);
        buf.writeLong(val);
    }

    /**
     * Read a packed Position long (1.8-1.13 format).
     * Returns int[3] = {x, y, z} with sign extension for x and z.
     */
    public static int[] readPosition(ByteBuf buf) {
        long val = buf.readLong();
        int x = (int)(val >> 38);
        int y = (int)((val >> 26) & 0xFFF);
        int z = (int)(val << 38 >> 38); // sign-extend 26-bit z
        return new int[]{x, y, z};
    }

    /**
     * Write a packed Position long (1.14+ format).
     * Bit layout: x (26 bits, signed) | z (26 bits, signed) | y (12 bits, signed)
     * Encoded as: ((x & 0x3FFFFFF) << 38) | ((z & 0x3FFFFFF) << 12) | (y & 0xFFF)
     */
    public static void writePositionV477(ByteBuf buf, int x, int y, int z) {
        long val = ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (y & 0xFFF);
        buf.writeLong(val);
    }

    /**
     * Read a packed Position long (1.14+ format).
     * Bit layout changed from 1.8: y moved to bottom 12 bits, z moved to bits 12-37.
     * Returns int[3] = {x, y, z} with sign extension for x and z.
     */
    public static int[] readPositionV477(ByteBuf buf) {
        long val = buf.readLong();
        int x = (int)(val >> 38);
        int z = (int)(val << 26 >> 38); // sign-extend 26-bit z from bits 12-37
        int y = (int)(val & 0xFFF);
        return new int[]{x, y, z};
    }

    /**
     * Write a 1.8+ item slot with data (V47 format).
     * Wire format: [short itemId, byte count, short damage, byte 0x00 (TAG_End)]
     * 1.8 uses byte(0x00) TAG_End for no NBT, instead of 1.7's short(-1).
     */
    public static void writeV47SlotItem(ByteBuf buf, int itemId, int count, int damage) {
        buf.writeShort(itemId);
        buf.writeByte(count);
        buf.writeShort(damage);
        buf.writeByte(0x00); // TAG_End = no NBT
    }

    /**
     * Write an empty 1.8+ item slot (V47 format).
     * Wire format: short(-1)
     */
    public static void writeEmptyV47Slot(ByteBuf buf) {
        buf.writeShort(-1);
    }

    /**
     * Skip the NBT trailer of a 1.8+ slot (after itemId/count/damage have been read).
     * Wire format: byte 0x00 = TAG_End (no data), byte 0x0A = TAG_Compound (skip compound).
     */
    public static void skipV47SlotNbt(ByteBuf buf) {
        byte nbtType = buf.readByte();
        if (nbtType == 0x0A) {
            skipNbtCompound(buf);
        }
    }

    /**
     * Skip a 1.8+ C2S item slot (V47 format).
     * Wire format: [short itemId, if >= 0: byte count, short damage, NBT...]
     * NBT: byte 0x00 = TAG_End (no data), byte 0x0A = TAG_Compound (skip compound).
     */
    public static void skipV47SlotData(ByteBuf buf) {
        short itemId = buf.readShort();
        if (itemId >= 0) {
            buf.skipBytes(1); // count
            buf.skipBytes(2); // damage
            // NBT: peek at type byte
            byte nbtType = buf.readByte();
            if (nbtType == 0x0A) {
                // TAG_Compound — skip its contents
                skipNbtCompound(buf);
            }
            // else 0x00 = TAG_End, nothing more to skip
        }
    }

    // ========================================================================
    // VarInt (1.7+ / post-Netty format)
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
     * Optimized: peels 1-byte and 2-byte cases (the most common for packet IDs
     * and lengths) into single writeByte/writeShort calls to reduce Netty write
     * call overhead. Derived from Velocity/Krypton.
     */
    public static void writeVarInt(ByteBuf buf, int value) {
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
            buf.writeMedium(w);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21);
            buf.writeInt(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
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
