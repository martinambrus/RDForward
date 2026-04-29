package com.github.martinambrus.rdforward.server.gamemode;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.ChangeGameStatePacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyChangeGameStatePacket;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@link GameModeDispatcher} per-version packet selection and
 * the runtime clamp. Netty 1.7.2+ uses {@link NettyChangeGameStatePacket}
 * with float value; pre-Netty Beta 1.8 - 1.6.4 use
 * {@link ChangeGameStatePacket} with byte value; everything older is a
 * no-op.
 */
class GameModeDispatcherTest {

    private static ConnectedPlayer newPlayer(ProtocolVersion v, EmbeddedChannel ch) {
        return new ConnectedPlayer((byte) 1, "tester",
                "00000000-0000-0000-0000-000000000001", ch, v);
    }

    private static List<Packet> drain(EmbeddedChannel ch) {
        List<Packet> out = new ArrayList<>();
        Object o;
        while ((o = ch.readOutbound()) != null) {
            if (o instanceof Packet p) out.add(p);
        }
        return out;
    }

    @Test
    void modernNettyClientGetsNettyChangeGameStatePacketWithFloatValue() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5, ch);
        int delivered = GameModeDispatcher.push(p, GameModeUtil.SPECTATOR);
        assertEquals(GameModeUtil.SPECTATOR, delivered);

        List<Packet> sent = drain(ch);
        NettyChangeGameStatePacket pkt = (NettyChangeGameStatePacket) sent.stream()
                .filter(x -> x instanceof NettyChangeGameStatePacket)
                .findFirst().orElseThrow(() ->
                        new AssertionError("expected NettyChangeGameStatePacket; got " + sent));
        assertEquals(NettyChangeGameStatePacket.CHANGE_GAME_MODE, pkt.getReason());
        assertEquals(3.0f, pkt.getValue(), 1e-6f);
    }

    @Test
    void v17ClientGetsNettyVariantToo() {
        // 1.7.2 is the first Netty version; same packet class, just
        // remapped via NettyPacketRegistry.
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_7_2, ch);
        int delivered = GameModeDispatcher.push(p, GameModeUtil.CREATIVE);
        assertEquals(GameModeUtil.CREATIVE, delivered);
        List<Packet> sent = drain(ch);
        assertTrue(sent.stream().anyMatch(x -> x instanceof NettyChangeGameStatePacket));
    }

    @Test
    void preNettyRelease16xUsesAlphaChangeGameStatePacket() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_6_4, ch);
        int delivered = GameModeDispatcher.push(p, GameModeUtil.CREATIVE);
        assertEquals(GameModeUtil.CREATIVE, delivered);
        List<Packet> sent = drain(ch);
        ChangeGameStatePacket pkt = (ChangeGameStatePacket) sent.stream()
                .filter(x -> x instanceof ChangeGameStatePacket)
                .findFirst().orElseThrow(() ->
                        new AssertionError("expected pre-Netty ChangeGameStatePacket; got " + sent));
        assertEquals(ChangeGameStatePacket.CHANGE_GAME_MODE, pkt.getReason());
        assertEquals((byte) 1, pkt.getValue());
    }

    @Test
    void beta18UsesAlphaPacket() {
        // Beta 1.8 is the floor for the pre-Netty branch — gameMode was
        // introduced in this version.
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.BETA_1_8, ch);
        int delivered = GameModeDispatcher.push(p, GameModeUtil.CREATIVE);
        assertEquals(GameModeUtil.CREATIVE, delivered);
        List<Packet> sent = drain(ch);
        assertTrue(sent.stream().anyMatch(x -> x instanceof ChangeGameStatePacket
                && !(x instanceof NettyChangeGameStatePacket)),
                "Beta 1.8 must use the pre-Netty packet; got " + sent);
    }

    @Test
    void preBeta18BetaClientNoOps() {
        // Beta 1.5 - 1.7.3: ChangeGameStatePacket exists on the wire but
        // creative mode itself isn't supported by the client. Dispatcher
        // bails out so the client doesn't see a nonsensical change.
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.BETA_1_7_3, ch);
        int delivered = GameModeDispatcher.push(p, GameModeUtil.CREATIVE);
        assertEquals(-1, delivered, "pre-Beta-1.8 must report no-delivery");
        assertTrue(drain(ch).isEmpty(), "no packet must be sent on pre-Beta-1.8");
    }

    @Test
    void classicAndRubyDungNoOp() {
        for (ProtocolVersion v : new ProtocolVersion[] {
                ProtocolVersion.RUBYDUNG, ProtocolVersion.CLASSIC}) {
            EmbeddedChannel ch = new EmbeddedChannel();
            ConnectedPlayer p = newPlayer(v, ch);
            int delivered = GameModeDispatcher.push(p, GameModeUtil.CREATIVE);
            assertEquals(-1, delivered, "Classic/RubyDung have no concept of gamemode");
            assertTrue(drain(ch).isEmpty(), "no packet on Classic/RubyDung; got " + v);
        }
    }

    @Test
    void nullVersionNoOp() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(null, ch);
        assertEquals(-1, GameModeDispatcher.push(p, GameModeUtil.CREATIVE));
        assertTrue(drain(ch).isEmpty());
    }

    @Test
    void pre131ClientReceivesAdventureClampedToSurvival() {
        // Beta 1.8 - Release 1.2.5 only knows survival/creative.
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_2_4, ch);
        int delivered = GameModeDispatcher.push(p, GameModeUtil.ADVENTURE);
        assertEquals(GameModeUtil.SURVIVAL, delivered,
                "pre-1.3.1 client must receive adventure as survival");
        ChangeGameStatePacket pkt = (ChangeGameStatePacket) drain(ch).stream()
                .filter(x -> x instanceof ChangeGameStatePacket)
                .findFirst().orElseThrow();
        assertEquals((byte) 0, pkt.getValue(),
                "wire value must reflect the post-clamp gamemode");
    }

    @Test
    void pre18ClientReceivesSpectatorClampedToSurvival() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_7_2, ch);
        int delivered = GameModeDispatcher.push(p, GameModeUtil.SPECTATOR);
        assertEquals(GameModeUtil.SURVIVAL, delivered,
                "pre-1.8 client must receive spectator as survival");
        NettyChangeGameStatePacket pkt = (NettyChangeGameStatePacket) drain(ch).stream()
                .filter(x -> x instanceof NettyChangeGameStatePacket)
                .findFirst().orElseThrow();
        assertEquals(0.0f, pkt.getValue(), 1e-6f);
    }

    @Test
    void connectedPlayerSetGameModePersistsDeliveredValue() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_7_2, ch);
        p.setGameMode(GameModeUtil.SPECTATOR);
        // 1.7.2 doesn't support spectator → clamped to survival on
        // the wire AND mirrored in getGameMode.
        assertEquals(GameModeUtil.SURVIVAL, p.getGameMode(),
                "getGameMode must reflect what the client actually has, not the requested mode");
    }

    @Test
    void connectedPlayerSetGameModeStoresEvenOnNoOpVersion() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.CLASSIC, ch);
        p.setGameMode(GameModeUtil.CREATIVE);
        // Classic has no wire field but plugin-side bookkeeping should
        // still see survival (the post-clamp value, not the request).
        assertEquals(GameModeUtil.SURVIVAL, p.getGameMode(),
                "Classic clamps everything to survival; getGameMode must show that");
    }
}
