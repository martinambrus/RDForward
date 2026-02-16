package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.7.2 Play state, S2C packet 0x20: Entity Properties.
 *
 * Wire format:
 *   [int]    entityId
 *   [int]    property count
 *   Per property:
 *     [String] key
 *     [double] value
 *     [short]  modifier count (0)
 */
public class NettyEntityPropertiesPacket implements Packet {

    private int entityId;
    private String key;
    private double value;

    public NettyEntityPropertiesPacket() {}

    public NettyEntityPropertiesPacket(int entityId, String key, double value) {
        this.entityId = entityId;
        this.key = key;
        this.value = value;
    }

    @Override
    public int getPacketId() { return 0x20; }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(1); // 1 property
        McDataTypes.writeVarIntString(buf, key);
        buf.writeDouble(value);
        buf.writeShort(0); // 0 modifiers
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        int count = buf.readInt();
        if (count > 0) {
            key = McDataTypes.readVarIntString(buf);
            value = buf.readDouble();
            int modCount = buf.readShort();
            for (int i = 0; i < modCount; i++) {
                buf.skipBytes(16 + 8 + 1); // UUID + amount + operation
            }
        }
        // Skip remaining properties
        for (int i = 1; i < count; i++) {
            McDataTypes.readVarIntString(buf);
            buf.skipBytes(8);
            int modCount = buf.readShort();
            for (int j = 0; j < modCount; j++) {
                buf.skipBytes(16 + 8 + 1);
            }
        }
    }

    public int getEntityId() { return entityId; }
}
