package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.20.5 Play state, S2C: Update Attributes (Entity Properties).
 *
 * 1.20.5 changed attribute key from String to VarInt registry ID.
 *
 * Wire format:
 *   [VarInt] entityId
 *   [VarInt] property count
 *   Per property:
 *     [VarInt]  attribute registry ID  (was String in 1.17-1.20.3)
 *     [double]  value
 *     [VarInt]  modifier count
 *     Per modifier:
 *       [UUID]   uuid (2 longs)
 *       [double] amount
 *       [byte]   operation
 *
 * Common attribute IDs:
 *   0=armor, 2=attack_damage, 4=attack_speed, 9=flying_speed,
 *   16=max_health, 17=movement_speed
 */
public class NettyEntityPropertiesPacketV766 implements Packet {

    /** generic.movement_speed registry ID in 1.20.5 */
    public static final int MOVEMENT_SPEED = 17;

    private int entityId;
    private int attributeId;
    private double value;

    public NettyEntityPropertiesPacketV766() {}

    public NettyEntityPropertiesPacketV766(int entityId, int attributeId, double value) {
        this.entityId = entityId;
        this.attributeId = attributeId;
        this.value = value;
    }

    @Override
    public int getPacketId() { return 0x75; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, entityId);
        McDataTypes.writeVarInt(buf, 1); // 1 property
        McDataTypes.writeVarInt(buf, attributeId);
        buf.writeDouble(value);
        McDataTypes.writeVarInt(buf, 0); // 0 modifiers
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = McDataTypes.readVarInt(buf);
        int count = McDataTypes.readVarInt(buf);
        for (int i = 0; i < count; i++) {
            int id = McDataTypes.readVarInt(buf);
            if (i == 0) {
                attributeId = id;
                value = buf.readDouble();
            } else {
                buf.skipBytes(8); // double value
            }
            int modCount = McDataTypes.readVarInt(buf);
            for (int j = 0; j < modCount; j++) {
                buf.skipBytes(16 + 8 + 1); // UUID + amount + operation
            }
        }
    }
}
