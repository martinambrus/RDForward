package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.17 Play state, S2C: Update Attributes (Entity Properties).
 *
 * 1.17 changed property count from int to VarInt.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [VarInt] property count   (was int in 1.8-1.16.x)
 *   Per property:
 *     [String]  key
 *     [double]  value
 *     [VarInt]  modifier count
 *     Per modifier:
 *       [UUID]   uuid (2 longs)
 *       [double] amount
 *       [byte]   operation
 */
public class NettyEntityPropertiesPacketV755 implements Packet {

    private int entityId;
    private String key;
    private double value;

    public NettyEntityPropertiesPacketV755() {}

    public NettyEntityPropertiesPacketV755(int entityId, String key, double value) {
        this.entityId = entityId;
        this.key = key;
        this.value = value;
    }

    @Override
    public int getPacketId() { return 0x63; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        McDataTypes.writeVarInt(buf, 1); // 1 property (VarInt, not int)
        McDataTypes.writeVarIntString(buf, key);
        buf.writeDouble(value);
        McDataTypes.writeVarInt(buf, 0); // 0 modifiers
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        int count = McDataTypes.readVarInt(buf);
        if (count > 0) {
            key = McDataTypes.readVarIntString(buf);
            value = buf.readDouble();
            int modCount = McDataTypes.readVarInt(buf);
            for (int i = 0; i < modCount; i++) {
                buf.skipBytes(16 + 8 + 1); // UUID + amount + operation
            }
        }
        for (int i = 1; i < count; i++) {
            McDataTypes.readVarIntString(buf);
            buf.skipBytes(8);
            int modCount = McDataTypes.readVarInt(buf);
            for (int j = 0; j < modCount; j++) {
                buf.skipBytes(16 + 8 + 1);
            }
        }
    }
}
