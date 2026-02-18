package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.15 Play state, S2C packet 0x08: Acknowledge Player Digging.
 *
 * New mandatory S2C packet in 1.15 — confirms block digging actions.
 * Without this, the 1.15 client reverts broken blocks after a timeout.
 *
 * Wire format:
 *   [Position] location (packed long, 1.14 encoding)
 *   [VarInt]   block state ID (after the action)
 *   [VarInt]   status (matches C2S digging status)
 *   [boolean]  successful
 */
public class AcknowledgePlayerDiggingPacketV573 implements Packet {

    private int x;
    private int y;
    private int z;
    private int blockStateId;
    private int status;
    private boolean successful;

    public AcknowledgePlayerDiggingPacketV573() {}

    public AcknowledgePlayerDiggingPacketV573(int x, int y, int z,
                                               int blockStateId, int status,
                                               boolean successful) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockStateId = blockStateId;
        this.status = status;
        this.successful = successful;
    }

    @Override
    public int getPacketId() { return 0x08; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writePositionV477(buf, x, y, z);
        McDataTypes.writeVarInt(buf, blockStateId);
        McDataTypes.writeVarInt(buf, status);
        buf.writeBoolean(successful);
    }

    @Override
    public void read(ByteBuf buf) {
        int[] pos = McDataTypes.readPositionV477(buf);
        x = pos[0];
        y = pos[1];
        z = pos[2];
        blockStateId = McDataTypes.readVarInt(buf);
        status = McDataTypes.readVarInt(buf);
        successful = buf.readBoolean();
    }
}
