package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.17+ Play state, C2S: Click Container.
 *
 * Format changed in 1.17 (v755): added stateId, changed slots array,
 * carried item at end. This version works for v755-v765.
 *
 * Wire format:
 *   [VarInt] windowId
 *   [VarInt] stateId (revision counter, server sends in SetContainerContent)
 *   [Short]  slot
 *   [Byte]   button (0=left, 1=right)
 *   [VarInt] mode (0=click, 1=shift-click, 4=drop)
 *   [VarInt] changed slots count
 *   For each changed slot:
 *     [Short] slot number
 *     [Slot]  new value
 *   [Slot]   carried item (cursor)
 */
public class NettyWindowClickPacketV755 implements Packet {

    private int windowId;
    private int stateId;
    private short slot;
    private byte button;
    private int mode;

    public NettyWindowClickPacketV755() {}

    public NettyWindowClickPacketV755(int windowId, int stateId, int slot, int button, int mode) {
        this.windowId = windowId;
        this.stateId = stateId;
        this.slot = (short) slot;
        this.button = (byte) button;
        this.mode = mode;
    }

    @Override
    public int getPacketId() { return 0x0D; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, windowId);
        McDataTypes.writeVarInt(buf, stateId);
        buf.writeShort(slot);
        buf.writeByte(button);
        McDataTypes.writeVarInt(buf, mode);
        McDataTypes.writeVarInt(buf, 0); // no changed slots
        // Carried item: not present (empty slot)
        buf.writeBoolean(false);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = McDataTypes.readVarInt(buf);
        stateId = McDataTypes.readVarInt(buf);
        slot = buf.readShort();
        button = buf.readByte();
        mode = McDataTypes.readVarInt(buf);
        buf.skipBytes(buf.readableBytes()); // skip remaining (changed slots + carried item)
    }

    public int getWindowId() { return windowId; }
    public int getStateId() { return stateId; }
    public short getSlot() { return slot; }
    public byte getButton() { return button; }
    public int getMode() { return mode; }
}
