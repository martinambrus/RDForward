package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 1.8 Play state, C2S packet 0x02: Use Entity.
 *
 * 1.8 changed entityId from int to VarInt, action from byte to VarInt,
 * and added conditional target xyz for INTERACT_AT (action=2).
 *
 * Wire format:
 *   [VarInt] targetEntityId
 *   [VarInt] action (0=interact, 1=attack, 2=interact_at)
 *   if action == 2: [float] targetX, [float] targetY, [float] targetZ
 */
public class NettyUseEntityPacketV47 implements Packet {

    public NettyUseEntityPacketV47() {}

    @Override
    public int getPacketId() { return 0x02; }

    @Override
    public void write(ByteBuf buf) {}

    @Override
    public void read(ByteBuf buf) {
        McDataTypes.readVarInt(buf); // targetEntityId
        int action = McDataTypes.readVarInt(buf);
        if (action == 2) {
            buf.skipBytes(12); // 3 floats: targetX, targetY, targetZ
        }
    }
}
