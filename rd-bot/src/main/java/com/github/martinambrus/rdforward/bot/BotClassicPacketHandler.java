package com.github.martinambrus.rdforward.bot;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Netty inbound handler for Classic/RubyDung bot clients.
 * Uses the Nati-framed protocol (4-byte length prefix) and Classic packet set.
 *
 * Login flow:
 *   C2S: PlayerIdentificationPacket (0x00)
 *   S2C: ServerIdentificationPacket (0x00)
 *   S2C: LevelInitialize (0x02)
 *   S2C: LevelDataChunk (0x03) x N
 *   S2C: LevelFinalize (0x04)
 *   S2C: SpawnPlayerPacket (0x07) with ID -1 (self)
 *   Login complete.
 */
public class BotClassicPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final ProtocolVersion version;
    private final String username;
    private volatile BotSession session;
    private final CountDownLatch sessionReady = new CountDownLatch(1);

    public BotClassicPacketHandler(ProtocolVersion version, String username) {
        this.version = version;
        this.username = username;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        session = new BotSession(ctx.channel(), version);
        sessionReady.countDown();

        // Send PlayerIdentification (Classic login)
        ctx.writeAndFlush(new PlayerIdentificationPacket(
                version.getVersionNumber(), username, ""));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        session.recordPacket(packet);

        if (packet instanceof SpawnPlayerPacket sp) {
            // Fixed-point to double: divide by 32
            double x = sp.getX() / 32.0;
            double y = sp.getY() / 32.0;
            double z = sp.getZ() / 32.0;

            if (sp.getPlayerId() == SpawnPlayerPacket.SELF_ID) {
                // Self-spawn: record position and mark login complete
                session.recordPosition(x, y, z, sp.getYaw(), sp.getPitch());
                session.markLoginComplete();
            } else {
                session.recordSpawnPlayer(sp.getPlayerId(), sp.getPlayerName().trim());
            }
        } else if (packet instanceof MessagePacket msg) {
            session.recordChat(msg.getMessage().trim());
        } else if (packet instanceof SetBlockServerPacket sb) {
            session.recordBlockChange(sb.getX(), sb.getY(), sb.getZ(), sb.getBlockType());
        } else if (packet instanceof PlayerTeleportPacket tp) {
            if (tp.getPlayerId() == -1) {
                double x = tp.getX() / 32.0;
                double y = tp.getY() / 32.0;
                double z = tp.getZ() / 32.0;
                session.recordPosition(x, y, z, tp.getYaw(), tp.getPitch());
            }
        } else if (packet instanceof DespawnPlayerPacket dp) {
            session.recordDespawn(dp.getPlayerId());
        } else if (packet instanceof PingPacket) {
            // Respond to keep-alive pings
            ctx.writeAndFlush(new PingPacket());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("BotClassicPacketHandler error (" + version + "): " + cause.getMessage());
        ctx.close();
    }

    public BotSession awaitSession(long timeoutMs) throws InterruptedException {
        sessionReady.await(timeoutMs, TimeUnit.MILLISECONDS);
        return session;
    }

    public BotSession getSession() {
        return session;
    }
}
