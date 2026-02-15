package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Beta 1.8+ protocol 0xCA (Client -> Server): Player Abilities.
 *
 * Sent when the player toggles flying (double-jump in creative mode).
 * The server should accept and silently consume this.
 *
 * Wire format:
 *   [boolean] invulnerable
 *   [boolean] flying
 *   [boolean] can fly
 *   [boolean] instant destroy
 */
public class PlayerAbilitiesPacket implements Packet {

    private boolean invulnerable;
    private boolean flying;
    private boolean canFly;
    private boolean instantDestroy;

    public PlayerAbilitiesPacket() {}

    @Override
    public int getPacketId() {
        return 0xCA;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeBoolean(invulnerable);
        buf.writeBoolean(flying);
        buf.writeBoolean(canFly);
        buf.writeBoolean(instantDestroy);
    }

    @Override
    public void read(ByteBuf buf) {
        invulnerable = buf.readBoolean();
        flying = buf.readBoolean();
        canFly = buf.readBoolean();
        instantDestroy = buf.readBoolean();
    }

    public boolean isInvulnerable() { return invulnerable; }
    public boolean isFlying() { return flying; }
    public boolean isCanFly() { return canFly; }
    public boolean isInstantDestroy() { return instantDestroy; }
}
