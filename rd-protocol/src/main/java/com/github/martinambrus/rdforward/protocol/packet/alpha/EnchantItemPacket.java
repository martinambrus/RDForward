package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.0.0+ protocol 0x6C (Client -> Server): Enchant Item.
 *
 * Sent when the player selects an enchantment from the enchanting table.
 * Silently consumed by the server.
 *
 * Wire format:
 *   [byte] window ID
 *   [byte] enchantment (0, 1, or 2 — the slot clicked)
 */
public class EnchantItemPacket implements Packet {

    private byte windowId;
    private byte enchantment;

    public EnchantItemPacket() {}

    @Override
    public int getPacketId() {
        return 0x6C;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(windowId);
        buf.writeByte(enchantment);
    }

    @Override
    public void read(ByteBuf buf) {
        windowId = buf.readByte();
        enchantment = buf.readByte();
    }

    public byte getWindowId() { return windowId; }
    public byte getEnchantment() { return enchantment; }
}
