package com.github.martinambrus.rdforward.server.eaglercraft;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.server.AlphaConnectionHandler;
import com.github.martinambrus.rdforward.server.ChunkManager;
import com.github.martinambrus.rdforward.server.NettyConnectionHandler;
import com.github.martinambrus.rdforward.server.PlayerManager;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.world.FlatWorldGenerator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static com.github.martinambrus.rdforward.server.eaglercraft.EaglerCraftConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EaglerCraftHandshakeHandler covering:
 * - EaglerCraft v2/v3 handshake protocol negotiation (MC protocols 14, 47, 340)
 * - Pre-Netty detection (0x02 first byte for EaglerCraft 1.5.2 / Beta 1.7.3)
 * - Pipeline reconfiguration (AlphaConnectionHandler for pre-Netty, NettyConnectionHandler for Netty)
 * - Version mismatch and deny responses
 */
class EaglerCraftHandshakeHandlerTest {

    @TempDir
    static Path tempDir;

    private static ServerWorld world;
    private static PlayerManager playerManager;
    private static ChunkManager chunkManager;

    @BeforeAll
    static void setUp() {
        world = new ServerWorld(16, 16, 16);
        world.generate(new FlatWorldGenerator(), 0L);
        playerManager = new PlayerManager();
        chunkManager = new ChunkManager(new FlatWorldGenerator(), 0L, tempDir.toFile(), 2);
    }

    /**
     * Create an EmbeddedChannel with the EaglerCraft handshake pipeline.
     *
     * Pipeline (HEAD → TAIL): wsFrameEncoder → wsFrameDecoder → eaglerQuery → eaglerTimeout → eaglerHandshake
     *
     * Stub handlers (ChannelDuplexHandler) pass through both inbound and outbound.
     * This lets us write raw ByteBuf inbound and read raw ByteBuf outbound.
     */
    private EmbeddedChannel createChannel() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ch.pipeline().addLast("wsFrameEncoder", new ChannelDuplexHandler());
        ch.pipeline().addLast("wsFrameDecoder", new ChannelDuplexHandler());
        ch.pipeline().addLast("eaglerQuery", new ChannelInboundHandlerAdapter());
        ch.pipeline().addLast("eaglerTimeout", new ReadTimeoutHandler(30));
        ch.pipeline().addLast("eaglerHandshake",
                new EaglerCraftHandshakeHandler(ProtocolVersion.CLASSIC, world, playerManager, chunkManager));
        return ch;
    }

    /**
     * Build a CLIENT_VERSION (0x01) packet with EaglerCraft v2 protocol
     * and the specified MC protocol versions.
     */
    private ByteBuf buildClientVersion(int... mcProtocols) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PROTOCOL_CLIENT_VERSION);    // 0x01
        buf.writeByte(0x02);                        // legacy version marker (v2+)
        buf.writeShort(1);                          // eagler protocol count
        buf.writeShort(EAGLER_PROTOCOL_V2);         // eagler v2
        buf.writeShort(mcProtocols.length);         // MC protocol count
        for (int p : mcProtocols) {
            buf.writeShort(p);
        }
        byte[] brand = "TestClient".getBytes(StandardCharsets.US_ASCII);
        buf.writeByte(brand.length);
        buf.writeBytes(brand);
        byte[] version = "1.0.0".getBytes(StandardCharsets.US_ASCII);
        buf.writeByte(version.length);
        buf.writeBytes(version);
        buf.writeBoolean(false);    // auth disabled
        buf.writeByte(0);           // auth username (empty)
        return buf;
    }

    /** Build a V1 CLIENT_VERSION packet (no MC protocol info). */
    private ByteBuf buildV1ClientVersion() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PROTOCOL_CLIENT_VERSION);  // 0x01
        buf.writeByte(0x01);                      // legacy byte (v1 — not 0x02/0x03)
        return buf;
    }

    /** Build a V1 CLIENT_VERSION packet with explicit MC protocol. */
    private ByteBuf buildV1ClientVersionWithMcProtocol(int mcProtocol) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PROTOCOL_CLIENT_VERSION);  // 0x01
        buf.writeByte(0x01);                      // legacy byte (v1)
        buf.writeShort(mcProtocol);               // MC protocol version
        return buf;
    }

    /** Build a V1 CLIENT_REQUEST_LOGIN (no server/password fields). */
    private ByteBuf buildV1ClientRequestLogin(String username) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PROTOCOL_CLIENT_REQUEST_LOGIN);
        byte[] nameBytes = username.getBytes(StandardCharsets.US_ASCII);
        buf.writeByte(nameBytes.length);
        buf.writeBytes(nameBytes);
        // V1: no requested server or password fields
        return buf;
    }

    /** Build a CLIENT_REQUEST_LOGIN (0x04) packet. */
    private ByteBuf buildClientRequestLogin(String username) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PROTOCOL_CLIENT_REQUEST_LOGIN);
        byte[] nameBytes = username.getBytes(StandardCharsets.US_ASCII);
        buf.writeByte(nameBytes.length);
        buf.writeBytes(nameBytes);
        buf.writeByte(0); // requested server (empty)
        buf.writeByte(0); // password (empty)
        return buf;
    }

    /** Build a CLIENT_FINISH_LOGIN (0x08) packet. */
    private ByteBuf buildClientFinishLogin() {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PROTOCOL_CLIENT_FINISH_LOGIN);
        return buf;
    }

    /** Read and discard all pending outbound messages. */
    private void drainOutbound(EmbeddedChannel ch) {
        ByteBuf msg;
        while ((msg = ch.readOutbound()) != null) {
            msg.release();
        }
    }

    // ---- Protocol negotiation tests ----

    @Test
    void serverVersionResponseForProtocol14() {
        EmbeddedChannel ch = createChannel();
        ch.writeInbound(buildClientVersion(MC_PROTOCOL_14));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected SERVER_VERSION response");
        assertEquals(PROTOCOL_SERVER_VERSION, resp.readUnsignedByte());
        assertEquals(EAGLER_PROTOCOL_V2, resp.readUnsignedShort());
        assertEquals(MC_PROTOCOL_14, resp.readUnsignedShort());
        resp.release();
        ch.finishAndReleaseAll();
    }

    @Test
    void serverVersionResponseForProtocol47() {
        EmbeddedChannel ch = createChannel();
        ch.writeInbound(buildClientVersion(MC_PROTOCOL_47));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected SERVER_VERSION response");
        assertEquals(PROTOCOL_SERVER_VERSION, resp.readUnsignedByte());
        assertEquals(EAGLER_PROTOCOL_V2, resp.readUnsignedShort());
        assertEquals(MC_PROTOCOL_47, resp.readUnsignedShort());
        resp.release();
        ch.finishAndReleaseAll();
    }

    @Test
    void serverVersionResponseForProtocol340() {
        EmbeddedChannel ch = createChannel();
        ch.writeInbound(buildClientVersion(MC_PROTOCOL_340));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected SERVER_VERSION response");
        assertEquals(PROTOCOL_SERVER_VERSION, resp.readUnsignedByte());
        assertEquals(EAGLER_PROTOCOL_V2, resp.readUnsignedShort());
        assertEquals(MC_PROTOCOL_340, resp.readUnsignedShort());
        resp.release();
        ch.finishAndReleaseAll();
    }

    @Test
    void highestSupportedProtocolSelected() {
        EmbeddedChannel ch = createChannel();
        ch.writeInbound(buildClientVersion(MC_PROTOCOL_14, MC_PROTOCOL_47, MC_PROTOCOL_340));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp);
        resp.readUnsignedByte();  // SERVER_VERSION type
        resp.readUnsignedShort(); // eagler protocol
        assertEquals(MC_PROTOCOL_340, resp.readUnsignedShort(),
                "Server should select highest MC protocol (340 > 47 > 14)");
        resp.release();
        ch.finishAndReleaseAll();
    }

    @Test
    void protocol14SelectedOverUnsupported() {
        EmbeddedChannel ch = createChannel();
        // Client supports 14 and some unsupported protocol
        ch.writeInbound(buildClientVersion(MC_PROTOCOL_14, 999));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp);
        resp.readUnsignedByte();  // SERVER_VERSION type
        resp.readUnsignedShort(); // eagler protocol
        assertEquals(MC_PROTOCOL_14, resp.readUnsignedShort(),
                "Server should select 14 (only supported option)");
        resp.release();
        ch.finishAndReleaseAll();
    }

    @Test
    void unsupportedProtocolDenied() {
        EmbeddedChannel ch = createChannel();
        ch.writeInbound(buildClientVersion(999));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected deny response");
        assertEquals(PROTOCOL_SERVER_DENY_LOGIN, resp.readUnsignedByte());
        resp.release();
        ch.finishAndReleaseAll();
    }

    // ---- Full handshake + pipeline reconfiguration tests ----

    @Test
    void fullHandshakeProtocol14InstallsAlphaHandler() {
        EmbeddedChannel ch = createChannel();

        // Step 1: CLIENT_VERSION → SERVER_VERSION
        ch.writeInbound(buildClientVersion(MC_PROTOCOL_14));
        drainOutbound(ch);

        // Step 2: CLIENT_REQUEST_LOGIN → SERVER_ALLOW_LOGIN
        ch.writeInbound(buildClientRequestLogin("EagleBeta173"));
        drainOutbound(ch);

        // Step 3: CLIENT_FINISH_LOGIN → triggers completeHandshake
        ch.writeInbound(buildClientFinishLogin());

        // Verify SERVER_FINISH_LOGIN was sent
        ByteBuf first = ch.readOutbound();
        assertNotNull(first, "Expected SERVER_FINISH_LOGIN");
        assertEquals(PROTOCOL_SERVER_FINISH_LOGIN, first.readUnsignedByte());
        first.release();

        // Verify pipeline was reconfigured for pre-Netty (Beta 1.7.3)
        assertNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class),
                "Handshake handler should be removed after completion");
        assertNotNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "Protocol 14 should install AlphaConnectionHandler");
        assertNull(ch.pipeline().get(NettyConnectionHandler.class),
                "Protocol 14 should NOT install NettyConnectionHandler");

        // Verify channel attributes
        assertTrue(Boolean.TRUE.equals(ch.attr(ATTR_IS_EAGLECRAFT).get()));
        assertEquals("EagleBeta173", ch.attr(ATTR_EAGLER_USERNAME).get());

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void fullHandshakeProtocol47InstallsNettyHandler() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildClientVersion(MC_PROTOCOL_47));
        drainOutbound(ch);

        ch.writeInbound(buildClientRequestLogin("EagleCraft18"));
        drainOutbound(ch);

        ch.writeInbound(buildClientFinishLogin());

        ByteBuf first = ch.readOutbound();
        assertNotNull(first, "Expected SERVER_FINISH_LOGIN");
        assertEquals(PROTOCOL_SERVER_FINISH_LOGIN, first.readUnsignedByte());
        first.release();

        // Verify pipeline was reconfigured for Netty (1.8)
        assertNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class));
        assertNotNull(ch.pipeline().get(NettyConnectionHandler.class),
                "Protocol 47 should install NettyConnectionHandler");
        assertNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "Protocol 47 should NOT install AlphaConnectionHandler");

        assertTrue(Boolean.TRUE.equals(ch.attr(ATTR_IS_EAGLECRAFT).get()));
        assertEquals("EagleCraft18", ch.attr(ATTR_EAGLER_USERNAME).get());

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void fullHandshakeProtocol340InstallsNettyHandler() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildClientVersion(MC_PROTOCOL_340));
        drainOutbound(ch);

        ch.writeInbound(buildClientRequestLogin("EagleCraft1122"));
        drainOutbound(ch);

        ch.writeInbound(buildClientFinishLogin());

        ByteBuf first = ch.readOutbound();
        assertNotNull(first, "Expected SERVER_FINISH_LOGIN");
        assertEquals(PROTOCOL_SERVER_FINISH_LOGIN, first.readUnsignedByte());
        first.release();

        assertNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class));
        assertNotNull(ch.pipeline().get(NettyConnectionHandler.class),
                "Protocol 340 should install NettyConnectionHandler");

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    // ---- Pre-Netty detection tests (raw MC handshake over WebSocket) ----

    @Test
    void preNettyDetectionSetsEaglecraftFlagAndInstallsAlphaHandler() {
        EmbeddedChannel ch = createChannel();

        // Build a Beta 1.7.3 MC Handshake (0x02) with String16 username.
        // This is what EaglerCraft 1.5.2 / Beta 1.7.3 sends as its first frame.
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x02); // MC Handshake packet ID
        String username = "EaglePre";
        buf.writeShort(username.length()); // String16 char count
        for (char c : username.toCharArray()) {
            buf.writeShort(c); // UTF-16BE
        }

        ch.writeInbound(buf);

        // Verify ATTR_IS_EAGLECRAFT is set
        assertTrue(Boolean.TRUE.equals(ch.attr(ATTR_IS_EAGLECRAFT).get()),
                "Pre-Netty EaglerCraft connection should set ATTR_IS_EAGLECRAFT");

        // Verify pipeline reconfigured for pre-Netty
        assertNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class),
                "Handshake handler should be removed");
        assertNotNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "Pre-Netty detection should install AlphaConnectionHandler");

        // There should be outbound data (HandshakeS2C "-" response from AlphaConnectionHandler)
        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "AlphaConnectionHandler should send HandshakeS2C response");
        resp.release();

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void preNettyDetectionRemovesEaglerQueryAndTimeout() {
        EmbeddedChannel ch = createChannel();

        // Verify query and timeout handlers exist before
        assertNotNull(ch.pipeline().get("eaglerQuery"));
        assertNotNull(ch.pipeline().get("eaglerTimeout"));

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x02);
        String username = "Test";
        buf.writeShort(username.length());
        for (char c : username.toCharArray()) {
            buf.writeShort(c);
        }
        ch.writeInbound(buf);

        // Verify query and timeout handlers were removed during pipeline reconfiguration
        assertNull(ch.pipeline().get("eaglerQuery"),
                "eaglerQuery should be removed after pre-Netty detection");
        assertNull(ch.pipeline().get("eaglerTimeout"),
                "eaglerTimeout should be removed after pre-Netty detection");

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void rawMcLoginInstallsAlphaHandlerAndSetsAttributes() {
        EmbeddedChannel ch = createChannel();

        // Build a raw MC Login Request (0x01) as PeytonPlayz595 Beta 1.7.3 sends it.
        // Wire format: [0x01][int:14][String16:"rdf"][long:0][byte:0]
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x01);        // MC Login packet ID
        buf.writeInt(14);           // protocol version (Beta 1.7.3)
        String user = "rdf";
        buf.writeShort(user.length());
        for (char c : user.toCharArray()) {
            buf.writeShort(c);      // String16 UTF-16BE
        }
        buf.writeLong(0);           // map seed
        buf.writeByte(0);           // dimension

        ch.writeInbound(buf);

        // Verify pipeline reconfigured for pre-Netty
        assertNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class),
                "Handshake handler should be removed");
        assertNotNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "Raw MC Login should install AlphaConnectionHandler");

        // Verify channel attributes
        assertTrue(Boolean.TRUE.equals(ch.attr(ATTR_IS_EAGLECRAFT).get()));
        assertEquals("rdf", ch.attr(ATTR_EAGLER_USERNAME).get());

        // Verify eaglerQuery and eaglerTimeout removed
        assertNull(ch.pipeline().get("eaglerQuery"));
        assertNull(ch.pipeline().get("eaglerTimeout"));

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void rawMcLoginSendsLoginS2CResponse() {
        EmbeddedChannel ch = createChannel();

        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x01);
        buf.writeInt(14);
        String user = "BetaTest";
        buf.writeShort(user.length());
        for (char c : user.toCharArray()) {
            buf.writeShort(c);
        }
        buf.writeLong(0);
        buf.writeByte(0);

        ch.writeInbound(buf);

        // The server should send Login S2C (0x01) as the first outbound packet
        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected Login S2C response");
        int packetId = resp.readUnsignedByte();
        assertEquals(0x01, packetId, "First response should be Login S2C (0x01)");
        resp.release();

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void rawMcLoginExtractsUsernameFromString16() {
        EmbeddedChannel ch = createChannel();

        // Test with a longer username to verify String16 parsing
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x01);
        buf.writeInt(14);
        String user = "PeytonPlayz595";
        buf.writeShort(user.length());
        for (char c : user.toCharArray()) {
            buf.writeShort(c);
        }
        buf.writeLong(42);      // non-zero map seed
        buf.writeByte(0);

        ch.writeInbound(buf);

        assertEquals("PeytonPlayz595", ch.attr(ATTR_EAGLER_USERNAME).get());
        assertNotNull(ch.pipeline().get(AlphaConnectionHandler.class));

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void rawMcLoginDoesNotTriggerForNonZeroFirstByte() {
        // If legacyByte != 0x00, it should NOT be detected as raw MC Login.
        // legacyByte 0x02/0x03 → v2/v3 path, anything else → v1 path.
        EmbeddedChannel ch = createChannel();

        // Build a packet where legacyByte = 0x01 (v1 path, not raw MC Login)
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(PROTOCOL_CLIENT_VERSION);  // 0x01
        buf.writeByte(0x01);                      // legacyByte = 0x01 → v1 path
        ch.writeInbound(buf);

        // Should enter V1 path and send SERVER_VERSION, not set up Alpha pipeline
        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected V1 SERVER_VERSION response");
        assertEquals(PROTOCOL_SERVER_VERSION, resp.readUnsignedByte());
        resp.release();

        // Handshake handler should still be in pipeline (waiting for login)
        assertNotNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class),
                "V1 path should keep handshake handler for login step");

        ch.finishAndReleaseAll();
    }

    // ---- Allow-login response tests ----

    @Test
    void allowLoginResponseContainsUsernameAndUuid() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildClientVersion(MC_PROTOCOL_47));
        drainOutbound(ch);

        ch.writeInbound(buildClientRequestLogin("TestUser"));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected SERVER_ALLOW_LOGIN");
        assertEquals(PROTOCOL_SERVER_ALLOW_LOGIN, resp.readUnsignedByte());

        // Read username back
        int nameLen = resp.readUnsignedByte();
        assertEquals(8, nameLen);
        byte[] nameBytes = new byte[nameLen];
        resp.readBytes(nameBytes);
        assertEquals("TestUser", new String(nameBytes, StandardCharsets.US_ASCII));

        // Read UUID (16 bytes)
        long msb = resp.readLong();
        long lsb = resp.readLong();
        assertNotEquals(0, msb | lsb, "UUID should be non-zero");

        resp.release();
        ch.finishAndReleaseAll();
    }

    // ---- V1 protocol tests ----

    @Test
    void v1ServerVersionResponseIsTwoBytes() {
        EmbeddedChannel ch = createChannel();
        ch.writeInbound(buildV1ClientVersion());

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected V1 SERVER_VERSION response");
        assertEquals(2, resp.readableBytes(), "V1 SERVER_VERSION should be exactly 2 bytes");
        assertEquals(PROTOCOL_SERVER_VERSION, resp.readUnsignedByte());
        assertEquals(EAGLER_PROTOCOL_V1, resp.readUnsignedByte());
        resp.release();
        ch.finishAndReleaseAll();
    }

    @Test
    void v1WithExplicitProtocol14InstallsAlphaHandler() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildV1ClientVersionWithMcProtocol(MC_PROTOCOL_14));
        drainOutbound(ch);

        ch.writeInbound(buildV1ClientRequestLogin("BetaPlayer"));
        drainOutbound(ch);

        ch.writeInbound(buildClientFinishLogin());
        drainOutbound(ch);

        // V1 with explicit protocol 14 → direct pre-Netty pipeline (no detection needed)
        assertNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class));
        assertNotNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "V1 with explicit MC protocol 14 should install AlphaConnectionHandler directly");
        assertNull(ch.pipeline().get("v1Detect"),
                "V1 with explicit MC protocol should NOT install v1Detect handler");

        ch.finishAndReleaseAll();
    }

    @Test
    void v1WithoutMcProtocolInstallsDetectionHandler() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildV1ClientVersion());
        drainOutbound(ch);

        ch.writeInbound(buildV1ClientRequestLogin("V1Player"));
        drainOutbound(ch);

        ch.writeInbound(buildClientFinishLogin());
        drainOutbound(ch);

        // V1 without MC protocol → v1Detect handler should be installed
        assertNull(ch.pipeline().get(EaglerCraftHandshakeHandler.class),
                "Handshake handler should be removed");
        assertNotNull(ch.pipeline().get("v1Detect"),
                "V1 without explicit MC protocol should install v1Detect handler");
        // No MC handler yet — waiting for first frame or timeout
        assertNull(ch.pipeline().get(AlphaConnectionHandler.class));
        assertNull(ch.pipeline().get(NettyConnectionHandler.class));

        ch.finishAndReleaseAll();
    }

    @Test
    void v1DetectionInstallsPreNettyOnHandshakeByte() {
        EmbeddedChannel ch = createChannel();

        // Complete v1 handshake without MC protocol
        ch.writeInbound(buildV1ClientVersion());
        drainOutbound(ch);
        ch.writeInbound(buildV1ClientRequestLogin("DetectBeta"));
        drainOutbound(ch);
        ch.writeInbound(buildClientFinishLogin());
        drainOutbound(ch);

        // Verify v1Detect is installed
        assertNotNull(ch.pipeline().get("v1Detect"));

        // Send a frame starting with 0x02 (MC Handshake = pre-Netty)
        ByteBuf frame = Unpooled.buffer();
        frame.writeByte(0x02);
        String user = "DetectBeta";
        frame.writeShort(user.length());
        for (char c : user.toCharArray()) {
            frame.writeShort(c); // String16 UTF-16BE
        }
        ch.writeInbound(frame);

        // Detection should have resolved to pre-Netty
        assertNull(ch.pipeline().get("v1Detect"),
                "v1Detect should be removed after detection");
        assertNotNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "Pre-Netty first byte should install AlphaConnectionHandler");

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void v1DetectionInstallsPreNettyOnLoginByte() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildV1ClientVersion());
        drainOutbound(ch);
        ch.writeInbound(buildV1ClientRequestLogin("DetectLogin"));
        drainOutbound(ch);
        ch.writeInbound(buildClientFinishLogin());
        drainOutbound(ch);

        assertNotNull(ch.pipeline().get("v1Detect"));

        // Send a frame starting with 0x01 (MC Login = pre-Netty)
        ByteBuf frame = Unpooled.buffer();
        frame.writeByte(0x01);
        frame.writeInt(14); // protocol version (int)
        ch.writeInbound(frame);

        assertNull(ch.pipeline().get("v1Detect"));
        assertNotNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "Pre-Netty login byte should install AlphaConnectionHandler");

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    @Test
    void v1LoginWithoutServerPasswordSucceeds() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildV1ClientVersion());
        drainOutbound(ch);

        // V1 login: just username, no server/password fields
        ch.writeInbound(buildV1ClientRequestLogin("V1Login"));

        ByteBuf resp = ch.readOutbound();
        assertNotNull(resp, "Expected SERVER_ALLOW_LOGIN for V1 login");
        assertEquals(PROTOCOL_SERVER_ALLOW_LOGIN, resp.readUnsignedByte());
        resp.release();

        // Verify username was accepted
        drainOutbound(ch);

        // Finish login should work
        ch.writeInbound(buildClientFinishLogin());
        drainOutbound(ch);

        assertEquals("V1Login", ch.attr(ATTR_EAGLER_USERNAME).get());
        assertTrue(Boolean.TRUE.equals(ch.attr(ATTR_IS_EAGLECRAFT).get()));

        ch.finishAndReleaseAll();
    }

    @Test
    void v1DetectionTimeoutInstallsNettyHandler() {
        EmbeddedChannel ch = createChannel();
        ch.freezeTime();

        // Complete v1 handshake without MC protocol
        ch.writeInbound(buildV1ClientVersion());
        drainOutbound(ch);
        ch.writeInbound(buildV1ClientRequestLogin("TimeoutUser"));
        drainOutbound(ch);
        ch.writeInbound(buildClientFinishLogin());
        drainOutbound(ch);

        // v1Detect should be installed, no MC handler yet
        assertNotNull(ch.pipeline().get("v1Detect"),
                "v1Detect should be installed after v1 handshake");
        assertNull(ch.pipeline().get(AlphaConnectionHandler.class));
        assertNull(ch.pipeline().get(NettyConnectionHandler.class));

        // Advance time past the 500ms timeout
        ch.advanceTimeBy(501, TimeUnit.MILLISECONDS);
        // Run the scheduled timeout task
        ch.runPendingTasks();

        // Timeout should have resolved to Netty 1.8
        assertNull(ch.pipeline().get("v1Detect"),
                "v1Detect should be removed after timeout");
        assertNotNull(ch.pipeline().get(NettyConnectionHandler.class),
                "Timeout should install NettyConnectionHandler (assuming 1.8)");
        assertNull(ch.pipeline().get(AlphaConnectionHandler.class),
                "Timeout should NOT install AlphaConnectionHandler");

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }

    // ---- Skin data handling test ----

    @Test
    void profileDataSkinStoredInChannelAttribute() {
        EmbeddedChannel ch = createChannel();

        ch.writeInbound(buildClientVersion(MC_PROTOCOL_47));
        drainOutbound(ch);

        ch.writeInbound(buildClientRequestLogin("SkinUser"));
        drainOutbound(ch);

        // Send CLIENT_PROFILE_DATA with skin_v1 type
        ByteBuf skinPacket = Unpooled.buffer();
        skinPacket.writeByte(PROTOCOL_CLIENT_PROFILE_DATA);
        byte[] typeStr = "skin_v1".getBytes(StandardCharsets.US_ASCII);
        skinPacket.writeByte(typeStr.length);
        skinPacket.writeBytes(typeStr);
        byte[] skinPayload = {SKIN_TYPE_PRESET, 0, 0, 0, 1}; // preset skin ID 1
        skinPacket.writeShort(skinPayload.length);
        skinPacket.writeBytes(skinPayload);
        ch.writeInbound(skinPacket);

        // Finish login
        ch.writeInbound(buildClientFinishLogin());

        // Verify skin data was stored in channel attribute
        byte[] storedSkin = ch.attr(ATTR_EAGLER_SKIN).get();
        assertNotNull(storedSkin, "Skin data should be stored in channel attribute");
        assertEquals(SKIN_TYPE_PRESET, storedSkin[0] & 0xFF);

        drainOutbound(ch);
        ch.finishAndReleaseAll();
    }
}
