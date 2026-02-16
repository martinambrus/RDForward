package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Release 1.3.1+ protocol 0xCA (bidirectional): Player Abilities.
 *
 * Changed from 4 booleans to a flags bitmask + speed bytes.
 *
 * Wire format:
 *   [byte] flags (bitmask: 1=invulnerable, 2=flying, 4=allowFlying, 8=creativeMode)
 *   [byte] fly speed (default: (byte)(0.05f * 255) = 12)
 *   [byte] walk speed (default: (byte)(0.1f * 255) = 25)
 */
public class PlayerAbilitiesPacketV39 implements Packet {

    private byte flags;
    private byte flySpeed;
    private byte walkSpeed;

    public PlayerAbilitiesPacketV39() {}

    public PlayerAbilitiesPacketV39(int flags, int flySpeed, int walkSpeed) {
        this.flags = (byte) flags;
        this.flySpeed = (byte) flySpeed;
        this.walkSpeed = (byte) walkSpeed;
    }

    @Override
    public int getPacketId() {
        return 0xCA;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeByte(flags);
        buf.writeByte(flySpeed);
        buf.writeByte(walkSpeed);
    }

    @Override
    public void read(ByteBuf buf) {
        flags = buf.readByte();
        flySpeed = buf.readByte();
        walkSpeed = buf.readByte();
    }

    public byte getFlags() { return flags; }
    public byte getFlySpeed() { return flySpeed; }
    public byte getWalkSpeed() { return walkSpeed; }
}
