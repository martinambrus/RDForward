package com.github.martinambrus.rdforward.server.abilities;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityPropertiesPacket;
import com.github.martinambrus.rdforward.protocol.packet.alpha.EntityPropertiesPacketV74;
import com.github.martinambrus.rdforward.protocol.packet.alpha.PlayerAbilitiesPacketV73;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacket;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacketV47;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacketV755;
import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityPropertiesPacketV766;
import com.github.martinambrus.rdforward.server.ConnectedPlayer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins {@link V73FloatAbilitiesDispatcher} behavior:
 *
 * <ul>
 *   <li>The on-wire {@code flyingSpeed} / {@code walkingSpeed} fields
 *       are half the Bukkit-API value (matches CraftPlayer).</li>
 *   <li>Modern clients (1.6.1+) also receive a {@code generic.movement_speed}
 *       attribute update — without it, the abilities packet only changes
 *       FOV and the player keeps walking at the default ground speed.</li>
 *   <li>The right per-version {@code EntityProperties} variant is picked
 *       (Alpha 1.6.1 / 1.6.2-1.6.4 / Netty 1.7.x / 1.8+ / 1.17+ / 1.20.5+).</li>
 * </ul>
 */
class V73FloatAbilitiesDispatcherTest {

    private static ConnectedPlayer newPlayer(ProtocolVersion v, EmbeddedChannel channel) {
        return new ConnectedPlayer((byte) 1, "tester", "00000000-0000-0000-0000-000000000001", channel, v);
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
    void abilitiesPacketCarriesValueOverTwo() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_21_5, ch);
        p.setFlySpeed(0.4f);   // pushAbilities runs inside setter
        // Drain just to see the burst from setFlySpeed first.
        List<Packet> first = drain(ch);
        PlayerAbilitiesPacketV73 abil = (PlayerAbilitiesPacketV73)
                first.stream().filter(x -> x instanceof PlayerAbilitiesPacketV73).findFirst().orElseThrow();
        assertEquals(0.2f, abil.getFlySpeed(), 1e-6f,
                "wire flySpeed must be API/2 (Bukkit semantics)");
        // Walk still default 0.2 → wire 0.1.
        assertEquals(0.1f, abil.getWalkSpeed(), 1e-6f);
    }

    @Test
    void walkSpeedPushesAttributeUpdateOnNettyV47() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_8, ch);
        p.setWalkSpeed(0.6f);
        List<Packet> sent = drain(ch);

        assertTrue(sent.stream().anyMatch(x -> x instanceof PlayerAbilitiesPacketV73),
                "abilities packet must be sent");
        NettyEntityPropertiesPacketV47 attr = (NettyEntityPropertiesPacketV47) sent.stream()
                .filter(x -> x instanceof NettyEntityPropertiesPacketV47)
                .findFirst().orElseThrow(() ->
                        new AssertionError("V47 attribute push missing; got " + sent));
        // 0.6 / 2 = 0.3 (with float→double rounding, ~0.30000001)
        assertEquals(0.3, attr.getValue(), 1e-6,
                "movement_speed attribute must be (walkSpeed/2) so ground speed actually changes");
    }

    @Test
    void v17ClientUsesV755AttributePacket() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_17, ch);
        p.setWalkSpeed(0.2f);
        List<Packet> sent = drain(ch);
        assertTrue(sent.stream().anyMatch(x -> x instanceof NettyEntityPropertiesPacketV755),
                "1.17+ must use the V755 EntityProperties packet; got " + sent);
    }

    @Test
    void v205ClientUsesV766AttributePacket() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_20_5, ch);
        p.setWalkSpeed(0.2f);
        List<Packet> sent = drain(ch);
        NettyEntityPropertiesPacketV766 attr = (NettyEntityPropertiesPacketV766) sent.stream()
                .filter(x -> x instanceof NettyEntityPropertiesPacketV766)
                .findFirst().orElseThrow(() ->
                        new AssertionError("V766 attribute push missing; got " + sent));
        assertEquals(NettyEntityPropertiesPacketV766.MOVEMENT_SPEED, attr.getAttributeId(),
                "1.20.5+ uses VarInt registry id, must be MOVEMENT_SPEED");
    }

    @Test
    void v17xClientUsesNettyV47Packet() {
        // 1.7.2 - 1.7.10: NettyEntityPropertiesPacket (no modifier list len change yet).
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_7_2, ch);
        p.setWalkSpeed(0.2f);
        List<Packet> sent = drain(ch);
        assertTrue(sent.stream().anyMatch(x -> x instanceof NettyEntityPropertiesPacket
                && !(x instanceof NettyEntityPropertiesPacketV47)),
                "1.7.x must use the base NettyEntityPropertiesPacket; got " + sent);
    }

    @Test
    void alpha164ClientUsesV74AttributePacket() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_6_4, ch);
        p.setWalkSpeed(0.2f);
        List<Packet> sent = drain(ch);
        assertTrue(sent.stream().anyMatch(x -> x instanceof EntityPropertiesPacketV74),
                "1.6.2 - 1.6.4 (Alpha pipeline) must use EntityPropertiesPacketV74; got " + sent);
    }

    @Test
    void alpha161UsesPreModifierListAttributePacket() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_6_1, ch);
        p.setWalkSpeed(0.2f);
        List<Packet> sent = drain(ch);
        assertTrue(sent.stream().anyMatch(x -> x instanceof EntityPropertiesPacket
                && !(x instanceof EntityPropertiesPacketV74)),
                "1.6.1 only must use the pre-modifier-list EntityPropertiesPacket; got " + sent);
    }

    @Test
    void modernKeyIsNamespaced() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_16, ch);
        p.setWalkSpeed(0.2f);
        List<Packet> sent = drain(ch);
        NettyEntityPropertiesPacketV47 attr = (NettyEntityPropertiesPacketV47) sent.stream()
                .filter(x -> x instanceof NettyEntityPropertiesPacketV47)
                .findFirst().orElseThrow();
        assertEquals("minecraft:generic.movement_speed", attr.getKey(),
                "1.16+ uses namespaced snake_case attribute keys");
    }

    @Test
    void preModernKeyIsLegacyDotted() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ConnectedPlayer p = newPlayer(ProtocolVersion.RELEASE_1_8, ch);
        p.setWalkSpeed(0.2f);
        List<Packet> sent = drain(ch);
        NettyEntityPropertiesPacketV47 attr = (NettyEntityPropertiesPacketV47) sent.stream()
                .filter(x -> x instanceof NettyEntityPropertiesPacketV47)
                .findFirst().orElseThrow();
        assertEquals("generic.movementSpeed", attr.getKey(),
                "pre-1.16 uses the legacy camelCase attribute key");
    }
}
