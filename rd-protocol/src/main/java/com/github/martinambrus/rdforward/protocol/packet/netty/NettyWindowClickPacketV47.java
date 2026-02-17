package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x0E: Click Window.
 *
 * Same structure as 1.7.2 but uses V47 slot format (byte TAG_End for no NBT).
 *
 * Wire format:
 *   [byte]  windowId
 *   [short] slotIndex
 *   [byte]  button
 *   [short] actionNumber
 *   [byte]  mode
 *   [slot]  clickedItem (V47 format)
 */
public class NettyWindowClickPacketV47 implements Packet {

    private int windowId;
    private short slotIndex;
    private byte button;
    private short actionNumber;
    private byte mode;

    public NettyWindowClickPacketV47() {}

    public NettyWindowClickPacketV47(int windowId, int slotIndex, int button, int actionNumber, int mode) {
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
        // V47 empty slot: short -1
        buf.writeShort(-1);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        slotIndex = buf.readShort();
        button = buf.readByte();
        actionNumber = buf.readShort();
        mode = buf.readByte();
        McDataTypes.skipV47SlotData(buf);
    }

    public int getWindowId() { return windowId; }
    public short getSlotIndex() { return slotIndex; }
    public byte getButton() { return button; }
    public short getActionNumber() { return actionNumber; }
    public byte getMode() { return mode; }
}
