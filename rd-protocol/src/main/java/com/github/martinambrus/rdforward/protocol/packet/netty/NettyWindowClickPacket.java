package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, C2S packet 0x0E: Click Window.
 *
 * Wire format:
 *   [byte]  windowId
 *   [short] slotIndex
 *   [byte]  button
 *   [short] actionNumber
 *   [byte]  mode
 *   [slot]  clickedItem (Netty slot data)
 */
public class NettyWindowClickPacket implements Packet {

    private int windowId;
    private short slotIndex;
    private byte button;
    private short actionNumber;
    private byte mode;

    public NettyWindowClickPacket() {}

    public NettyWindowClickPacket(int windowId, int slotIndex, int button, int actionNumber, int mode) {
        this.windowId = windowId;
        this.slotIndex = (short) slotIndex;
        this.button = (byte) button;
        this.actionNumber = (short) actionNumber;
        this.mode = (byte) mode;
    }

    @Override
    public int getPacketId() { return 0x0E; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeShort(slotIndex);
        buf.writeByte(button);
        buf.writeShort(actionNumber);
        buf.writeByte(mode);
        // Empty slot data: short -1 means no item
        buf.writeShort(-1);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slotIndex = buf.readShort();
        button = buf.readByte();
        actionNumber = buf.readShort();
        mode = buf.readByte();
        McDataTypes.skipNettySlotData(buf);
    }

    public int getWindowId() { return windowId; }
    public short getSlotIndex() { return slotIndex; }
    public byte getButton() { return button; }
    public short getActionNumber() { return actionNumber; }
    public byte getMode() { return mode; }
}
