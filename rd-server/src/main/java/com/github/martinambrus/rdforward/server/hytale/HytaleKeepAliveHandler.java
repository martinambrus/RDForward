package com.github.martinambrus.rdforward.server.hytale;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Sends periodic Ping (ID 2) packets and monitors Pong (ID 3) responses.
 *
 * Ping (S2C, ID 2): 29 bytes fixed
 *   nullBits(1) + id(4) + InstantData(12) + lastPingValueRaw(4)
 *   + lastPingValueDirect(4) + lastPingValueTick(4)
 *
 * Pong (C2S, ID 3): 20 bytes fixed
 *   nullBits(1) + id(4) + InstantData(12) + type(1) + packetQueueSize(2)
 *
 * Disconnects the client if no pong is received within the timeout period.
 */
public class HytaleKeepAliveHandler {

    private static final int PING_INTERVAL_SECONDS = 10;
    private static final int TIMEOUT_SECONDS = HytaleProtocolConstants.IDLE_TIMEOUT_SECONDS;

    private final HytaleSession session;
    private ScheduledFuture<?> pingTask;
    private int nextPingId = 1;

    public HytaleKeepAliveHandler(HytaleSession session) {
        this.session = session;
    }

    /** Start periodic pinging on the given channel context. */
    public void start(ChannelHandlerContext ctx) {
        pingTask = ctx.executor().scheduleAtFixedRate(() -> {
            if (!session.isActive()) {
                stop();
                return;
            }

            // Check for timeout
            long elapsed = System.currentTimeMillis() - session.getLastPongTime();
            if (elapsed > TIMEOUT_SECONDS * 1000L) {
                System.out.println("[Hytale] " + session.getUsername()
                        + " timed out (no pong for " + (elapsed / 1000) + "s)");
                session.disconnect("Timed out");
                stop();
                return;
            }

            // Send Ping (ID 2)
            sendPing(ctx);
        }, PING_INTERVAL_SECONDS, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    /** Send a Ping packet. */
    private void sendPing(ChannelHandlerContext ctx) {
        int pingId = nextPingId++;
        session.setLastPingId(pingId);

        HytalePacketBuffer pkt = HytalePacketBuffer.create(
                HytaleProtocolConstants.PACKET_PING, ctx.alloc(), 29);

        pkt.writeByte(0);       // nullBits (no time)
        pkt.writeIntLE(pingId); // id

        // InstantData (12 bytes) — zeroed (nullable, bit not set)
        pkt.writeZeroes(12);

        pkt.writeIntLE(0); // lastPingValueRaw
        pkt.writeIntLE(0); // lastPingValueDirect
        pkt.writeIntLE(0); // lastPingValueTick

        session.sendPacket(pkt);
    }

    /** Stop the periodic ping task. */
    public void stop() {
        if (pingTask != null) {
            pingTask.cancel(false);
            pingTask = null;
        }
    }
}
