package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x16 (Server -> Client): Collect Item.
 *
 * Tells the client that an item entity was collected by a player.
 * The client plays the pickup animation and (for the local player)
 * adds the item to inventory based on the original PickupSpawn data.
 *
 * Wire format (8 bytes payload):
 *   [int] collected entity ID (the item entity)
 *   [int] collector entity ID (the player who picked it up)
 */
public class CollectItemPacket implements Packet {

    private int collectedEntityId;
    private int collectorEntityId;

    public CollectItemPacket() {}

    public CollectItemPacket(int collectedEntityId, int collectorEntityId) {
        this.collectedEntityId = collectedEntityId;
        this.collectorEntityId = collectorEntityId;
    }

    @Override
    public int getPacketId() {
        return 0x16;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(collectedEntityId);
        buf.writeInt(collectorEntityId);
    }

    @Override
    public void read(ByteBuf buf) {
        collectedEntityId = buf.readInt();
        collectorEntityId = buf.readInt();
    }

    public int getCollectedEntityId() { return collectedEntityId; }
    public int getCollectorEntityId() { return collectorEntityId; }
}
