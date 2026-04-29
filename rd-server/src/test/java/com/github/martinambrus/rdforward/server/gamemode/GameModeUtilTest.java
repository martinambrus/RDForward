package com.github.martinambrus.rdforward.server.gamemode;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the version-aware gamemode clamp ladder. Adventure (2) and
 * spectator (3) must fall back to survival (0) on clients whose wire
 * format predates them, otherwise pre-1.3.1 / pre-1.8 clients receive
 * an unknown gamemode int and the on-screen behavior is undefined.
 */
class GameModeUtilTest {

    @Test
    void modernClientsKeepFullRange() {
        for (int gm = 0; gm <= 3; gm++) {
            assertEquals(gm, GameModeUtil.clampForLogin(gm, ProtocolVersion.RELEASE_1_8),
                    "1.8+ supports the full 0..3 range");
            assertEquals(gm, GameModeUtil.clampForLogin(gm, ProtocolVersion.RELEASE_1_21_5),
                    "modern client supports the full 0..3 range");
        }
    }

    @Test
    void pre18ClientsClampSpectatorToSurvival() {
        // 1.3.1 added adventure but spectator wasn't introduced until 1.8.
        assertEquals(GameModeUtil.SURVIVAL,
                GameModeUtil.clampForLogin(GameModeUtil.SPECTATOR, ProtocolVersion.RELEASE_1_3_1));
        assertEquals(GameModeUtil.SURVIVAL,
                GameModeUtil.clampForLogin(GameModeUtil.SPECTATOR, ProtocolVersion.RELEASE_1_7_2));
        // Adventure stays.
        assertEquals(GameModeUtil.ADVENTURE,
                GameModeUtil.clampForLogin(GameModeUtil.ADVENTURE, ProtocolVersion.RELEASE_1_3_1));
    }

    @Test
    void pre131ClientsClampAdventureAndSpectatorToSurvival() {
        // Beta 1.8 - Release 1.2.5: gameMode field exists but is 0 or 1 only.
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.BETA_1_8,
                ProtocolVersion.RELEASE_1_0,
                ProtocolVersion.RELEASE_1_2_1,
                ProtocolVersion.RELEASE_1_2_4}) {
            assertEquals(GameModeUtil.SURVIVAL,
                    GameModeUtil.clampForLogin(GameModeUtil.ADVENTURE, v),
                    "pre-1.3.1 must clamp adventure to survival for " + v);
            assertEquals(GameModeUtil.SURVIVAL,
                    GameModeUtil.clampForLogin(GameModeUtil.SPECTATOR, v),
                    "pre-1.3.1 must clamp spectator to survival for " + v);
            assertEquals(GameModeUtil.CREATIVE,
                    GameModeUtil.clampForLogin(GameModeUtil.CREATIVE, v),
                    "pre-1.3.1 must keep creative for " + v);
        }
    }

    @Test
    void preBeta18ClientsCollapseEverythingToSurvival() {
        // No gameMode wire field at all on Indev/Alpha/early Beta. The
        // clamp returns survival so the placeholder int the caller still
        // has to write somewhere is benign.
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.ALPHA_1_2_5,
                ProtocolVersion.BETA_1_3,
                ProtocolVersion.BETA_1_7_3}) {
            for (int gm = 0; gm <= 3; gm++) {
                assertEquals(GameModeUtil.SURVIVAL,
                        GameModeUtil.clampForLogin(gm, v),
                        "pre-Beta-1.8 must collapse gm=" + gm + " to survival for " + v);
            }
        }
    }

    @Test
    void classicAndRubyDungAlwaysSurvival() {
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC}) {
            for (int gm = 0; gm <= 3; gm++) {
                assertEquals(GameModeUtil.SURVIVAL,
                        GameModeUtil.clampForLogin(gm, v),
                        "Classic/RubyDung have no gamemode wire field");
            }
        }
    }

    @Test
    void nullVersionPassesThrough() {
        // Connection-handler call sites already null-guard separately;
        // null here means "version unknown" and we leave the int as-is.
        assertEquals(2, GameModeUtil.clampForLogin(2, null));
    }

    @Test
    void outOfRangeIntFallsBackToSurvival() {
        assertEquals(GameModeUtil.SURVIVAL,
                GameModeUtil.clampForLogin(-1, ProtocolVersion.RELEASE_1_8));
        assertEquals(GameModeUtil.SURVIVAL,
                GameModeUtil.clampForLogin(99, ProtocolVersion.RELEASE_1_8));
    }

    @Test
    void runtimeClampMatchesLoginClamp() {
        // The two helpers are intentionally same-shape — this pins that
        // they don't drift.
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.RUBYDUNG, ProtocolVersion.BETA_1_8,
                ProtocolVersion.RELEASE_1_3_1, ProtocolVersion.RELEASE_1_8}) {
            for (int gm = 0; gm <= 3; gm++) {
                assertEquals(
                        GameModeUtil.clampForLogin(gm, v),
                        GameModeUtil.clampForRuntime(gm, v),
                        "login and runtime clamp must agree for v=" + v + " gm=" + gm);
            }
        }
    }
}
