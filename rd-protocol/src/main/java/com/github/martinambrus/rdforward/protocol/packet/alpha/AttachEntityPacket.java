package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta protocol 0x27 (Server -> Client): Attach Entity.
 *
 * Attaches an entity to a vehicle (e.g. minecart, boat).
 *
 * Wire format (8 bytes payload):
 *   [int] entity ID
 *   [int] vehicle entity ID (-1 = detach)
 */
public class AttachEntityPacket implements Packet {

    private int entityId;
    private int vehicleId;

    public AttachEntityPacket() {}

    public AttachEntityPacket(int entityId, int vehicleId) {
        this.entityId = entityId;
        this.vehicleId = vehicleId;
    }

    @Override
    public int getPacketId() {
        return 0x27;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeInt(vehicleId);
    }

    @Override
    public void read(ByteBuf buf) {
        entityId = buf.readInt();
        vehicleId = buf.readInt();
    }

    public int getEntityId() { return entityId; }
    public int getVehicleId() { return vehicleId; }
}
