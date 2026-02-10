package com.github.martinambrus.rdforward.client;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles packets received from the server on the client side.
 *
 * Routes incoming Classic protocol packets to the appropriate
 * client-side systems (world renderer, player list, chat display, etc.)
 *
 * Expected login sequence from server:
 *   ServerIdentification (0x00) — server info, user type
 *   LevelInitialize (0x02) — world transfer starting
 *   LevelDataChunk (0x03)... — world data chunks
 *   LevelFinalize (0x04) — world transfer complete, dimensions
 *   SpawnPlayer (0x07) with ID -1 — self-spawn with position
 */
public class ClientConnectionHandler extends SimpleChannelInboundHandler<Packet> {

    private final ProtocolVersion clientVersion;
    private boolean loginComplete = false;

    public ClientConnectionHandler(ProtocolVersion clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        if (packet instanceof ServerIdentificationPacket) {
            handleServerIdentification((ServerIdentificationPacket) packet);
        } else if (packet instanceof PingPacket) {
            // Ping requires no response in Classic
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
        // Unknown packet types are silently ignored (forward compatibility)
    }

    private void handleServerIdentification(ServerIdentificationPacket identification) {
        loginComplete = true;
        ProtocolVersion serverVersion = ProtocolVersion.fromNumber(identification.getProtocolVersion());
        String serverName = serverVersion != null ? serverVersion.getDisplayName() : "Unknown";
        System.out.println("Connected to server: " + identification.getServerName()
                + " (protocol: " + serverName + ")");
        System.out.println("MOTD: " + identification.getServerMotd());
        System.out.println("User type: " + (identification.getUserType() == ServerIdentificationPacket.USER_TYPE_OP ? "Op" : "Normal"));
    }

    private void handleLevelInitialize() {
        System.out.println("Receiving world data...");
        // TODO: prepare world buffer for incoming chunk data
    }

    private void handleLevelDataChunk(LevelDataChunkPacket packet) {
        System.out.println("World data: " + packet.getPercentComplete() + "% complete");
        // TODO: append chunk data to world buffer
    }

    private void handleLevelFinalize(LevelFinalizePacket packet) {
        System.out.println("World loaded: " + packet.getXSize() + "x"
                + packet.getYSize() + "x" + packet.getZSize());
        // TODO: decompress and load the world from accumulated chunk data
    }

    private void handleSetBlock(SetBlockServerPacket packet) {
        // TODO: update local world state and trigger re-render
    }

    private void handleSpawnPlayer(SpawnPlayerPacket packet) {
        if (packet.getPlayerId() == SpawnPlayerPacket.SELF_ID) {
            System.out.println("Spawned at: (" + (packet.getX() / 32.0) + ", "
                    + (packet.getY() / 32.0) + ", " + (packet.getZ() / 32.0) + ")");
        } else {
            System.out.println("Player joined: " + packet.getPlayerName()
                    + " (ID " + packet.getPlayerId() + ")");
        }
        // TODO: create player entity in world
    }

    private void handlePlayerTeleport(PlayerTeleportPacket packet) {
        // TODO: update player position (absolute)
    }

    private void handlePositionOrientationUpdate(PositionOrientationUpdatePacket packet) {
        // TODO: update player position (relative + rotation)
    }

    private void handlePositionUpdate(PositionUpdatePacket packet) {
        // TODO: update player position (relative, no rotation change)
    }

    private void handleOrientationUpdate(OrientationUpdatePacket packet) {
        // TODO: update player rotation (no position change)
    }

    private void handleDespawnPlayer(DespawnPlayerPacket packet) {
        System.out.println("Player left: ID " + packet.getPlayerId());
        // TODO: remove player entity from world
    }

    private void handleMessage(MessagePacket packet) {
        System.out.println("[Chat] Player " + packet.getPlayerId() + ": " + packet.getMessage());
        // TODO: display in chat overlay
    }

    private void handleDisconnect(ChannelHandlerContext ctx, DisconnectPacket packet) {
        System.out.println("Disconnected: " + packet.getReason());
        ctx.close();
    }

    private void handleUpdateUserType(UpdateUserTypePacket packet) {
        System.out.println("User type updated: " + (packet.getUserType() == UpdateUserTypePacket.USER_TYPE_OP ? "Op" : "Normal"));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
