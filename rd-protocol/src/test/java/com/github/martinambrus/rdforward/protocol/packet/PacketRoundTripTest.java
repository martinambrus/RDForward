package com.github.martinambrus.rdforward.protocol.packet;

import com.github.martinambrus.rdforward.protocol.McDataTypes;
import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import com.github.martinambrus.rdforward.protocol.packet.alpha.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies round-trip encoding/decoding for all packet types.
 * Each test creates a packet, writes it to a buffer, reads it back,
 * and verifies the fields match.
 */
class PacketRoundTripTest {

    private <T extends Packet> T roundTrip(T packet, ProtocolVersion version, PacketDirection direction) {
        ByteBuf buf = Unpooled.buffer();
        try {
            packet.write(buf);
            @SuppressWarnings("unchecked")
            T decoded = (T) PacketRegistry.createPacket(version, direction, packet.getPacketId());
            assertNotNull(decoded, "No packet registered for ID 0x" + Integer.toHexString(packet.getPacketId())
                    + " in " + version + " " + direction);
            decoded.read(buf);
            assertEquals(0, buf.readableBytes(), "Not all bytes consumed for " + packet.getClass().getSimpleName());
            return decoded;
        } finally {
            buf.release();
        }
    }

    // === Classic Packets ===

    @Test
    void classicPingRoundTrip() {
        PingPacket original = new PingPacket();
        PingPacket decoded = roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(0x01, decoded.getPacketId());
    }

    @Test
    void classicSetBlockServerRoundTrip() {
        SetBlockServerPacket original = new SetBlockServerPacket(10, 20, 30, 42);
        SetBlockServerPacket decoded = roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(10, decoded.getX());
        assertEquals(20, decoded.getY());
        assertEquals(30, decoded.getZ());
        assertEquals(42, decoded.getBlockType());
    }

    @Test
    void classicSetBlockClientRoundTrip() {
        SetBlockClientPacket original = new SetBlockClientPacket(5, 10, 15, 1, 4);
        SetBlockClientPacket decoded = roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(5, decoded.getX());
        assertEquals(10, decoded.getY());
        assertEquals(15, decoded.getZ());
        assertEquals(1, decoded.getMode());
        assertEquals(4, decoded.getBlockType());
    }

    @Test
    void classicMessageRoundTrip() {
        MessagePacket original = new MessagePacket((byte) 5, "Hello World");
        MessagePacket decoded = roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals("Hello World", decoded.getMessage().trim());
    }

    @Test
    void classicSpawnPlayerRoundTrip() {
        com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket original =
                new com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket(3, "TestPlayer", (short) 100, (short) 200, (short) 300, 90, 45);
        com.github.martinambrus.rdforward.protocol.packet.classic.SpawnPlayerPacket decoded =
                roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(3, decoded.getPlayerId());
        assertEquals("TestPlayer", decoded.getPlayerName().trim());
        assertEquals(100, decoded.getX());
        assertEquals(200, decoded.getY());
        assertEquals(300, decoded.getZ());
    }

    @Test
    void classicPlayerTeleportRoundTrip() {
        PlayerTeleportPacket original = new PlayerTeleportPacket(-1, (short) 50, (short) 100, (short) 150, 128, 64);
        PlayerTeleportPacket decoded = roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(50, decoded.getX());
        assertEquals(100, decoded.getY());
        assertEquals(150, decoded.getZ());
        assertEquals(128, decoded.getYaw());
        assertEquals(64, decoded.getPitch());
    }

    @Test
    void classicDespawnPlayerRoundTrip() {
        DespawnPlayerPacket original = new DespawnPlayerPacket(7);
        DespawnPlayerPacket decoded = roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(7, decoded.getPlayerId());
    }

    @Test
    void classicDisconnectRoundTrip() {
        com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket original =
                new com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket("Server full");
        com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket decoded =
                roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals("Server full", decoded.getReason().trim());
    }

    @Test
    void classicLevelFinalizeRoundTrip() {
        LevelFinalizePacket original = new LevelFinalizePacket(256, 64, 256);
        LevelFinalizePacket decoded = roundTrip(original, ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(256, decoded.getXSize());
        assertEquals(64, decoded.getYSize());
        assertEquals(256, decoded.getZSize());
    }

    // === Alpha Packets ===

    @Test
    void alphaKeepAliveRoundTrip() {
        KeepAlivePacket original = new KeepAlivePacket();
        KeepAlivePacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(0x00, decoded.getPacketId());
    }

    @Test
    void alphaChatRoundTrip() {
        ChatPacket original = new ChatPacket("Hello from Alpha!");
        ChatPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals("Hello from Alpha!", decoded.getMessage());
    }

    @Test
    void alphaTimeUpdateRoundTrip() {
        TimeUpdatePacket original = new TimeUpdatePacket(6000L);
        TimeUpdatePacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_2_5, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(6000L, decoded.getTime());
    }

    @Test
    void alphaBlockChangeRoundTrip() {
        BlockChangePacket original = new BlockChangePacket(100, 64, 200, 4, 2);
        BlockChangePacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(100, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(200, decoded.getZ());
        assertEquals(4, decoded.getBlockType());
        assertEquals(2, decoded.getMetadata());
    }

    @Test
    void alphaUpdateHealthRoundTrip() {
        UpdateHealthPacket original = new UpdateHealthPacket((short) 15);
        UpdateHealthPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(15, decoded.getHealth());
    }

    @Test
    void alphaSpawnPositionRoundTrip() {
        SpawnPositionPacket original = new SpawnPositionPacket(128, 65, 128);
        SpawnPositionPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(128, decoded.getX());
        assertEquals(65, decoded.getY());
        assertEquals(128, decoded.getZ());
    }

    @Test
    void alphaPreChunkRoundTrip() {
        PreChunkPacket original = new PreChunkPacket(3, -2, true);
        PreChunkPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(3, decoded.getChunkX());
        assertEquals(-2, decoded.getChunkZ());
        assertTrue(decoded.isLoad());
    }

    @Test
    void alphaDestroyEntityRoundTrip() {
        DestroyEntityPacket original = new DestroyEntityPacket(42);
        DestroyEntityPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(42, decoded.getEntityId());
    }

    @Test
    void alphaEntityRelativeMoveRoundTrip() {
        EntityRelativeMovePacket original = new EntityRelativeMovePacket(10, (byte) 5, (byte) -3, (byte) 7);
        EntityRelativeMovePacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(10, decoded.getEntityId());
        assertEquals(5, decoded.getDx());
        assertEquals(-3, decoded.getDy());
        assertEquals(7, decoded.getDz());
    }

    @Test
    void alphaEntityTeleportRoundTrip() {
        EntityTeleportPacket original = new EntityTeleportPacket(99, 1000, 2000, 3000, (byte) 128, (byte) 64);
        EntityTeleportPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(99, decoded.getEntityId());
        assertEquals(1000, decoded.getX());
        assertEquals(2000, decoded.getY());
        assertEquals(3000, decoded.getZ());
    }

    @Test
    void alphaDisconnectRoundTrip() {
        com.github.martinambrus.rdforward.protocol.packet.alpha.DisconnectPacket original =
                new com.github.martinambrus.rdforward.protocol.packet.alpha.DisconnectPacket("Kicked!");
        com.github.martinambrus.rdforward.protocol.packet.alpha.DisconnectPacket decoded =
                roundTrip(original, ProtocolVersion.ALPHA_1_0_15, PacketDirection.SERVER_TO_CLIENT);
        assertEquals("Kicked!", decoded.getReason());
    }

    // === V14 (pre-rewrite) uses same wire format as v6 ===

    @Test
    void v14BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacket original = new PlayerBlockPlacementPacket(10, 64, 20, 1, (short) 4);
        PlayerBlockPlacementPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_16, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
    }

    @Test
    void v6BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacket original = new PlayerBlockPlacementPacket(10, 64, 20, 1, (short) 4);
        PlayerBlockPlacementPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_2_5, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
    }

    @Test
    void v5BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacket original = new PlayerBlockPlacementPacket(10, 64, 20, 1, (short) 4);
        PlayerBlockPlacementPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_2_3, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
    }

    @Test
    void v4BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacket original = new PlayerBlockPlacementPacket(10, 64, 20, 1, (short) 4);
        PlayerBlockPlacementPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_2_2, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
    }

    @Test
    void v3BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacket original = new PlayerBlockPlacementPacket(10, 64, 20, 1, (short) 4);
        PlayerBlockPlacementPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_2_0, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
    }

    @Test
    void v2BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacket original = new PlayerBlockPlacementPacket(10, 64, 20, 1, (short) 4);
        PlayerBlockPlacementPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_1_0, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
    }

    @Test
    void v1BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacket original = new PlayerBlockPlacementPacket(10, 64, 20, 1, (short) 4);
        PlayerBlockPlacementPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_17, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
    }

    @Test
    void v14PickupSpawnRoundTrip() {
        PickupSpawnPacket original = new PickupSpawnPacket(42, 4, 1, 320, 2080, 640);
        PickupSpawnPacket decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_16, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(42, decoded.getEntityId());
        assertEquals(4, decoded.getItemId());
        assertEquals(1, decoded.getCount());
        assertEquals(320, decoded.getX());
        assertEquals(2080, decoded.getY());
        assertEquals(640, decoded.getZ());
    }

    // === Beta 1.0 Packets ===

    @Test
    void betaBlockPlacementRoundTrip() {
        PlayerBlockPlacementPacketBeta original = new PlayerBlockPlacementPacketBeta(
                10, 64, 20, 1, (short) 4, (byte) 1, (byte) 0);
        PlayerBlockPlacementPacketBeta decoded = roundTrip(original, ProtocolVersion.BETA_1_0, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
        assertEquals(1, decoded.getAmount());
        assertEquals(0, decoded.getDamage());
    }

    @Test
    void betaBlockPlacementEmptyHandRoundTrip() {
        PlayerBlockPlacementPacketBeta original = new PlayerBlockPlacementPacketBeta(
                5, 32, 15, -1, (short) -1, (byte) 0, (byte) 0);
        PlayerBlockPlacementPacketBeta decoded = roundTrip(original, ProtocolVersion.BETA_1_0, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(5, decoded.getX());
        assertEquals(32, decoded.getY());
        assertEquals(15, decoded.getZ());
        assertEquals(-1, decoded.getDirection());
        assertEquals(-1, decoded.getItemId());
    }

    @Test
    void betaHoldingChangeRoundTrip() {
        HoldingChangePacketBeta original = new HoldingChangePacketBeta((short) 3);
        HoldingChangePacketBeta decoded = roundTrip(original, ProtocolVersion.BETA_1_0, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(3, decoded.getSlotId());
    }

    // === Beta 1.2 Packets (v8) ===

    @Test
    void beta12BlockPlacementRoundTrip() {
        PlayerBlockPlacementPacketBeta original = new PlayerBlockPlacementPacketBeta(
                10, 64, 20, 1, (short) 4, (byte) 1, (byte) 0);
        PlayerBlockPlacementPacketBeta decoded = roundTrip(original, ProtocolVersion.BETA_1_2, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getX());
        assertEquals(64, decoded.getY());
        assertEquals(20, decoded.getZ());
        assertEquals(1, decoded.getDirection());
        assertEquals(4, decoded.getItemId());
        assertEquals(1, decoded.getAmount());
        assertEquals(0, decoded.getDamage());
    }

    @Test
    void beta12HoldingChangeRoundTrip() {
        HoldingChangePacketBeta original = new HoldingChangePacketBeta((short) 3);
        HoldingChangePacketBeta decoded = roundTrip(original, ProtocolVersion.BETA_1_2, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(3, decoded.getSlotId());
    }

    // === V2 Login Packet Variants ===

    @Test
    void v2LoginC2SRoundTrip() {
        LoginC2SPacketV2 original = new LoginC2SPacketV2(2, "TestPlayer");
        LoginC2SPacketV2 decoded = roundTrip(original, ProtocolVersion.ALPHA_1_1_0, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(2, decoded.getProtocolVersion());
        assertEquals("TestPlayer", decoded.getUsername());
    }

    @Test
    void v2LoginS2CRoundTrip() {
        LoginS2CPacketV2 original = new LoginS2CPacketV2(42);
        LoginS2CPacketV2 decoded = roundTrip(original, ProtocolVersion.ALPHA_1_1_0, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(42, decoded.getEntityId());
    }

    @Test
    void v1LoginC2SRoundTrip() {
        LoginC2SPacketV2 original = new LoginC2SPacketV2(1, "TestPlayer");
        LoginC2SPacketV2 decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_17, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(1, decoded.getProtocolVersion());
        assertEquals("TestPlayer", decoded.getUsername());
    }

    @Test
    void v1LoginS2CRoundTrip() {
        LoginS2CPacketV2 original = new LoginS2CPacketV2(42);
        LoginS2CPacketV2 decoded = roundTrip(original, ProtocolVersion.ALPHA_1_0_17, PacketDirection.SERVER_TO_CLIENT);
        assertEquals(42, decoded.getEntityId());
    }

    // === Beta 1.3 Packets (v9) ===

    @Test
    void beta13EntityActionRoundTrip() {
        EntityActionPacket original = new EntityActionPacket();
        // Write manually to set fields (packet has no constructor args)
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(42);   // entityId
        buf.writeByte(1);   // actionId (crouch)
        original.read(buf);
        buf.release();

        EntityActionPacket decoded = roundTrip(original, ProtocolVersion.BETA_1_3, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(42, decoded.getEntityId());
        assertEquals(1, decoded.getActionId());
    }

    @Test
    void beta13InputPacketRoundTrip() {
        InputPacket original = new InputPacket();
        InputPacket decoded = roundTrip(original, ProtocolVersion.BETA_1_3, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(0x1B, decoded.getPacketId());
    }

    // === Beta 1.4 Packets (v10) ===

    @Test
    void beta14LoginC2SRoundTrip() {
        // Beta 1.4 sends v10 which clashes with pre-rewrite Alpha v10.
        // The forceMapSeed flag ensures mapSeed/dimension are read correctly.
        LoginC2SPacket original = new LoginC2SPacket(10, "TestPlayer", true);
        LoginC2SPacket decoded = roundTrip(original, ProtocolVersion.BETA_1_4, PacketDirection.CLIENT_TO_SERVER);
        assertEquals(10, decoded.getProtocolVersion());
        assertEquals("TestPlayer", decoded.getUsername());
        assertEquals(0, decoded.getMapSeed());
        assertEquals(0, decoded.getDimension());
    }

    // === Beta 1.5 Packets (v11) — String16 encoding ===

    @Test
    void beta15LoginC2SString16RoundTrip() {
        // Write Login in String16 mode, read back, verify fields
        McDataTypes.STRING16_MODE.set(true);
        try {
            LoginC2SPacket original = new LoginC2SPacket(11, "TestPlayer", true);
            ByteBuf buf = Unpooled.buffer();
            try {
                original.write(buf);
                LoginC2SPacket decoded = (LoginC2SPacket) PacketRegistry.createPacket(
                        ProtocolVersion.BETA_1_5, PacketDirection.CLIENT_TO_SERVER, 0x01);
                assertNotNull(decoded);
                decoded.read(buf);
                assertEquals(0, buf.readableBytes(), "Not all bytes consumed");
                assertEquals(11, decoded.getProtocolVersion());
                assertEquals("TestPlayer", decoded.getUsername());
                assertEquals(0, decoded.getMapSeed());
                assertEquals(0, decoded.getDimension());
            } finally {
                buf.release();
            }
        } finally {
            McDataTypes.STRING16_MODE.remove();
        }
    }

    @Test
    void beta15ChatString16RoundTrip() {
        // Write Chat in String16 mode, read back, verify message
        McDataTypes.STRING16_MODE.set(true);
        try {
            ChatPacket original = new ChatPacket("Hello Beta 1.5!");
            ByteBuf buf = Unpooled.buffer();
            try {
                original.write(buf);
                ChatPacket decoded = new ChatPacket();
                decoded.read(buf);
                assertEquals(0, buf.readableBytes(), "Not all bytes consumed");
                assertEquals("Hello Beta 1.5!", decoded.getMessage());
            } finally {
                buf.release();
            }
        } finally {
            McDataTypes.STRING16_MODE.remove();
        }
    }

    @Test
    void beta15HandshakeC2SAutoDetection() {
        // Write Handshake in String16 format, read with auto-detect,
        // verify isDetectedString16() is true
        ByteBuf buf = Unpooled.buffer();
        try {
            // Manually write String16 format: char count + UTF-16BE
            McDataTypes.writeString16(buf, "TestPlayer");

            HandshakeC2SPacket decoded = new HandshakeC2SPacket();
            decoded.read(buf);
            assertEquals(0, buf.readableBytes(), "Not all bytes consumed");
            assertEquals("TestPlayer", decoded.getUsername());
            assertTrue(decoded.isDetectedString16(), "Should detect String16 format");
        } finally {
            buf.release();
        }
    }

    @Test
    void beta15HandshakeC2SAutoDetectionWriteUTF() {
        // Write Handshake in writeUTF format, read with auto-detect,
        // verify isDetectedString16() is false
        ByteBuf buf = Unpooled.buffer();
        try {
            McDataTypes.writeJavaUTF(buf, "TestPlayer");

            HandshakeC2SPacket decoded = new HandshakeC2SPacket();
            decoded.read(buf);
            assertEquals(0, buf.readableBytes(), "Not all bytes consumed");
            assertEquals("TestPlayer", decoded.getUsername());
            assertFalse(decoded.isDetectedString16(), "Should detect writeUTF format");
        } finally {
            buf.release();
        }
    }

    // === Registry Completeness ===

    @Test
    void allClassicServerToClientPacketsRegistered() {
        int[] classicS2CIds = {0x00, 0x01, 0x02, 0x03, 0x04, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};
        for (int id : classicS2CIds) {
            assertTrue(PacketRegistry.hasPacket(ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT, id),
                    "Classic S2C packet 0x" + Integer.toHexString(id) + " not registered");
        }
    }

    @Test
    void allClassicClientToServerPacketsRegistered() {
        int[] classicC2SIds = {0x00, 0x05, 0x08, 0x0D};
        for (int id : classicC2SIds) {
            assertTrue(PacketRegistry.hasPacket(ProtocolVersion.CLASSIC, PacketDirection.CLIENT_TO_SERVER, id),
                    "Classic C2S packet 0x" + Integer.toHexString(id) + " not registered");
        }
    }

    @Test
    void v2v3v4v5AndV6ShareSamePacketRegistrations() {
        // Post-rewrite Alpha v2, v3, v4, v5 and v6 use identical wire formats
        ProtocolVersion[] postRewriteVersions = {ProtocolVersion.ALPHA_1_0_17, ProtocolVersion.ALPHA_1_1_0, ProtocolVersion.ALPHA_1_2_0, ProtocolVersion.ALPHA_1_2_2, ProtocolVersion.ALPHA_1_2_3, ProtocolVersion.ALPHA_1_2_5};
        int[] sharedC2SIds = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x12, 0x15, 0xFF};
        int[] sharedS2CIds = {0x00, 0x01, 0x02, 0x03, 0x04, 0x06, 0x08, 0x0D, 0x11, 0x12, 0x14, 0x15, 0x16, 0x1D, 0x1F, 0x20, 0x21, 0x22, 0x32, 0x33, 0x35, 0xFF};
        for (int id : sharedC2SIds) {
            for (ProtocolVersion v : postRewriteVersions) {
                assertEquals(
                        PacketRegistry.hasPacket(ProtocolVersion.ALPHA_1_2_5, PacketDirection.CLIENT_TO_SERVER, id),
                        PacketRegistry.hasPacket(v, PacketDirection.CLIENT_TO_SERVER, id),
                        v.name() + " and v6 should share C2S packet 0x" + Integer.toHexString(id)
                );
            }
        }
        for (int id : sharedS2CIds) {
            for (ProtocolVersion v : postRewriteVersions) {
                assertEquals(
                        PacketRegistry.hasPacket(ProtocolVersion.ALPHA_1_2_5, PacketDirection.SERVER_TO_CLIENT, id),
                        PacketRegistry.hasPacket(v, PacketDirection.SERVER_TO_CLIENT, id),
                        v.name() + " and v6 should share S2C packet 0x" + Integer.toHexString(id)
                );
            }
        }
    }

    @Test
    void allBetaVersionsSharePacketRegistrations() {
        // All Beta versions (v7, v8, v9, v10, v11) share packet registrations
        ProtocolVersion[] betaVersions = {ProtocolVersion.BETA_1_0, ProtocolVersion.BETA_1_2, ProtocolVersion.BETA_1_3, ProtocolVersion.BETA_1_4, ProtocolVersion.BETA_1_5};
        int[] betaC2SIds = {0x00, 0x01, 0x02, 0x03, 0x04, 0x07, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x12, 0x13, 0x15, 0x1B, 0x65, 0x66, 0x67, 0x6A, 0x82, 0xFF};
        int[] betaS2CIds = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x08, 0x0D, 0x12, 0x14, 0x15, 0x16, 0x18, 0x1C, 0x1D, 0x1F, 0x20, 0x21, 0x22, 0x26, 0x27, 0x32, 0x33, 0x35, 0x3C, 0x64, 0x65, 0x67, 0x68, 0x69, 0x6A, 0x82, 0xFF};
        for (int id : betaC2SIds) {
            for (ProtocolVersion v : betaVersions) {
                assertEquals(
                        PacketRegistry.hasPacket(ProtocolVersion.BETA_1_0, PacketDirection.CLIENT_TO_SERVER, id),
                        PacketRegistry.hasPacket(v, PacketDirection.CLIENT_TO_SERVER, id),
                        "Beta 1.0 and " + v.name() + " should share C2S packet 0x" + Integer.toHexString(id));
            }
        }
        for (int id : betaS2CIds) {
            for (ProtocolVersion v : betaVersions) {
                assertEquals(
                        PacketRegistry.hasPacket(ProtocolVersion.BETA_1_0, PacketDirection.SERVER_TO_CLIENT, id),
                        PacketRegistry.hasPacket(v, PacketDirection.SERVER_TO_CLIENT, id),
                        "Beta 1.0 and " + v.name() + " should share S2C packet 0x" + Integer.toHexString(id));
            }
        }
    }

    @Test
    void rubyDungSharesClassicPacketRegistry() {
        // RubyDung uses same packets as Classic
        int[] ids = {0x00, 0x01, 0x06, 0x07, 0x08, 0x0D, 0x0E};
        for (int id : ids) {
            assertEquals(
                    PacketRegistry.hasPacket(ProtocolVersion.CLASSIC, PacketDirection.SERVER_TO_CLIENT, id),
                    PacketRegistry.hasPacket(ProtocolVersion.RUBYDUNG, PacketDirection.SERVER_TO_CLIENT, id),
                    "RubyDung and Classic should share S2C packet 0x" + Integer.toHexString(id)
            );
        }
    }
}
