package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.9 Play state, C2S packet 0x00: Teleport Confirm.
 *
 * New packet in 1.9. Mandatory response to S2C PlayerPositionAndLook.
 *
 * Wire format:
 *   [VarInt] teleportId
 */
public class TeleportConfirmPacketV109 implements Packet {

    private int teleportId;

    public TeleportConfirmPacketV109() {}

    public TeleportConfirmPacketV109(int teleportId) {
        this.teleportId = teleportId;
    }

    @Override
    public int getPacketId() { return 0x00; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, teleportId);
    }

    @Override
    public void read(ByteBuf buf) {
        teleportId = McDataTypes.readVarInt(buf);
    }

    public int getTeleportId() { return teleportId; }
}
