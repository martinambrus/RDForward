package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.server.bedrock.BedrockSessionWrapper;
import io.netty.channel.Channel;

/**
 * Represents a connected player on the server.
 *
 * Tracks the player's network channel, assigned ID, name, position,
 * orientation, and protocol version. Player IDs follow the MC Classic
 * convention: signed bytes (-128 to 127), with -1 reserved for "self"
 * in SpawnPlayer packets.
 */
public class ConnectedPlayer {

    private final byte playerId;
    private final String username;
    private final Channel channel;
    private final ProtocolVersion protocolVersion;

    // Position in fixed-point units (multiply by 32 for Classic protocol)
    private volatile short x;
    private volatile short y;
    private volatile short z;
    private volatile byte yaw;
    private volatile byte pitch;

    // Bedrock session wrapper (null for non-Bedrock clients)
    private volatile BedrockSessionWrapper bedrockSession;

    // Double-precision position for Alpha clients (block coordinates)
    private volatile double doubleX;
    private volatile double doubleY;
    private volatile double doubleZ;
    private volatile float floatYaw;
    private volatile float floatPitch;

    public ConnectedPlayer(byte playerId, String username, Channel channel, ProtocolVersion protocolVersion) {
        this.playerId = playerId;
        this.username = username;
        this.channel = channel;
        this.protocolVersion = protocolVersion;
    }

    public void sendPacket(Packet packet) {
        if (bedrockSession != null) {
            bedrockSession.translateAndSend(packet);
            return;
        }
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet);
        }
    }

    public void updatePosition(short x, short y, short z, byte yaw, byte pitch) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /**
     * Update position using double-precision coordinates (Alpha clients).
     * Also updates the fixed-point fields for Classic compatibility.
     */
    public void updatePositionDouble(double x, double y, double z, float yaw, float pitch) {
        this.doubleX = x;
        this.doubleY = y;
        this.doubleZ = z;
        this.floatYaw = yaw;
        this.floatPitch = pitch;
        // Also update fixed-point for Classic compatibility
        this.x = (short) (x * 32);
        this.y = (short) (y * 32);
        this.z = (short) (z * 32);
        this.yaw = (byte) ((yaw / 360.0f) * 256);
        this.pitch = (byte) ((pitch / 360.0f) * 256);
    }

    public void disconnect() {
        if (bedrockSession != null) {
            bedrockSession.disconnect("Disconnected");
            return;
        }
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    public void setBedrockSession(BedrockSessionWrapper session) {
        this.bedrockSession = session;
    }

    public BedrockSessionWrapper getBedrockSession() {
        return bedrockSession;
    }

    public byte getPlayerId() { return playerId; }
    public String getUsername() { return username; }
    public Channel getChannel() { return channel; }
    public ProtocolVersion getProtocolVersion() { return protocolVersion; }
    public short getX() { return x; }
    public short getY() { return y; }
    public short getZ() { return z; }
    public byte getYaw() { return yaw; }
    public byte getPitch() { return pitch; }
    public double getDoubleX() { return doubleX; }
    public double getDoubleY() { return doubleY; }
    public double getDoubleZ() { return doubleZ; }
    public float getFloatYaw() { return floatYaw; }
    public float getFloatPitch() { return floatPitch; }
}
