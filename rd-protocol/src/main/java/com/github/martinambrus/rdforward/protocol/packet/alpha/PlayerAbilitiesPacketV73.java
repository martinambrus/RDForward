package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.6.1+ protocol 0xCA (bidirectional): Player Abilities.
 *
 * Changed from byte speeds (v39-v61) to float speeds.
 *
 * Wire format:
 *   [byte] flags (bitmask: 1=invulnerable, 2=flying, 4=allowFlying, 8=creativeMode)
 *   [float] fly speed (default: 0.05f)
 *   [float] walk speed (default: 0.1f)
 */
public class PlayerAbilitiesPacketV73 implements Packet {

    private byte flags;
    private float flySpeed;
    private float walkSpeed;

    public PlayerAbilitiesPacketV73() {}

    public PlayerAbilitiesPacketV73(int flags, float flySpeed, float walkSpeed) {
        this.flags = (byte) flags;
        this.flySpeed = flySpeed;
        this.walkSpeed = walkSpeed;
    }

    @Override
    public int getPacketId() {
        return 0xCA;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(flags);
        buf.writeFloat(flySpeed);
        buf.writeFloat(walkSpeed);
    }

    @Override
    public void read(ByteBuf buf) {
        flags = buf.readByte();
        flySpeed = buf.readFloat();
        walkSpeed = buf.readFloat();
    }

    public byte getFlags() { return flags; }
    public float getFlySpeed() { return flySpeed; }
    public float getWalkSpeed() { return walkSpeed; }
}
