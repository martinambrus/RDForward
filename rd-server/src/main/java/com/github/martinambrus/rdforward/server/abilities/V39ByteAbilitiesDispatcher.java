package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerAbilitiesPacketV39;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import com.github.martinambrus.rdforward.server.api.ServerProperties;

/**
 * Pushes {@link PlayerAbilitiesPacketV39} (byte flags + byte flySpeed
 * + byte walkSpeed). Used for Release 1.3.1 through 1.5.2.
 *
 * <p>Bukkit semantics: API value range -1..1, defaults 0.1 (fly) /
 * 0.2 (walk). The on-wire byte fields are {@code (byte) round((value/2) * 255)}
 * clamped to 0..255 — matching CraftPlayer's value/2 conversion. The
 * vanilla wire defaults work out to 12 (fly = 0.05 × 255) and 25
 * (walk = 0.1 × 255), the values the join handler sends.
 */
public final class V39ByteAbilitiesDispatcher implements AbilitiesSpeedDispatcher {

    public static final V39ByteAbilitiesDispatcher INSTANCE = new V39ByteAbilitiesDispatcher();

    private V39ByteAbilitiesDispatcher() {}

    @Override
    public void push(ConnectedPlayer player) {
        player.sendPacket(new PlayerAbilitiesPacketV39(
                ServerProperties.getAbilitiesFlags(),
                toByteSpeed(player.getFlySpeed()),
                toByteSpeed(player.getWalkSpeed())));
    }

    private static int toByteSpeed(float apiSpeed) {
        int v = Math.round((apiSpeed / 2.0f) * 255f);
        if (v < 0) v = 0;
        if (v > 255) v = 255;
        return v;
    }
}
