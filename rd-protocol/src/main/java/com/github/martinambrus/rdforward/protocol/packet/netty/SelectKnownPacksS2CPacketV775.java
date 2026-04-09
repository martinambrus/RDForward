package com.github.martinambrus.rdforward.protocol.packet.netty;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * 26.1/26.1.1/26.1.2 Configuration state, S2C packet 0x0E: Select Known Packs.
 *
 * v775 clients only recognize their own single "core" pack version (not older
 * per-version packs, and not other 26.x point releases). Each 26.x point
 * release ships its own built-in pack named after its version:
 * "minecraft:core:26.1", "minecraft:core:26.1.1", "minecraft:core:26.1.2".
 * All of them share wire protocol 775, so the server cannot tell them apart
 * from the handshake and must advertise ALL known pack versions. Each client
 * confirms only its own matching pack and resolves null-data registry entries
 * from its own built-in data.
 *
 * Advertising an unknown pack version is a no-op for a client that doesn't
 * have it, so this remains safe for every 26.x point release.
 */
public class SelectKnownPacksS2CPacketV775 implements Packet {

    @Override
    public int getPacketId() { return 0x0E; }

    @Override
    public void write(ByteBuf buf) {
        McDataTypes.writeVarInt(buf, 3);
        // Pack 1: 26.1
        McDataTypes.writeVarIntString(buf, "minecraft");
        McDataTypes.writeVarIntString(buf, "core");
        McDataTypes.writeVarIntString(buf, "26.1");
        // Pack 2: 26.1.1 (chat-reporting hotfix, same protocol 775 as 26.1)
        McDataTypes.writeVarIntString(buf, "minecraft");
        McDataTypes.writeVarIntString(buf, "core");
        McDataTypes.writeVarIntString(buf, "26.1.1");
        // Pack 3: 26.1.2 (exploit + UI hotfix, same protocol 775 as 26.1)
        McDataTypes.writeVarIntString(buf, "minecraft");
        McDataTypes.writeVarIntString(buf, "core");
        McDataTypes.writeVarIntString(buf, "26.1.2");
    }

    @Override
    public void read(ByteBuf buf) {
        // S2C only
    }
}
