package com.github.martinambrus.rdforward.client;

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
 * Client-side packet handler for multiplayer connections.
 *
 * Receives packets from the server and updates the shared
 * MultiplayerState, which the game thread reads during rendering.
 *
 * Handles the full Classic login sequence:
 *   1. Receive ServerIdentification (0x00)
 *   2. Receive LevelInitialize (0x02) — start accumulating world data
 *   3. Receive LevelDataChunk (0x03) x N — collect compressed world chunks
 *   4. Receive LevelFinalize (0x04) — decompress and set world dimensions
 *   5. Receive SpawnPlayer (0x07) with ID -1 (self) + other players
 *   6. Normal gameplay: block changes, position updates, chat
 */
public class ClientConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private final MultiplayerState state = MultiplayerState.getInstance();
    private ByteArrayOutputStream levelDataBuffer;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof ServerIdentificationPacket) {
            handleServerIdentification((ServerIdentificationPacket) packet);
        } else if (packet instanceof PingPacket) {
            // Keep-alive: no response needed in Classic protocol
        } else if (packet instanceof LevelInitializePacket) {
            handleLevelInitialize();
        } else if (packet instanceof LevelDataChunkPacket) {
            handleLevelDataChunk((LevelDataChunkPacket) packet);
        } else if (packet instanceof LevelFinalizePacket) {
            handleLevelFinalize((LevelFinalizePacket) packet);
        } else if (packet instanceof SetBlockServerPacket) {
            handleSetBlock((SetBlockServerPacket) packet);
        } else if (packet instanceof SpawnPlayerPacket) {
            handleSpawnPlayer((SpawnPlayerPacket) packet);
        } else if (packet instanceof PlayerTeleportPacket) {
            handlePlayerTeleport((PlayerTeleportPacket) packet);
        } else if (packet instanceof PositionOrientationUpdatePacket) {
            handlePositionOrientationUpdate((PositionOrientationUpdatePacket) packet);
        } else if (packet instanceof PositionUpdatePacket) {
            handlePositionUpdate((PositionUpdatePacket) packet);
        } else if (packet instanceof OrientationUpdatePacket) {
            handleOrientationUpdate((OrientationUpdatePacket) packet);
        } else if (packet instanceof DespawnPlayerPacket) {
            handleDespawnPlayer((DespawnPlayerPacket) packet);
        } else if (packet instanceof MessagePacket) {
            handleMessage((MessagePacket) packet);
        } else if (packet instanceof DisconnectPacket) {
            handleDisconnect(ctx, (DisconnectPacket) packet);
        } else if (packet instanceof UpdateUserTypePacket) {
            handleUpdateUserType((UpdateUserTypePacket) packet);
        }
    }

    private void handleServerIdentification(ServerIdentificationPacket packet) {
        state.setServerName(packet.getServerName());
        state.setServerMotd(packet.getServerMotd());
        System.out.println("Connected to server: " + packet.getServerName());
        System.out.println("MOTD: " + packet.getServerMotd());
    }

    private void handleLevelInitialize() {
        levelDataBuffer = new ByteArrayOutputStream();
        System.out.println("Receiving world data...");
    }

    private void handleLevelDataChunk(LevelDataChunkPacket packet) {
        if (levelDataBuffer == null) return;
        byte[] data = packet.getChunkData();
        int length = packet.getChunkLength();
        levelDataBuffer.write(data, 0, length);
    }

    private void handleLevelFinalize(LevelFinalizePacket packet) {
        if (levelDataBuffer == null) return;

        int width = packet.getXSize();
        int height = packet.getYSize();
        int depth = packet.getZSize();

        try {
            byte[] compressed = levelDataBuffer.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
            GZIPInputStream gzip = new GZIPInputStream(bais);
            DataInputStream dis = new DataInputStream(gzip);

            int volume = dis.readInt();
            byte[] blocks = new byte[volume];
            dis.readFully(blocks);
            dis.close();

            // Classic sends blocks in XZY order, convert to our internal YZX order
            // (matching RubyDung's Level indexing: (y * depth + z) * width + x)
            byte[] reordered = new byte[width * height * depth];
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    for (int y = 0; y < height; y++) {
                        int classicIndex = (x * depth + z) * height + y;
                        int internalIndex = (y * depth + z) * width + x;
                        reordered[internalIndex] = blocks[classicIndex];
                    }
                }
            }

            state.setWorldData(reordered, width, height, depth);
            System.out.println("World loaded: " + width + "x" + height + "x" + depth
                + " (" + volume + " blocks)");
        } catch (IOException e) {
            System.err.println("Failed to decompress world data: " + e.getMessage());
        } finally {
            levelDataBuffer = null;
        }
    }

    private void handleSetBlock(SetBlockServerPacket packet) {
        // Confirm any pending prediction at this position (server accepted the change)
        state.confirmPrediction(packet.getX(), packet.getY(), packet.getZ());
        state.queueBlockChange(packet.getX(), packet.getY(), packet.getZ(), (byte) packet.getBlockType());
    }

    private void handleSpawnPlayer(SpawnPlayerPacket packet) {
        int id = packet.getPlayerId();
        if (id == SpawnPlayerPacket.SELF_ID) {
            System.out.println("Spawned at: (" + (packet.getX() / 32.0) + ", "
                    + (packet.getY() / 32.0) + ", " + (packet.getZ() / 32.0) + ")");
            // Queue self-teleport so the mixin moves the local player to the server's spawn
            state.queueSelfTeleport(packet.getX(), packet.getY(), packet.getZ());
            return;
        }

        state.addRemotePlayer(
            (byte) id, packet.getPlayerName(),
            packet.getX(), packet.getY(), packet.getZ(),
            (byte) packet.getYaw(), (byte) packet.getPitch()
        );
        System.out.println("Player joined: " + packet.getPlayerName() + " (ID " + id + ")");
    }

    private void handlePlayerTeleport(PlayerTeleportPacket packet) {
        int rawId = packet.getPlayerId();
        // Server correcting our position (ID -1 = self)
        if (rawId == -1) {
            state.queueSelfTeleport(packet.getX(), packet.getY(), packet.getZ());
            return;
        }
        byte id = (byte) rawId;
        RemotePlayer player = state.getRemotePlayer(id);
        if (player != null) {
            player.updatePosition(
                packet.getX(), packet.getY(), packet.getZ(),
                (byte) packet.getYaw(), (byte) packet.getPitch()
            );
        }
    }

    private void handlePositionOrientationUpdate(PositionOrientationUpdatePacket packet) {
        byte id = (byte) packet.getPlayerId();
        RemotePlayer player = state.getRemotePlayer(id);
        if (player != null) {
            player.updatePosition(
                (short) (player.getX() + packet.getChangeX()),
                (short) (player.getY() + packet.getChangeY()),
                (short) (player.getZ() + packet.getChangeZ()),
                (byte) packet.getYaw(), (byte) packet.getPitch()
            );
        }
    }

    private void handlePositionUpdate(PositionUpdatePacket packet) {
        byte id = (byte) packet.getPlayerId();
        RemotePlayer player = state.getRemotePlayer(id);
        if (player != null) {
            player.updatePosition(
                (short) (player.getX() + packet.getChangeX()),
                (short) (player.getY() + packet.getChangeY()),
                (short) (player.getZ() + packet.getChangeZ()),
                player.getYaw(), player.getPitch()
            );
        }
    }

    private void handleOrientationUpdate(OrientationUpdatePacket packet) {
        byte id = (byte) packet.getPlayerId();
        RemotePlayer player = state.getRemotePlayer(id);
        if (player != null) {
            player.updatePosition(
                player.getX(), player.getY(), player.getZ(),
                (byte) packet.getYaw(), (byte) packet.getPitch()
            );
        }
    }

    private void handleDespawnPlayer(DespawnPlayerPacket packet) {
        byte id = (byte) packet.getPlayerId();
        RemotePlayer removed = state.getRemotePlayer(id);
        if (removed != null) {
            System.out.println("Player left: " + removed.getName());
        }
        state.removeRemotePlayer(id);
    }

    private void handleMessage(MessagePacket packet) {
        String message = packet.getMessage();
        state.queueChatMessage(message);
        System.out.println("[Chat] " + message);
    }

    private void handleDisconnect(ChannelHandlerContext ctx, DisconnectPacket packet) {
        System.out.println("Disconnected: " + packet.getReason());
        state.setConnected(false);
        ctx.close();
    }

    private void handleUpdateUserType(UpdateUserTypePacket packet) {
        System.out.println("User type updated: "
            + (packet.getUserType() == UpdateUserTypePacket.USER_TYPE_OP ? "Op" : "Normal"));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        state.setConnected(true);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        state.setConnected(false);
        System.out.println("Disconnected from server.");
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("Network error: " + cause.getMessage());
        ctx.close();
    }
}
