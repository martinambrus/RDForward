package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
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

    public ConnectedPlayer(byte playerId, String username, Channel channel, ProtocolVersion protocolVersion) {
        this.playerId = playerId;
        this.username = username;
        this.channel = channel;
        this.protocolVersion = protocolVersion;
    }

    public void sendPacket(Packet packet) {
        if (channel.isActive()) {
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

    public void disconnect() {
        if (channel.isActive()) {
            channel.close();
        }
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
}
