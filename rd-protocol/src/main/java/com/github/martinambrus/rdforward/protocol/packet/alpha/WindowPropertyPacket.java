package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x69 (Server -> Client): Window Property.
 *
 * Updates a progress bar / property value in a window (e.g. furnace progress).
 *
 * Wire format (5 bytes payload):
 *   [byte]  window ID
 *   [short] property index
 *   [short] value
 */
public class WindowPropertyPacket implements Packet {

    private int windowId;
    private short property;
    private short value;

    public WindowPropertyPacket() {}

    public WindowPropertyPacket(int windowId, int property, int value) {
        this.windowId = windowId;
        this.property = (short) property;
        this.value = (short) value;
    }

    @Override
    public int getPacketId() {
        return 0x69;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(property);
        buf.writeShort(value);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        property = buf.readShort();
        value = buf.readShort();
    }

    public int getWindowId() { return windowId; }
    public short getProperty() { return property; }
    public short getValue() { return value; }
}
