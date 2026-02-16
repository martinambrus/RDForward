package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.1+ protocol 0x09 (Client -> Server): Respawn.
 *
 * Release 1.1 (v23) appended a String16 levelType field at the end.
 *
 * Wire format:
 *   [byte]     dimension (0 = overworld, -1 = nether)
 *   [byte]     difficulty (0-3)
 *   [byte]     game mode (0 = survival, 1 = creative)
 *   [short]    world height (128)
 *   [long]     map seed
 *   [string16] level type ("default")
 */
public class RespawnPacketV23 extends RespawnPacketV17 {

    private String levelType;

    public RespawnPacketV23() {}

    @Override
    public void write(ByteBuf buf) {
        super.write(buf);
        McDataTypes.writeStringAdaptive(buf, levelType != null ? levelType : "default");
    }

    @Override
    public void read(ByteBuf buf) {
        super.read(buf);
        levelType = McDataTypes.readStringAdaptive(buf);
    }

    public String getLevelType() { return levelType; }
}
