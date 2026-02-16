package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0xFC (bidirectional): Encryption Key Response.
 *
 * C2S: Contains the RSA-encrypted shared secret and verify token.
 * S2C: Both arrays are empty (length 0), signaling "enable encryption now".
 *
 * Wire format:
 *   [short]   data1 length
 *   [byte[]]  data1 (C2S: encrypted shared secret, S2C: empty)
 *   [short]   data2 length
 *   [byte[]]  data2 (C2S: encrypted verify token, S2C: empty)
 */
public class EncryptionKeyResponsePacket implements Packet {

    private byte[] data1;
    private byte[] data2;

    public EncryptionKeyResponsePacket() {}

    /** S2C constructor: both arrays empty to signal "enable encryption". */
    public EncryptionKeyResponsePacket(byte[] data1, byte[] data2) {
        this.data1 = data1;
        this.data2 = data2;
    }

    @Override
    public int getPacketId() {
        return 0xFC;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(data1 != null ? data1.length : 0);
        if (data1 != null && data1.length > 0) {
            buf.writeBytes(data1);
        }
        buf.writeShort(data2 != null ? data2.length : 0);
        if (data2 != null && data2.length > 0) {
            buf.writeBytes(data2);
        }
    }

    @Override
    public void read(ByteBuf buf) {
        int len1 = buf.readShort();
        data1 = new byte[len1];
        if (len1 > 0) {
            buf.readBytes(data1);
        }
        int len2 = buf.readShort();
        data2 = new byte[len2];
        if (len2 > 0) {
            buf.readBytes(data2);
        }
    }

    public byte[] getData1() { return data1; }
    public byte[] getData2() { return data2; }
}
