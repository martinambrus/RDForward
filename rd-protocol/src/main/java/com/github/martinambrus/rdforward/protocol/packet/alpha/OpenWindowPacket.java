package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x64 (Server -> Client): Open Window.
 *
 * Opens a container window on the client.
 *
 * Wire format:
 *   [byte]     window ID
 *   [byte]     window type (0=chest, 1=workbench, 2=furnace, 3=dispenser)
 *   [string16] window title
 *   [byte]     number of slots
 */
public class OpenWindowPacket implements Packet {

    private int windowId;
    private int windowType;
    private String title;
    private int slotCount;

    public OpenWindowPacket() {}

    public OpenWindowPacket(int windowId, int windowType, String title, int slotCount) {
        this.windowId = windowId;
        this.windowType = windowType;
        this.title = title;
        this.slotCount = slotCount;
    }

    @Override
    public int getPacketId() {
        return 0x64;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeByte(windowType);
        McDataTypes.writeString16(buf, title);
        buf.writeByte(slotCount);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        windowType = buf.readByte();
        title = McDataTypes.readString16(buf);
        slotCount = buf.readByte();
    }

    public int getWindowId() { return windowId; }
    public int getWindowType() { return windowType; }
    public String getTitle() { return title; }
    public int getSlotCount() { return slotCount; }
}
