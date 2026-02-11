package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x08 (Server -> Client): Update Health.
 *
 * Sent when the player's health changes (damage, healing, respawn).
 * Health 0 = dead (triggers death screen on client).
 *
 * Wire format (2 bytes payload):
 *   [short] health (0-20, where 20 = full, each point = half heart)
 */
public class UpdateHealthPacket implements Packet {

    private short health;

    public UpdateHealthPacket() {}

    public UpdateHealthPacket(short health) {
        this.health = health;
    }

    @Override
    public int getPacketId() {
        return 0x08;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeShort(health);
    }

    @Override
    public void read(ByteBuf buf) {
        health = buf.readShort();
    }

    public short getHealth() { return health; }
}
