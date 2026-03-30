package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.server.bedrock.BedrockSessionWrapper;
import com.github.martinambrus.rdforward.server.mcpe.MCPESessionWrapper;
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
    private final String uuid;
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

    // Legacy MCPE session wrapper (null for non-MCPE clients)
    private volatile MCPESessionWrapper mcpeSession;

    // MCPE skin data (null for non-MCPE clients; raw RGBA bytes, 64x64 or 64x32)
    private volatile byte[] mcpeSkinData;
    private volatile int mcpeSkinSlim;

    // Double-precision position for Alpha clients (block coordinates)
    private volatile double doubleX;
    private volatile double doubleY;
    private volatile double doubleZ;
    private volatile float floatYaw;
    private volatile float floatPitch;

    // RTT tracking (used for keep-alive timeout detection and graceful degradation)
    private volatile long rttMillis = 0;
    private volatile long keepAliveSentNanos = 0;
    private volatile long lastKeepAliveResponseTime = System.currentTimeMillis();
    private volatile int lastRttTier = 0;

    // Delta compression — last broadcast position (fixed-point)
    private volatile short lastBroadcastX;
    private volatile short lastBroadcastY;
    private volatile short lastBroadcastZ;
    private volatile byte lastBroadcastYaw;
    private volatile byte lastBroadcastPitch;
    private volatile boolean hasBroadcastPosition = false;

    // RTT-based entity update throttling
    private volatile int entityUpdateThrottleCounter = 0;

    // Adaptive chunk send rate (EMA, alpha=0.1). Starts at 4 chunks/tick.
    private volatile double chunkSendRate = 4.0;

    // Alphaver client flag (based on Alpha 1.0.16 with modified packet formats)
    private volatile boolean alphaverClient = false;

    // Teleport grace: skip chunk-boundary checks on movement packets for a
    // short duration after teleport, giving old clients time to process the
    // position + chunk data. Prevents false "stuck at unloaded chunk" kicks.
    private volatile long teleportGraceUntil = 0;

    public ConnectedPlayer(byte playerId, String username, String uuid, Channel channel, ProtocolVersion protocolVersion) {
        this.playerId = playerId;
        this.username = username;
        this.uuid = uuid;
        this.channel = channel;
        this.protocolVersion = protocolVersion;
    }

    public void sendPacket(Packet packet) {
        if (sendViaNonTcp(packet)) return;
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet);
        }
    }

    /**
     * Write a packet without flushing. Use with {@link #flushPackets()}
     * to batch multiple writes, allowing the {@link PrioritizingOutboundHandler}
     * to reorder them by priority before flushing to the network.
     */
    public void writePacket(Packet packet) {
        if (sendViaNonTcp(packet)) return;
        if (channel != null && channel.isActive()) {
            channel.write(packet);
        }
    }

    /** Route packet to Bedrock/MCPE transport if applicable. Returns true if handled. */
    private boolean sendViaNonTcp(Packet packet) {
        if (bedrockSession != null) {
            bedrockSession.translateAndSend(packet);
            return true;
        }
        if (mcpeSession != null) {
            mcpeSession.translateAndSend(packet);
            return true;
        }
        return false;
    }

    /**
     * Flush all buffered writes. Called after a batch of {@link #writePacket}
     * calls to trigger the {@link PrioritizingOutboundHandler} to sort and send.
     */
    public void flushPackets() {
        if (channel != null && channel.isActive()) {
            channel.flush();
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
        if (mcpeSession != null) {
            mcpeSession.disconnect("Disconnected");
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

    public void setMcpeSession(MCPESessionWrapper session) {
        this.mcpeSession = session;
    }

    public MCPESessionWrapper getMcpeSession() {
        return mcpeSession;
    }

    public void setMcpeSkin(int slim, byte[] skinData) {
        this.mcpeSkinSlim = slim;
        this.mcpeSkinData = skinData;
    }

    public byte[] getMcpeSkinData() { return mcpeSkinData; }
    public int getMcpeSkinSlim() { return mcpeSkinSlim; }

    public byte getPlayerId() { return playerId; }
    public String getUsername() { return username; }
    public String getUuid() { return uuid; }
    public Channel getChannel() { return channel; }
    public ProtocolVersion getProtocolVersion() { return protocolVersion; }
    public boolean isAlphaverClient() { return alphaverClient; }
    public void setAlphaverClient(boolean alphaverClient) { this.alphaverClient = alphaverClient; }

    /**
     * Check if the player is currently in teleport grace period.
     * During grace, movement handlers should skip chunk-boundary checks
     * but still accept the movement (don't drop packets).
     */
    public boolean isInTeleportGrace() {
        return System.currentTimeMillis() < teleportGraceUntil;
    }

    /** Start teleport grace for the given duration in milliseconds. */
    public void setTeleportGrace(long durationMs) {
        teleportGraceUntil = System.currentTimeMillis() + durationMs;
    }
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

    // --- RTT tracking ---

    public void setKeepAliveSentNanos(long nanos) { this.keepAliveSentNanos = nanos; }
    public long getKeepAliveSentNanos() { return keepAliveSentNanos; }

    public void setLastKeepAliveResponseTime(long millis) { this.lastKeepAliveResponseTime = millis; }
    public long getLastKeepAliveResponseTime() { return lastKeepAliveResponseTime; }

    /**
     * Update RTT using exponential moving average (weight 7/8 old, 1/8 new).
     * Call with the nanoTime recorded when the keep-alive was sent.
     * Logs tier transitions for observability.
     */
    public void updateRtt(long sentNanos) {
        long measured = (System.nanoTime() - sentNanos) / 1_000_000;
        if (measured < 0) measured = 0;
        rttMillis = (rttMillis == 0) ? measured : (rttMillis * 7 + measured) / 8;

        int newTier = getRttTier();
        int oldTier = lastRttTier;
        if (newTier != oldTier) {
            lastRttTier = newTier;
            if (newTier > oldTier) {
                System.out.println("[RTT] " + username + " degraded to tier " + newTier
                        + " (rtt=" + rttMillis + "ms)");
            } else {
                System.out.println("[RTT] " + username + " recovered to tier " + newTier
                        + " (rtt=" + rttMillis + "ms)");
            }
        }
    }

    public long getRttMillis() { return rttMillis; }

    /**
     * Get RTT tier for graceful degradation.
     * 0 = normal (< 150ms), 1 = degraded (150-499ms), 2 = critical (>= 500ms).
     */
    public int getRttTier() {
        long rtt = rttMillis;
        if (rtt >= 500) return 2;
        if (rtt >= 150) return 1;
        return 0;
    }

    // --- Adaptive chunk send rate ---

    /**
     * Get the chunk send budget for this tick based on RTT tier and EMA rate.
     * Tier 0 (normal): up to 8, driven by EMA.
     * Tier 1 (degraded): half the EMA, minimum 1.
     * Tier 2 (critical): 0 (skip sending).
     */
    public int getChunkSendBudget() {
        int tier = getRttTier();
        if (tier >= 2) return 0;
        int ema = (int) chunkSendRate;
        if (tier == 1) return Math.max(1, ema / 2);
        return Math.min(8, ema);
    }

    /**
     * Update the chunk send rate EMA based on how many chunks were actually
     * sent this tick. Alpha=0.1 for smooth adaptation.
     */
    public void updateChunkSendRate(int sentThisTick) {
        chunkSendRate = chunkSendRate * 0.9 + sentThisTick * 0.1;
    }

    // --- Delta compression (last broadcast position) ---

    public boolean hasBroadcastPosition() { return hasBroadcastPosition; }
    public short getLastBroadcastX() { return lastBroadcastX; }
    public short getLastBroadcastY() { return lastBroadcastY; }
    public short getLastBroadcastZ() { return lastBroadcastZ; }
    public byte getLastBroadcastYaw() { return lastBroadcastYaw; }
    public byte getLastBroadcastPitch() { return lastBroadcastPitch; }

    public void updateLastBroadcastPosition(short x, short y, short z, byte yaw, byte pitch) {
        this.lastBroadcastX = x;
        this.lastBroadcastY = y;
        this.lastBroadcastZ = z;
        this.lastBroadcastYaw = yaw;
        this.lastBroadcastPitch = pitch;
        this.hasBroadcastPosition = true;
    }

    // --- Chunk cache center tracking (1.14+) ---

    private volatile int lastChunkCenterX = Integer.MIN_VALUE;
    private volatile int lastChunkCenterZ = Integer.MIN_VALUE;

    public int getLastChunkCenterX() { return lastChunkCenterX; }
    public int getLastChunkCenterZ() { return lastChunkCenterZ; }

    public void setLastChunkCenter(int chunkX, int chunkZ) {
        this.lastChunkCenterX = chunkX;
        this.lastChunkCenterZ = chunkZ;
    }

    // --- RTT-based throttling ---

    public int incrementAndGetThrottleCounter() { return ++entityUpdateThrottleCounter; }
}
