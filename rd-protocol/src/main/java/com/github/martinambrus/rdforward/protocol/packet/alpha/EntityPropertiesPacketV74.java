package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.6.2+ (v74+) protocol 0x2C (Server -> Client): Entity Properties / Attributes.
 *
 * Same as {@link EntityPropertiesPacket} but with a modifier list added per property.
 * v74 added [short modifierCount + modifiers] after each property's double value.
 * Confirmed by ViaLegacy's Protocolr1_6_1Tor1_6_2 which appends short(0) when
 * translating v73 → v74.
 *
 * Wire format (v74+ / 1.6.2+):
 *   [int]     entity ID
 *   [int]     property count
 *   for each property:
 *     [String16] key (e.g., "generic.movementSpeed")
 *     [double]   base value
 *     [short]    modifier count
 *     for each modifier:
 *       [long]   UUID most significant bits
 *       [long]   UUID least significant bits
 *       [double] amount
 *       [byte]   operation
 */
public class EntityPropertiesPacketV74 implements Packet {

    private int entityId;
    private String key;
    private double value;

    public EntityPropertiesPacketV74() {}

    /**
     * Create a single-property packet with no modifiers.
     */
    public EntityPropertiesPacketV74(int entityId, String key, double value) {
        this.entityId = entityId;
        this.key = key;
        this.value = value;
    }

    @Override
    public int getPacketId() {
        return 0x2C;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(1); // 1 property
        McDataTypes.writeString16(buf, key);
        buf.writeDouble(value);
        buf.writeShort(0); // no modifiers
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            key = McDataTypes.readString16(buf);
            value = buf.readDouble();
            int modCount = buf.readUnsignedShort();
            for (int j = 0; j < modCount; j++) {
                buf.readLong();  // UUID msb
                buf.readLong();  // UUID lsb
                buf.readDouble(); // amount
                buf.readByte();  // operation
            }
        }
    }
}
