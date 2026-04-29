package com.github.martinambrus.rdforward.server.gamemode;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.alpha.ChangeGameStatePacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChangeGameStatePacket;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;

/**
 * Sends a runtime gamemode change to one connected client, picking the
 * right wire-format variant for the session's protocol version.
 *
 * <p>Three branches:
 * <ul>
 *   <li>Netty 1.7.2+ — {@link NettyChangeGameStatePacket}, reason = 3,
 *       value as float.</li>
 *   <li>Pre-Netty Beta 1.8 / Release 1.0–1.6.4 —
 *       {@link ChangeGameStatePacket}, reason = 3, value as byte. The
 *       packet was introduced in Beta 1.5 but creative mode itself
 *       isn't supported on Beta clients before 1.8, so the gate is
 *       Beta 1.8 here.</li>
 *   <li>Everyone else (Classic / RubyDung / Indev / Alpha / pre-Beta-1.8
 *       Beta / Bedrock / MCPE) — no-op, the client doesn't model
 *       runtime gamemode switching.</li>
 * </ul>
 *
 * <p>The requested mode is run through
 * {@link GameModeUtil#clampForRuntime} first so a 1.5.x client never
 * receives spectator (3) and a 1.2.x client never receives adventure (2).
 *
 * @return the gamemode that was actually delivered to the client
 *         (post-clamp), or {@code -1} if no packet was sent at all (pre-
 *         Beta-1.8 / Classic / Bedrock / MCPE / null version).
 */
public final class GameModeDispatcher {

    private GameModeDispatcher() {}

    public static int push(ConnectedPlayer player, int requestedMode) {
        if (player == null) return -1;
        ProtocolVersion v = player.getProtocolVersion();
        if (v == null) return -1;
        if (player.getBedrockSession() != null || player.getMcpeSession() != null) {
            // Bedrock/MCPE have their own gamemode-change codec path
            // (UpdatePlayerGameTypePacket) — out of scope here. Caller
            // can read the return and route appropriately if needed.
            return -1;
        }
        if (v.isClassicFormat() || !v.isAtLeast(ProtocolVersion.BETA_1_8)) {
            // No runtime-gamemode-change packet on the wire.
            return -1;
        }
        int clamped = GameModeUtil.clampForRuntime(requestedMode, v);
        if (v.isAtLeast(ProtocolVersion.RELEASE_1_7_2)) {
            player.sendPacket(new NettyChangeGameStatePacket(
                    NettyChangeGameStatePacket.CHANGE_GAME_MODE, (float) clamped));
        } else {
            player.sendPacket(new ChangeGameStatePacket(
                    ChangeGameStatePacket.CHANGE_GAME_MODE, clamped));
        }
        return clamped;
    }
}
