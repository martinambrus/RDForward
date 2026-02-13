package com.github.martinambrus.rdforward.server.bedrock;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.classic.DespawnPlayerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.MessagePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.OrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PlayerTeleportPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionOrientationUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.PositionUpdatePacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SetBlockServerPacket;
import com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket;
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket;
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket;
import org.cloudburstmc.protocol.bedrock.packet.TextPacket;
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Translates Classic broadcast packets to Bedrock packets.
 *
 * The server's internal language is Classic packets. When a Bedrock player
 * is the target, this translator converts Classic packets to the equivalent
 * Bedrock packet types.
 *
 * Coordinate conversion:
 * - Classic positions are fixed-point (x32) eye-level Y
 * - Bedrock positions are float blocks, feet Y
 * - Classic yaw 0 = North; Bedrock yaw 0 = South (+180 degrees)
 */
public class ClassicToBedrockTranslator {

    /** Eye-height offset: internal Y is eye-level, Bedrock expects feet. */
    private static final double EYE_HEIGHT = 1.62;

    private final BedrockBlockMapper blockMapper;

    public ClassicToBedrockTranslator(BedrockBlockMapper blockMapper) {
        this.blockMapper = blockMapper;
    }

    /**
     * Translate a Classic packet to one or more Bedrock packets.
     * Some translations (like SpawnPlayer) produce multiple packets
     * (PlayerListPacket + AddPlayerPacket). Returns empty list if dropped.
     */
    public List<BedrockPacket> translateAll(Packet packet) {
        if (packet instanceof SpawnPlayerPacket) {
            return translateSpawnPlayerAll((SpawnPlayerPacket) packet);
        }
        // All other packets produce 0 or 1 result
        BedrockPacket single = translate(packet);
        return single != null ? Collections.singletonList(single) : Collections.emptyList();
    }

    /**
     * Translate a Classic packet to a single Bedrock packet.
     * Returns null if the packet should be dropped.
     */
    public BedrockPacket translate(Packet packet) {
        if (packet instanceof SetBlockServerPacket) {
            return translateSetBlock((SetBlockServerPacket) packet);
        }
        if (packet instanceof SpawnPlayerPacket) {
            return translateSpawnPlayer((SpawnPlayerPacket) packet);
        }
        if (packet instanceof PlayerTeleportPacket) {
            return translatePlayerTeleport((PlayerTeleportPacket) packet);
        }
        if (packet instanceof PositionOrientationUpdatePacket) {
            return translatePositionOrientationUpdate((PositionOrientationUpdatePacket) packet);
        }
        if (packet instanceof PositionUpdatePacket) {
            return translatePositionUpdate((PositionUpdatePacket) packet);
        }
        if (packet instanceof OrientationUpdatePacket) {
            return translateOrientationUpdate((OrientationUpdatePacket) packet);
        }
        if (packet instanceof DespawnPlayerPacket) {
            return translateDespawn((DespawnPlayerPacket) packet);
        }
        if (packet instanceof MessagePacket) {
            return translateMessage((MessagePacket) packet);
        }
        if (packet instanceof com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket) {
            return translateDisconnect(
                    (com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket) packet);
        }
        // Drop: PingPacket (RakNet handles keep-alive),
        // LevelInitialize, LevelDataChunk, LevelFinalize,
        // UpdateUserType, ServerIdentification
        return null;
    }

    private UpdateBlockPacket translateSetBlock(SetBlockServerPacket pkt) {
        UpdateBlockPacket ubp = new UpdateBlockPacket();
        ubp.setBlockPosition(Vector3i.from(pkt.getX(), pkt.getY(), pkt.getZ()));
        ubp.setDefinition(blockMapper.toDefinition(pkt.getBlockType()));
        ubp.setDataLayer(0);
        ubp.getFlags().addAll(EnumSet.of(
                UpdateBlockPacket.Flag.NEIGHBORS,
                UpdateBlockPacket.Flag.NETWORK));
        return ubp;
    }

    private AddPlayerPacket translateSpawnPlayer(SpawnPlayerPacket pkt) {
        if (pkt.getPlayerId() == -1) return null; // self-spawn not applicable

        AddPlayerPacket app = new AddPlayerPacket();
        long entityId = pkt.getPlayerId() + 1;
        app.setUuid(UUID.nameUUIDFromBytes(
                ("RDForward:" + pkt.getPlayerName()).getBytes()));
        app.setUsername(pkt.getPlayerName());
        app.setUniqueEntityId(entityId);
        app.setRuntimeEntityId(entityId);
        app.setPosition(fixedPointToFloat(
                pkt.getX(), pkt.getY(), pkt.getZ(), true));
        app.setRotation(Vector3f.from(
                classicPitchToDegrees(pkt.getPitch()),
                classicYawToBedrockDegrees(pkt.getYaw()),
                0));
        app.setPlatformChatId("");
        app.setDeviceId("");
        app.setGameType(org.cloudburstmc.protocol.bedrock.data.GameType.CREATIVE);

        // Entity metadata required for the player to render
        EntityDataMap metadata = app.getMetadata();
        metadata.setFlag(EntityFlag.HAS_COLLISION, true);
        metadata.setFlag(EntityFlag.CAN_CLIMB, true);
        metadata.setFlag(EntityFlag.CAN_WALK, true);
        metadata.setFlag(EntityFlag.CAN_SWIM, true);
        metadata.setFlag(EntityFlag.BREATHING, true);
        metadata.setFlag(EntityFlag.HAS_GRAVITY, true);
        metadata.putType(EntityDataTypes.SCALE, 1.0f);
        metadata.putType(EntityDataTypes.WIDTH, 0.6f);
        metadata.putType(EntityDataTypes.HEIGHT, 1.8f);
        metadata.putType(EntityDataTypes.AIR_SUPPLY, (short) 400);
        metadata.putType(EntityDataTypes.AIR_SUPPLY_MAX, (short) 400);
        metadata.putType(EntityDataTypes.NAME, pkt.getPlayerName());

        return app;
    }

    private MovePlayerPacket translatePlayerTeleport(PlayerTeleportPacket pkt) {
        if (pkt.getPlayerId() == -1) return null;

        MovePlayerPacket mpp = new MovePlayerPacket();
        mpp.setRuntimeEntityId(pkt.getPlayerId() + 1);
        mpp.setPosition(fixedPointToFloat(
                pkt.getX(), pkt.getY(), pkt.getZ(), true));
        mpp.setRotation(Vector3f.from(
                classicPitchToDegrees(pkt.getPitch()),
                classicYawToBedrockDegrees(pkt.getYaw()),
                0));
        mpp.setMode(MovePlayerPacket.Mode.NORMAL);
        mpp.setOnGround(true);
        return mpp;
    }

    /**
     * Relative position+orientation updates.
     * We convert these to absolute MovePlayerPacket since Bedrock only has absolute moves.
     * The caller provides the current absolute position if available;
     * here we approximate by ignoring (the client will resync).
     * In practice, the server sends PlayerTeleportPacket for large moves.
     */
    private MovePlayerPacket translatePositionOrientationUpdate(PositionOrientationUpdatePacket pkt) {
        // Relative updates can't be cleanly converted without knowing absolute position.
        // Drop them — the full PlayerTeleportPacket handles sync.
        return null;
    }

    private MovePlayerPacket translatePositionUpdate(PositionUpdatePacket pkt) {
        // Same as above — relative updates not directly translatable
        return null;
    }

    private MovePlayerPacket translateOrientationUpdate(OrientationUpdatePacket pkt) {
        // Same as above
        return null;
    }

    private RemoveEntityPacket translateDespawn(DespawnPlayerPacket pkt) {
        RemoveEntityPacket rep = new RemoveEntityPacket();
        rep.setUniqueEntityId(pkt.getPlayerId() + 1);
        return rep;
    }

    private TextPacket translateMessage(MessagePacket pkt) {
        TextPacket tp = new TextPacket();
        tp.setType(TextPacket.Type.SYSTEM);
        tp.setMessage(pkt.getMessage());
        tp.setXuid("");
        tp.setPlatformChatId("");
        tp.setSourceName("");
        return tp;
    }

    private org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket translateDisconnect(
            com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket pkt) {
        org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket dp =
                new org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket();
        dp.setKickMessage(pkt.getReason());
        return dp;
    }

    /**
     * Convert Classic fixed-point position (x32, eye-level Y) to Bedrock float.
     * Bedrock Y is feet position (eye-level - 1.62).
     *
     * @param subtractEyeHeight if true, convert eye-level to feet Y
     */
    private Vector3f fixedPointToFloat(short x, short y, short z, boolean subtractEyeHeight) {
        float fx = x / 32.0f;
        float fy = y / 32.0f;
        float fz = z / 32.0f;
        if (subtractEyeHeight) {
            fy -= (float) EYE_HEIGHT;
        }
        return Vector3f.from(fx, fy, fz);
    }

    /**
     * Convert Classic byte yaw (0-255, 0=North) to Bedrock degrees (0=South).
     */
    private float classicYawToBedrockDegrees(int classicYaw) {
        // Classic 0 = North, Bedrock 0 = South → add 180°
        float degrees = (classicYaw & 0xFF) * 360.0f / 256.0f;
        return (degrees + 180.0f) % 360.0f;
    }

    /**
     * Convert Classic byte pitch (0-255) to Bedrock degrees.
     */
    private float classicPitchToDegrees(int classicPitch) {
        return (classicPitch & 0xFF) * 360.0f / 256.0f;
    }

    /**
     * Translate SpawnPlayer to PlayerListPacket(ADD) + AddPlayerPacket.
     * Bedrock requires the player to be in the player list (with skin)
     * before AddPlayerPacket will render the entity.
     */
    private List<BedrockPacket> translateSpawnPlayerAll(SpawnPlayerPacket pkt) {
        if (pkt.getPlayerId() == -1) return Collections.emptyList();

        long entityId = pkt.getPlayerId() + 1;
        UUID playerUuid = UUID.nameUUIDFromBytes(
                ("RDForward:" + pkt.getPlayerName()).getBytes());

        // 1. PlayerListPacket (ADD) with skin
        PlayerListPacket playerList = new PlayerListPacket();
        playerList.setAction(PlayerListPacket.Action.ADD);
        PlayerListPacket.Entry entry = new PlayerListPacket.Entry(playerUuid);
        entry.setEntityId(entityId);
        entry.setName(pkt.getPlayerName());
        entry.setSkin(createDefaultSkin());
        entry.setXuid("");
        entry.setPlatformChatId("");
        entry.setTrustedSkin(true);
        playerList.getEntries().add(entry);

        // 2. AddPlayerPacket
        AddPlayerPacket app = translateSpawnPlayer(pkt);

        List<BedrockPacket> result = new ArrayList<>(3);
        result.add(playerList);
        if (app != null) {
            result.add(app);

            // 3. MovePlayerPacket — some Bedrock clients need this after AddPlayer
            //    to make the entity actually render
            MovePlayerPacket mpp = new MovePlayerPacket();
            mpp.setRuntimeEntityId(entityId);
            mpp.setPosition(app.getPosition());
            mpp.setRotation(app.getRotation());
            mpp.setMode(MovePlayerPacket.Mode.TELEPORT);
            mpp.setOnGround(true);
            result.add(mpp);
        }
        return result;
    }

    /**
     * Create a default skin for PlayerListPacket entries.
     */
    static SerializedSkin createDefaultSkin() {
        byte[] skinBytes = new byte[64 * 64 * 4];
        for (int i = 0; i < skinBytes.length; i += 4) {
            skinBytes[i] = (byte) 0xC6;
            skinBytes[i + 1] = (byte) 0x8E;
            skinBytes[i + 2] = (byte) 0x5A;
            skinBytes[i + 3] = (byte) 0xFF;
        }
        return SerializedSkin.of(
                "Standard_Custom", "", ImageData.of(64, 64, skinBytes),
                ImageData.of(0, 0, new byte[0]), "geometry.humanoid.custom",
                "", false);
    }
}
