package com.github.martinambrus.rdforward.android.multiplayer;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Client-side packet handler for Android multiplayer connections.
 * Mirrors the desktop rd-client ClientConnectionHandler.
 */
public class ClientConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private final MultiplayerState state = MultiplayerState.getInstance();
    private ByteArrayOutputStream levelDataBuffer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof ServerIdentificationPacket p) {
            state.setServerName(p.getServerName());
            state.setServerMotd(p.getServerMotd());
        } else if (packet instanceof PingPacket) {
            // keep-alive
        } else if (packet instanceof LevelInitializePacket) {
            levelDataBuffer = new ByteArrayOutputStream();
        } else if (packet instanceof LevelDataChunkPacket p) {
            if (levelDataBuffer != null) levelDataBuffer.write(p.getChunkData(), 0, p.getChunkLength());
        } else if (packet instanceof LevelFinalizePacket p) {
            handleLevelFinalize(p);
        } else if (packet instanceof SetBlockServerPacket p) {
            state.confirmPrediction(p.getX(), p.getY(), p.getZ());
            state.queueBlockChange(p.getX(), p.getY(), p.getZ(), (byte) p.getBlockType());
        } else if (packet instanceof SpawnPlayerPacket p) {
            handleSpawnPlayer(p);
        } else if (packet instanceof PlayerTeleportPacket p) {
            handlePlayerTeleport(p);
        } else if (packet instanceof PositionOrientationUpdatePacket p) {
            handlePosOrientUpdate(p);
        } else if (packet instanceof PositionUpdatePacket p) {
            handlePosUpdate(p);
        } else if (packet instanceof OrientationUpdatePacket p) {
            handleOrientUpdate(p);
        } else if (packet instanceof DespawnPlayerPacket p) {
            state.removeRemotePlayer((byte) p.getPlayerId());
        } else if (packet instanceof MessagePacket p) {
            state.queueChatMessage(p.getMessage());
        } else if (packet instanceof DisconnectPacket) {
            state.setConnected(false);
            ctx.close();
        }
    }

    private void handleLevelFinalize(LevelFinalizePacket packet) {
        if (levelDataBuffer == null) return;
        int width = packet.getXSize(), height = packet.getYSize(), depth = packet.getZSize();
        try {
            byte[] compressed = levelDataBuffer.toByteArray();
            DataInputStream dis = new DataInputStream(
                    new GZIPInputStream(new ByteArrayInputStream(compressed)));
            int volume = dis.readInt();
            byte[] blocks = new byte[volume];
            dis.readFully(blocks);
            dis.close();

            // Classic XZY → internal YZX order
            byte[] reordered = new byte[width * height * depth];
            for (int x = 0; x < width; x++)
                for (int z = 0; z < depth; z++)
                    for (int y = 0; y < height; y++) {
                        reordered[(y * depth + z) * width + x] =
                                blocks[(x * depth + z) * height + y];
                    }
            state.setWorldData(reordered, width, height, depth);
        } catch (IOException e) {
            System.err.println("Failed to decompress world: " + e.getMessage());
        } finally {
            levelDataBuffer = null;
        }
    }

    private void handleSpawnPlayer(SpawnPlayerPacket p) {
        if (p.getPlayerId() == SpawnPlayerPacket.SELF_ID) {
            state.queueSelfTeleport(p.getX(), p.getY(), p.getZ());
            return;
        }
        state.addRemotePlayer((byte) p.getPlayerId(), p.getPlayerName(),
                p.getX(), p.getY(), p.getZ(), (byte) p.getYaw(), (byte) p.getPitch());
    }

    private void handlePlayerTeleport(PlayerTeleportPacket p) {
        if (p.getPlayerId() == -1) {
            state.queueSelfTeleport(p.getX(), p.getY(), p.getZ());
            return;
        }
        RemotePlayer rp = state.getRemotePlayer((byte) p.getPlayerId());
        if (rp != null) rp.updatePosition(p.getX(), p.getY(), p.getZ(),
                (byte) p.getYaw(), (byte) p.getPitch());
    }

    private void handlePosOrientUpdate(PositionOrientationUpdatePacket p) {
        RemotePlayer rp = state.getRemotePlayer((byte) p.getPlayerId());
        if (rp != null) rp.updatePosition(
                (short) (rp.getX() + p.getChangeX()), (short) (rp.getY() + p.getChangeY()),
                (short) (rp.getZ() + p.getChangeZ()), (byte) p.getYaw(), (byte) p.getPitch());
    }

    private void handlePosUpdate(PositionUpdatePacket p) {
        RemotePlayer rp = state.getRemotePlayer((byte) p.getPlayerId());
        if (rp != null) rp.updatePosition(
                (short) (rp.getX() + p.getChangeX()), (short) (rp.getY() + p.getChangeY()),
                (short) (rp.getZ() + p.getChangeZ()), rp.getYaw(), rp.getPitch());
    }

    private void handleOrientUpdate(OrientationUpdatePacket p) {
        RemotePlayer rp = state.getRemotePlayer((byte) p.getPlayerId());
        if (rp != null) rp.updatePosition(rp.getX(), rp.getY(), rp.getZ(),
                (byte) p.getYaw(), (byte) p.getPitch());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        state.setConnected(true);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        state.setConnected(false);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Network error: " + cause.getMessage());
        ctx.close();
    }
}
