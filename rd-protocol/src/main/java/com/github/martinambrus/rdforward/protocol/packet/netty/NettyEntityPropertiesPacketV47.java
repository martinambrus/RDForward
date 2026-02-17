package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, S2C packet 0x20: Entity Properties.
 *
 * 1.8 changed entityId from int to VarInt and modifier count from short to VarInt.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [int]    property count
 *   Per property:
 *     [String]  key
 *     [double]  value
 *     [VarInt]  modifier count
 *     Per modifier:
 *       [UUID]   uuid (2 longs)
 *       [double] amount
 *       [byte]   operation
 */
public class NettyEntityPropertiesPacketV47 implements Packet {

    private int entityId;
    private String key;
    private double value;

    public NettyEntityPropertiesPacketV47() {}

    public NettyEntityPropertiesPacketV47(int entityId, String key, double value) {
        this.entityId = entityId;
        this.key = key;
        this.value = value;
    }

    @Override
    public int getPacketId() { return 0x20; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        buf.writeInt(1); // 1 property
        McDataTypes.writeVarIntString(buf, key);
        buf.writeDouble(value);
        McDataTypes.writeVarInt(buf, 0); // 0 modifiers
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        int count = buf.readInt();
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
