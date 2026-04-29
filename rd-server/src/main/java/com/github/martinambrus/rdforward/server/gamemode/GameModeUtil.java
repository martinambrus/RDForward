package com.github.martinambrus.rdforward.server.gamemode;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;

/**
 * Version-aware gamemode int clamp.
 *
 * <p>The wire-level gamemode field has different ranges per client:
 * <ul>
 *   <li>Pre-Beta 1.8 / Indev / Classic / RubyDung — no gamemode field
 *       on the login packet at all. The client uses its own hardcoded
 *       mode regardless of what the server thinks.</li>
 *   <li>Beta 1.8 (v17) — Release 1.2.5 (v29): introduced creative.
 *       Client only knows {@code 0=survival, 1=creative}; receiving
 *       {@code 2} or {@code 3} is undefined and on some clients
 *       silently falls back to creative.</li>
 *   <li>Release 1.3.1 (v39) — pre-1.8: added adventure ({@code 2}).
 *       Spectator ({@code 3}) is unknown.</li>
 *   <li>Release 1.8 (v47)+: full {@code 0..3} range.</li>
 * </ul>
 *
 * <p>{@link #clampForLogin} maps a server-config gamemode int down to
 * what the connecting client can safely render. Unsupported modes fall
 * back to {@code SURVIVAL (0)} rather than {@code CREATIVE (1)} — a
 * survival fallback gives the player no creative privileges, which is
 * the conservative choice if the operator picked spectator/adventure
 * but a legacy client cannot honour it.
 */
public final class GameModeUtil {

    public static final int SURVIVAL  = 0;
    public static final int CREATIVE  = 1;
    public static final int ADVENTURE = 2;
    public static final int SPECTATOR = 3;

    private GameModeUtil() {}

    /**
     * Clamp a config-level gamemode to what the connecting client's
     * login/join packet can carry. {@code null} version is left as-is
     * (handled elsewhere as the no-version fallback).
     */
    public static int clampForLogin(int gamemode, ProtocolVersion v) {
        return clamp(gamemode, v);
    }

    /**
     * Clamp a runtime {@code /gamemode} change to what a
     * {@link com.github.martinambrus.rdforward.protocol.packet.alpha.ChangeGameStatePacket
     * ChangeGameStatePacket} or
     * {@link com.github.martinambrus.rdforward.protocol.packet.netty.NettyChangeGameStatePacket
     * NettyChangeGameStatePacket} payload can express on this client.
     * Same rules as {@link #clampForLogin} since the wire ranges are
     * identical.
     */
    public static int clampForRuntime(int gamemode, ProtocolVersion v) {
        return clamp(gamemode, v);
    }

    private static int clamp(int gamemode, ProtocolVersion v) {
        if (gamemode < SURVIVAL || gamemode > SPECTATOR) return SURVIVAL;
        if (v == null) return gamemode;
        if (v.isClassicFormat()) return SURVIVAL;
        if (!v.isAtLeast(ProtocolVersion.BETA_1_8)) {
            // Pre-Beta-1.8: no gameMode wire field at all. Caller writes
            // SURVIVAL into placeholder fields if any; the client uses
            // its own built-in mode regardless.
            return SURVIVAL;
        }
        if (!v.isAtLeast(ProtocolVersion.RELEASE_1_3_1) && gamemode > CREATIVE) {
            // Beta 1.8 - Release 1.2.5: only 0/1 supported.
            return SURVIVAL;
        }
        if (!v.isAtLeast(ProtocolVersion.RELEASE_1_8) && gamemode > ADVENTURE) {
            // Release 1.3.1 - 1.7.x: 0/1/2 supported, no spectator.
            return SURVIVAL;
        }
        return gamemode;
    }
}
