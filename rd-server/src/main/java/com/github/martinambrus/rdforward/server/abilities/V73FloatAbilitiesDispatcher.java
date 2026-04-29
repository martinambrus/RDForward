package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityPropertiesPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityPropertiesPacketV74;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerAbilitiesPacketV73;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacketV755;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacketV766;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.api.ServerProperties;

/**
 * Pushes a {@link PlayerAbilitiesPacketV73} (byte flags + float flySpeed
 * + float walkSpeed) and — for clients that have the attribute system
 * (Release 1.6.1+) — also pushes a movement-speed attribute update.
 *
 * <p>Bukkit semantics: the API value range is -1..1 with API defaults
 * 0.1 (fly) / 0.2 (walk). The on-wire packet field and the
 * {@code generic.movement_speed} attribute are both half the API value,
 * matching {@code CraftPlayer.setFlySpeed} / {@code setWalkSpeed}.
 *
 * <p>The abilities packet's {@code walkSpeed} field drives FOV / sprint
 * multipliers on modern clients, but actual ground walking speed is
 * driven by the {@code generic.movement_speed} attribute — without the
 * attribute push, {@code /walkspeed} changes the FOV (so it looks like
 * the player is moving faster) but the ground velocity stays at the
 * default. The attribute push fixes that so ground speed actually
 * changes.
 */
public final class V73FloatAbilitiesDispatcher implements AbilitiesSpeedDispatcher {

    public static final V73FloatAbilitiesDispatcher INSTANCE = new V73FloatAbilitiesDispatcher();

    private static final String LEGACY_KEY = "generic.movementSpeed";
    private static final String NAMESPACED_KEY = "minecraft:generic.movement_speed";

    private V73FloatAbilitiesDispatcher() {}

    @Override
    public void push(ConnectedPlayer player) {
        ProtocolVersion v = player.getProtocolVersion();
        if (v == null) return;

        float wireFly = player.getFlySpeed() / 2.0f;
        float wireWalk = player.getWalkSpeed() / 2.0f;
        player.sendPacket(new PlayerAbilitiesPacketV73(
                ServerProperties.getAbilitiesFlags(), wireFly, wireWalk));

        // Also push the movement-speed attribute on clients that have it
        // (Release 1.6.1+). Skipping this leaves the FOV-only side effect
        // and the player keeps walking at default ground speed.
        int entityId = player.getPlayerId() + 1;
        double attributeValue = wireWalk;
        boolean useNamespacedKey = v.isAtLeast(ProtocolVersion.RELEASE_1_16);
        String key = useNamespacedKey ? NAMESPACED_KEY : LEGACY_KEY;

        if (v.isAtLeast(ProtocolVersion.RELEASE_1_20_5)) {
            player.sendPacket(new NettyEntityPropertiesPacketV766(
                    entityId, NettyEntityPropertiesPacketV766.MOVEMENT_SPEED, attributeValue));
        } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_17)) {
            player.sendPacket(new NettyEntityPropertiesPacketV755(entityId, key, attributeValue));
        } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_8)) {
            player.sendPacket(new NettyEntityPropertiesPacketV47(entityId, key, attributeValue));
        } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            player.sendPacket(new NettyEntityPropertiesPacket(entityId, key, attributeValue));
        } else if (v.isAtLeast(ProtocolVersion.RELEASE_1_6_2)) {
            player.sendPacket(new EntityPropertiesPacketV74(entityId, key, attributeValue));
        } else {
            // 1.6.1 only: pre-modifier-list attribute packet shape.
            player.sendPacket(new EntityPropertiesPacket(entityId, key, attributeValue));
        }
    }
}
