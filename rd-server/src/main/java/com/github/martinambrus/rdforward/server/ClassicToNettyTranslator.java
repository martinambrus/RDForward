package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.BlockStateMapper;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import com.github.martinambrus.rdforward.protocol.packet.netty.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * Outbound handler that translates Classic broadcast packets to their
 * 1.7.2 Netty equivalents for MC 1.7.2+ clients.
 *
 * Same pattern as ClassicToAlphaTranslator: the server's internal
 * broadcast language is Classic packets. This translator intercepts
 * them and converts to the corresponding Netty-protocol packets.
 *
 * Packets that are already Netty packets (from the netty package)
 * or Alpha packets (sent directly by NettyConnectionHandler during
 * login) pass through unchanged.
 */
public class ClassicToNettyTranslator extends ChannelOutboundHandlerAdapter {

    /** Eye-height offset in fixed-point units. Internal Y is eye-level; spawn expects feet. */
    private static final int EYE_HEIGHT_FIXED = AlphaConnectionHandler.PLAYER_EYE_HEIGHT_FIXED;

    private volatile ProtocolVersion clientVersion = ProtocolVersion.RELEASE_1_7_2;

    public void setClientVersion(ProtocolVersion version) {
        this.clientVersion = version;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof Packet)) {
            super.write(ctx, msg, promise);
            return;
        }

        Packet packet = (Packet) msg;
        Packet translated = translate(packet);

        if (translated != null) {
            super.write(ctx, translated, promise);
        } else {
            // Packet was dropped
            promise.setSuccess();
        }
    }

    private Packet translate(Packet packet) {
        boolean isV769 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_4);
        boolean isV768 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_21_2);
        boolean isV766 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_5);
        boolean isV765 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_3);
        boolean isV764 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20_2);
        boolean isV763 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_20);
        boolean isV762 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_4);
        boolean isV761 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_3);
        boolean isV760 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19_1);
        boolean isV759 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_19);
        boolean isV755 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17);
        boolean isV751 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16_2);
        boolean isV735 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_16);
        boolean isV573 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_15);
        boolean isV477 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_14);
        boolean isV393 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_13);
        boolean isV109 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_9);
        boolean isV47 = clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_8);

        // PlayerListItemPacket is in the .alpha package but needs translation
        // to NettyPlayerListItemPacket — check before isNettyPacket() which
        // passes .alpha packets through unchanged.
        if (packet instanceof PlayerListItemPacket) {
            PlayerListItemPacket pli = (PlayerListItemPacket) packet;
            if (isV761) {
                // V761: PlayerInfoUpdate/PlayerInfoRemove replace PlayerListItem
                String uuid = generateOfflineUuid(pli.getUsername());
                return pli.isOnline()
                        ? NettyPlayerInfoUpdatePacketV761.addPlayer(uuid, pli.getUsername(), 1, pli.getPing())
                        : NettyPlayerInfoRemovePacketV761.removePlayer(uuid);
            }
            if (isV759) {
                // V759 and V760 share the same PlayerListItem format
                String uuid = generateOfflineUuid(pli.getUsername());
                return pli.isOnline()
                        ? NettyPlayerListItemPacketV759.addPlayer(uuid, pli.getUsername(), 1, pli.getPing())
                        : NettyPlayerListItemPacketV759.removePlayer(uuid);
            }
            if (isV47) {
                String uuid = generateOfflineUuid(pli.getUsername());
                return pli.isOnline()
                        ? NettyPlayerListItemPacketV47.addPlayer(uuid, pli.getUsername(), 1, pli.getPing())
                        : NettyPlayerListItemPacketV47.removePlayer(uuid);
            }
            return new NettyPlayerListItemPacket(pli.getUsername(), pli.isOnline(), pli.getPing());
        }

        // Already a Netty-protocol packet — pass through
        if (isNettyPacket(packet)) {
            return packet;
        }

        if (packet instanceof PingPacket) {
            // Drop tick-loop pings — Netty protocol has its own KeepAlive heartbeat
            return null;
        }

        if (packet instanceof SetBlockServerPacket) {
            SetBlockServerPacket sb = (SetBlockServerPacket) packet;
            if (isV759) {
                // V759+ share the same block state IDs (through v764)
                return new NettyBlockChangePacketV477(sb.getX(), sb.getY(), sb.getZ(),
                        BlockStateMapper.toV759BlockState(sb.getBlockType()));
            }
            if (isV755) {
                return new NettyBlockChangePacketV477(sb.getX(), sb.getY(), sb.getZ(),
                        BlockStateMapper.toV755BlockState(sb.getBlockType()));
            }
            if (isV735) {
                return new NettyBlockChangePacketV477(sb.getX(), sb.getY(), sb.getZ(),
                        BlockStateMapper.toV735BlockState(sb.getBlockType()));
            }
            if (isV477) {
                return new NettyBlockChangePacketV477(sb.getX(), sb.getY(), sb.getZ(),
                        BlockStateMapper.toV393BlockState(sb.getBlockType()));
            }
            if (isV393) {
                return new NettyBlockChangePacketV393(sb.getX(), sb.getY(), sb.getZ(),
                        BlockStateMapper.toV393BlockState(sb.getBlockType()));
            }
            if (isV47) {
                return new NettyBlockChangePacketV47(sb.getX(), sb.getY(), sb.getZ(),
                        sb.getBlockType(), 0);
            }
            return new NettyBlockChangePacket(sb.getX(), sb.getY(), sb.getZ(),
                    sb.getBlockType(), 0);
        }

        if (packet instanceof com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket) {
            com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket sp =
                    (com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket) packet;

            if (sp.getPlayerId() == -1) {
                return null;
            }

            int entityId = sp.getPlayerId() + 1;
            int feetY = (int) sp.getY() - EYE_HEIGHT_FIXED;
            // Classic yaw 0 = North; Alpha/Netty yaw 0 = South. Add 128 (180°) to convert.
            int alphaYaw = (sp.getYaw() + 128) & 0xFF;
            // Generate offline UUID from username
            String uuid = generateOfflineUuid(sp.getPlayerName());
            if (isV769) {
                // 1.21.4: creaking_transient removed (player: 148 -> 147)
                return new NettySpawnEntityPacketV769(
                        entityId, uuid,
                        sp.getX() / 32.0, feetY / 32.0, sp.getZ() / 32.0,
                        alphaYaw, sp.getPitch());
            }
            if (isV768) {
                // 1.21.2: entity type IDs shifted (player: 128 -> 148)
                return new NettySpawnEntityPacketV768(
                        entityId, uuid,
                        sp.getX() / 32.0, feetY / 32.0, sp.getZ() / 32.0,
                        alphaYaw, sp.getPitch());
            }
            if (isV766) {
                // 1.20.5: entity type IDs shifted (player: 122 -> 128)
                return new NettySpawnEntityPacketV766(
                        entityId, uuid,
                        sp.getX() / 32.0, feetY / 32.0, sp.getZ() / 32.0,
                        alphaYaw, sp.getPitch());
            }
            if (isV764) {
                // 1.20.2: SpawnPlayer removed, use generic SpawnEntity
                return new NettySpawnEntityPacketV764(
                        entityId, uuid,
                        sp.getX() / 32.0, feetY / 32.0, sp.getZ() / 32.0,
                        alphaYaw, sp.getPitch());
            }
            if (isV573) {
                // 1.15: double coordinates, entity metadata fully removed
                return new NettySpawnPlayerPacketV573(
                        entityId, uuid,
                        sp.getX() / 32.0, feetY / 32.0, sp.getZ() / 32.0,
                        alphaYaw, sp.getPitch());
            }
            if (isV477) {
                // 1.14: double coordinates, empty metadata (0xFF terminator)
                return new NettySpawnPlayerPacketV477(
                        entityId, uuid,
                        sp.getX() / 32.0, feetY / 32.0, sp.getZ() / 32.0,
                        alphaYaw, sp.getPitch());
            }
            if (isV109) {
                // 1.9: double coordinates (convert from fixed-point /32)
                return new NettySpawnPlayerPacketV109(
                        entityId, uuid,
                        sp.getX() / 32.0, feetY / 32.0, sp.getZ() / 32.0,
                        alphaYaw, sp.getPitch());
            }
            if (isV47) {
                return new NettySpawnPlayerPacketV47(
                        entityId, uuid,
                        (int) sp.getX(), feetY, (int) sp.getZ(),
                        alphaYaw, sp.getPitch(), (short) 0);
            }
            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_7_6)) {
                return new NettySpawnPlayerPacketV5(
                        entityId, uuid, sp.getPlayerName(),
                        (int) sp.getX(), feetY, (int) sp.getZ(),
                        alphaYaw, sp.getPitch(), (short) 0);
            }
            return new NettySpawnPlayerPacket(
                    entityId, uuid, sp.getPlayerName(),
                    (int) sp.getX(), feetY, (int) sp.getZ(),
                    alphaYaw, sp.getPitch(), (short) 0);
        }

        if (packet instanceof PlayerTeleportPacket) {
            PlayerTeleportPacket pt = (PlayerTeleportPacket) packet;

            if (pt.getPlayerId() == -1) {
                return null;
            }

            int entityId = pt.getPlayerId() + 1;
            int feetY = (int) pt.getY() - EYE_HEIGHT_FIXED;
            int alphaYaw = (pt.getYaw() + 128) & 0xFF;
            if (isV768) {
                // 1.21.2: EntityTeleport replaced by EntityPositionSync (float degrees, delta velocities)
                return new EntityPositionSyncPacketV768(entityId,
                        pt.getX() / 32.0, feetY / 32.0, pt.getZ() / 32.0,
                        alphaYaw, pt.getPitch());
            }
            if (isV109) {
                // 1.9: double coordinates
                return new EntityTeleportPacketV109(entityId,
                        pt.getX() / 32.0, feetY / 32.0, pt.getZ() / 32.0,
                        alphaYaw, pt.getPitch());
            }
            if (isV47) {
                return new EntityTeleportPacketV47(entityId,
                        (int) pt.getX(), feetY, (int) pt.getZ(),
                        alphaYaw, pt.getPitch());
            }
            return new EntityTeleportPacket(entityId,
                    (int) pt.getX(), feetY, (int) pt.getZ(),
                    alphaYaw, pt.getPitch());
        }

        if (packet instanceof PositionOrientationUpdatePacket) {
            PositionOrientationUpdatePacket pou = (PositionOrientationUpdatePacket) packet;
            int entityId = pou.getPlayerId() + 1;
            if (isV109) {
                // 1.9: short deltas, scale 1/4096 (was 1/32). Multiply by 128.
                return new EntityLookAndMovePacketV109(entityId,
                        (short) (pou.getChangeX() * 128),
                        (short) (pou.getChangeY() * 128),
                        (short) (pou.getChangeZ() * 128),
                        pou.getYaw(), pou.getPitch());
            }
            if (isV47) {
                return new EntityLookAndMovePacketV47(entityId,
                        pou.getChangeX(), pou.getChangeY(), pou.getChangeZ(),
                        pou.getYaw(), pou.getPitch());
            }
            return new EntityLookAndMovePacket(entityId,
                    pou.getChangeX(), pou.getChangeY(), pou.getChangeZ(),
                    pou.getYaw(), pou.getPitch());
        }

        if (packet instanceof PositionUpdatePacket) {
            PositionUpdatePacket pu = (PositionUpdatePacket) packet;
            int entityId = pu.getPlayerId() + 1;
            if (isV109) {
                // 1.9: short deltas, scale 1/4096 (was 1/32). Multiply by 128.
                return new EntityRelativeMovePacketV109(entityId,
                        (short) (pu.getChangeX() * 128),
                        (short) (pu.getChangeY() * 128),
                        (short) (pu.getChangeZ() * 128));
            }
            if (isV47) {
                return new EntityRelativeMovePacketV47(entityId,
                        pu.getChangeX(), pu.getChangeY(), pu.getChangeZ());
            }
            return new EntityRelativeMovePacket(entityId,
                    pu.getChangeX(), pu.getChangeY(), pu.getChangeZ());
        }

        if (packet instanceof OrientationUpdatePacket) {
            OrientationUpdatePacket ou = (OrientationUpdatePacket) packet;
            int entityId = ou.getPlayerId() + 1;
            if (isV47) {
                return new EntityLookPacketV47(entityId, ou.getYaw(), ou.getPitch());
            }
            return new EntityLookPacket(entityId, ou.getYaw(), ou.getPitch());
        }

        if (packet instanceof DespawnPlayerPacket) {
            DespawnPlayerPacket dp = (DespawnPlayerPacket) packet;
            int entityId = dp.getPlayerId() + 1;
            if (clientVersion.isAtLeast(ProtocolVersion.RELEASE_1_17_1)) {
                return new NettyDestroyEntitiesPacketV47(entityId);
            }
            if (isV755) {
                return new RemoveEntityPacketV755(entityId);
            }
            if (isV47) {
                return new NettyDestroyEntitiesPacketV47(entityId);
            }
            return new NettyDestroyEntitiesPacket(entityId);
        }

        if (packet instanceof MessagePacket) {
            MessagePacket mp = (MessagePacket) packet;
            String plainText = mp.getMessage();
            if (isV765) {
                // V765+: NBT text component — pass plain text, NOT JSON
                return new SystemChatPacketV765(plainText, false);
            }
            // Pre-V765: JSON text component
            String message = "{\"text\":\"" + plainText.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            if (isV761) {
                return new SystemChatPacketV760(message, false);
            }
            if (isV760) {
                return new SystemChatPacketV760(message, false);
            }
            if (isV759) {
                return new SystemChatPacketV759(message, 1);
            }
            if (isV735) {
                return new NettyChatS2CPacketV735(message, (byte) 0, 0L, 0L);
            }
            if (isV47) {
                return new NettyChatS2CPacketV47(message, (byte) 0);
            }
            return new NettyChatS2CPacket(message);
        }

        if (packet instanceof com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket) {
            com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket dp =
                    (com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket) packet;
            String reason = dp.getReason();
            if (isV765) {
                return new NettyDisconnectPacketV765(reason);
            }
            reason = "{\"text\":\"" + reason.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            return new NettyDisconnectPacket(reason);
        }

        // Drop Classic-only packets
        return null;
    }

    private boolean isNettyPacket(Packet packet) {
        String packageName = packet.getClass().getPackage().getName();
        return packageName.endsWith(".netty") || packageName.endsWith(".alpha");
    }

    /**
     * Generate an offline-mode UUID v3 from a username (same as MC offline mode).
     * Format: "OfflinePlayer:" + username -> MD5 -> UUID v3 with hyphens.
     */
    static String generateOfflineUuid(String username) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(("OfflinePlayer:" + username).getBytes(
                    java.nio.charset.Charset.forName("UTF-8")));
            hash[6] = (byte) (hash[6] & 0x0f | 0x30); // version 3
            hash[8] = (byte) (hash[8] & 0x3f | 0x80); // variant 2
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; i++) {
                sb.append(String.format("%02x", hash[i]));
                if (i == 3 || i == 5 || i == 7 || i == 9) sb.append('-');
            }
            return sb.toString();
        } catch (Exception e) {
            // Fallback: deterministic fake UUID
            return "00000000-0000-3000-8000-000000000000";
        }
    }
}
