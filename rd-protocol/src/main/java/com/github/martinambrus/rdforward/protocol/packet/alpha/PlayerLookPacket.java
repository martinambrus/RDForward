package com.github.martinambrus.rdforward.protocol.packet.alpha;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.buffer.ByteBuf;

/**
 * Alpha protocol 0x0C (Client -> Server): Player Look.
 *
 * Sent when look direction changes but position doesn't.
 * Yaw and pitch are in degrees (float), unlike Classic's byte rotation.
 *
 * Wire format (9 bytes payload):
 *   [float]   yaw (degrees)
 *   [float]   pitch (degrees)
 *   [boolean] on ground
 */
public class PlayerLookPacket implements Packet {

    private float yaw;
    private float pitch;
    private boolean onGround;

    public PlayerLookPacket() {}

    public PlayerLookPacket(float yaw, float pitch, boolean onGround) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public int getPacketId() {
        return 0x0C;
    }

    @Override
    public void write(ByteBuf buf) {
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
        buf.writeBoolean(onGround);
    }

    @Override
    public void read(ByteBuf buf) {
        yaw = buf.readFloat();
        pitch = buf.readFloat();
        onGround = buf.readBoolean();
    }

    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }
    public boolean isOnGround() { return onGround; }
}
