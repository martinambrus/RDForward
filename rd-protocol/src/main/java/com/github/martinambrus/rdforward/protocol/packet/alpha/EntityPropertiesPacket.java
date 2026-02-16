package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.6.1 (v73) protocol 0x2C (Server -> Client): Entity Properties / Attributes.
 *
 * Sends entity attribute values (e.g., movement speed, max health).
 * Used by the 1.6.1+ client to set the player's base movement speed —
 * without this packet, the client defaults to EntityLivingBase's base
 * of 0.7 instead of the correct 0.1, causing 7x movement speed.
 *
 * Wire format (v73 / 1.6.1 — NO modifier list):
 *   [int]     entity ID
 *   [int]     property count
 *   for each property:
 *     [String16] key (e.g., "generic.movementSpeed")
 *     [double]   base value
 *
 * Note: The modifier list (short count + UUID/amount/operation per modifier)
 * was added in v74 (1.6.2). Confirmed by ViaLegacy's Protocolr1_6_1Tor1_6_2
 * which explicitly appends short(0) when translating v73 → v74.
 */
public class EntityPropertiesPacket implements Packet {

    private int entityId;
    private String key;
    private double value;

    public EntityPropertiesPacket() {}

    /**
     * Create a single-property packet with no modifiers.
     */
    public EntityPropertiesPacket(int entityId, String key, double value) {
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
        // v73 has NO modifier count field — it was added in v74 (1.6.2).
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            key = McDataTypes.readString16(buf);
            value = buf.readDouble();
            // v73 has NO modifier count field.
        }
    }
}
