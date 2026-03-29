package com.github.martinambrus.rdforward.server.lce;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import com.github.martinambrus.rdforward.protocol.packet.lce.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbound handler that translates Classic broadcast packets to LCE format.
 *
 * LCE is based on Java Edition 1.6.4, so most translations are similar to
 * ClassicToAlphaTranslator for v39+ (Release 1.3.1+) clients.
 *
 * Only one LCE version is supported (TU19), so no version branching is needed.
 *
 * Important: LCE's MoveEntity packets use RELATIVE (delta) body rotation,
 * while head rotation uses a separate RotateHeadPacket (0x23) with ABSOLUTE yaw.
 */
public class ClassicToLCETranslator extends ChannelOutboundHandlerAdapter {

    /** Packet classes already logged as dropped — log each only once to avoid flooding. */
    private static final Set<String> LOGGED_DROPPED = ConcurrentHashMap.newKeySet();

    /** Same as AlphaConnectionHandler.PLAYER_EYE_HEIGHT_FIXED (package-private). */
    private static final int EYE_HEIGHT_FIXED = (int) Math.ceil((double) 1.62f * 32);

    private volatile ProtocolVersion clientVersion;

    /** Tracks last-sent absolute yaw/pitch per entity ID (for computing body rotation deltas). */
    private final Map<Integer, int[]> lastEntityRot = new HashMap<>();

    public void setClientVersion(ProtocolVersion clientVersion) {
        this.clientVersion = clientVersion;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            super.write(ctx, msg, promise);
            return;
        }

        Packet packet = (Packet) msg;
        Packet[] translated = translate(packet);

        if (translated == null) {
            promise.setSuccess();
        } else if (translated.length == 1) {
            super.write(ctx, translated[0], promise);
        } else {
            // Multiple packets: write all, attach promise to the last one
            for (int i = 0; i < translated.length - 1; i++) {
                super.write(ctx, translated[i], ctx.voidPromise());
            }
            super.write(ctx, translated[translated.length - 1], promise);
        }
    }

    private Packet[] translate(Packet packet) {
        // Classic broadcast packets that need translation
        if (packet instanceof com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket) {
            return single(translateSpawnPlayer((com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket) packet));
        }
        if (packet instanceof PlayerTeleportPacket) {
            return translatePlayerTeleport((PlayerTeleportPacket) packet);
        }
        if (packet instanceof PositionOrientationUpdatePacket) {
            return translatePosOriUpdate((PositionOrientationUpdatePacket) packet);
        }
        if (packet instanceof PositionUpdatePacket) {
            return single(translatePosUpdate((PositionUpdatePacket) packet));
        }
        if (packet instanceof OrientationUpdatePacket) {
            return translateOriUpdate((OrientationUpdatePacket) packet);
        }
        if (packet instanceof DespawnPlayerPacket) {
            return single(translateDespawn((DespawnPlayerPacket) packet));
        }
        if (packet instanceof SetBlockServerPacket) {
            return single(translateSetBlock((SetBlockServerPacket) packet));
        }
        if (packet instanceof MessagePacket) {
            return single(translateMessage((MessagePacket) packet));
        }
        if (packet instanceof PingPacket) {
            return null; // Drop Classic pings; LCE uses KeepAlivePacketV17
        }
        if (packet instanceof PlayerListItemPacket) {
            return null; // Drop player list items for now
        }

        // Classic packets with no LCE equivalent — drop
        if (packet instanceof LevelInitializePacket || packet instanceof LevelDataChunkPacket
                || packet instanceof LevelFinalizePacket || packet instanceof ServerIdentificationPacket
                || packet instanceof UpdateUserTypePacket) {
            return null;
        }

        // Allow LCE-native and Alpha-format packets through unchanged.
        if (isLCECompatiblePacket(packet)) {
            return single(packet);
        }

        // Unknown packet type — drop it to prevent wrong-format data reaching LCE client
        String className = packet.getClass().getSimpleName();
        if (LOGGED_DROPPED.add(className)) {
            System.out.println("[LCE-TX] dropped incompatible: " + className
                    + " (ID 0x" + Integer.toHexString(packet.getPacketId())
                    + ") — further drops of this type suppressed");
        }
        return null;
    }

    private static Packet[] single(Packet p) {
        return new Packet[]{p};
    }

    private Packet translateSpawnPlayer(com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket classic) {
        int entityId = (classic.getPlayerId() & 0xFF) + 1;
        int feetY = classic.getY() - EYE_HEIGHT_FIXED;
        // Classic yaw 0=North; Alpha/LCE yaw 0=South: +128 byte rotation
        int alphaYaw = (classic.getYaw() + 128) & 0xFF;
        int pitch = classic.getPitch() & 0xFF;
        updateRot(entityId, alphaYaw, pitch);
        return new LCEAddPlayerPacket(entityId, classic.getPlayerName(),
                classic.getX(), feetY, classic.getZ(),
                (byte) alphaYaw, (byte) pitch, (byte) alphaYaw,
                (short) 0, 0L, 0L, (byte) 0, 0, 0, 0);
    }

    private Packet[] translatePlayerTeleport(PlayerTeleportPacket classic) {
        int entityId = (classic.getPlayerId() & 0xFF) + 1;
        int feetY = classic.getY() - EYE_HEIGHT_FIXED;
        int alphaYaw = (classic.getYaw() + 128) & 0xFF;
        int pitch = classic.getPitch() & 0xFF;
        updateRot(entityId, alphaYaw, pitch);
        return new Packet[]{
            new LCEEntityTeleportPacket(entityId,
                    classic.getX(), feetY, classic.getZ(),
                    alphaYaw, pitch),
            new LCERotateHeadPacket(entityId, alphaYaw)
        };
    }

    private Packet[] translatePosOriUpdate(PositionOrientationUpdatePacket classic) {
        int entityId = (classic.getPlayerId() & 0xFF) + 1;
        int alphaYaw = (classic.getYaw() + 128) & 0xFF;
        int pitch = classic.getPitch() & 0xFF;
        // LCE MoveEntity uses RELATIVE body rotation — compute deltas
        int[] last = getOrCreateRot(entityId);
        int deltaYaw = (byte) (alphaYaw - last[0]);
        int deltaPitch = (byte) (pitch - last[1]);
        last[0] = alphaYaw;
        last[1] = pitch;
        return new Packet[]{
            new LCEEntityLookAndMovePacket(entityId,
                    classic.getChangeX(), classic.getChangeY(), classic.getChangeZ(),
                    deltaYaw, deltaPitch),
            new LCERotateHeadPacket(entityId, alphaYaw)
        };
    }

    private Packet translatePosUpdate(PositionUpdatePacket classic) {
        int entityId = (classic.getPlayerId() & 0xFF) + 1;
        return new LCEEntityRelativeMovePacket(entityId,
                classic.getChangeX(), classic.getChangeY(), classic.getChangeZ());
    }

    private Packet[] translateOriUpdate(OrientationUpdatePacket classic) {
        int entityId = (classic.getPlayerId() & 0xFF) + 1;
        int alphaYaw = (classic.getYaw() + 128) & 0xFF;
        int pitch = classic.getPitch() & 0xFF;
        // LCE MoveEntity uses RELATIVE body rotation — compute deltas
        int[] last = getOrCreateRot(entityId);
        int deltaYaw = (byte) (alphaYaw - last[0]);
        int deltaPitch = (byte) (pitch - last[1]);
        last[0] = alphaYaw;
        last[1] = pitch;
        return new Packet[]{
            new LCEEntityLookPacket(entityId, deltaYaw, deltaPitch),
            new LCERotateHeadPacket(entityId, alphaYaw)
        };
    }

    /** Get or create the rotation tracker for an entity, reusing existing arrays. */
    private int[] getOrCreateRot(int entityId) {
        int[] rot = lastEntityRot.get(entityId);
        if (rot == null) {
            rot = new int[2];
            lastEntityRot.put(entityId, rot);
        }
        return rot;
    }

    /** Update the rotation tracker in-place (or create if first time). */
    private void updateRot(int entityId, int yaw, int pitch) {
        int[] rot = getOrCreateRot(entityId);
        rot[0] = yaw;
        rot[1] = pitch;
    }

    private Packet translateDespawn(DespawnPlayerPacket classic) {
        int entityId = (classic.getPlayerId() & 0xFF) + 1;
        lastEntityRot.remove(entityId);
        return new DestroyEntityPacketV39(new int[]{entityId});
    }

    private Packet translateSetBlock(SetBlockServerPacket classic) {
        return new BlockChangePacketV39(
                classic.getX(), (byte) classic.getY(), classic.getZ(),
                (short) (classic.getBlockType() & 0xFF), (byte) 0);
    }

    private Packet translateMessage(MessagePacket classic) {
        // LCE uses structured chat with messageType + string args
        String text = classic.getMessage().trim();
        return new LCEChatPacket((short) 0, text);
    }

    /**
     * LCE-compatible packets (from .lce or .alpha packages) pass through unchanged.
     */
    private boolean isLCECompatiblePacket(Packet packet) {
        String packageName = packet.getClass().getPackage().getName();
        return packageName.endsWith(".lce") || packageName.endsWith(".alpha");
    }
}
