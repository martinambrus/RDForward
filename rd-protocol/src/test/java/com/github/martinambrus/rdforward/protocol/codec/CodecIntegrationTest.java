package com.github.martinambrus.rdforward.protocol.codec;

import com.github.martinambrus.rdforward.protocol.ProtocolVersion;
import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.protocol.packet.PacketDirection;
import com.github.martinambrus.rdforward.protocol.packet.classic.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Netty codec pipeline.
 * Verifies that packets survive encode → decode round-trips through the
 * actual PacketEncoder/PacketDecoder as they would on the wire.
 */
class CodecIntegrationTest {

    /**
     * Create an outbound-only channel with a PacketEncoder.
     */
    private EmbeddedChannel outboundChannel() {
        return new EmbeddedChannel(new PacketEncoder());
    }

    /**
     * Create an inbound-only channel with a PacketDecoder.
     */
    private EmbeddedChannel inboundChannel(PacketDirection dir, ProtocolVersion version) {
        return new EmbeddedChannel(new PacketDecoder(dir, version));
    }

    /**
     * Encode a packet and then decode the resulting bytes.
     */
    private Packet encodeThenDecode(Packet packet, PacketDirection decodeDir, ProtocolVersion version) {
        // Encode
        EmbeddedChannel encoder = outboundChannel();
        assertTrue(encoder.writeOutbound(packet));
        ByteBuf encoded = encoder.readOutbound();
        assertNotNull(encoded);

        // Decode
        EmbeddedChannel decoder = inboundChannel(decodeDir, version);
        assertTrue(decoder.writeInbound(encoded));
        Packet decoded = decoder.readInbound();
        assertNotNull(decoded, "Decoder produced no packet");

        encoder.finishAndReleaseAll();
        decoder.finishAndReleaseAll();
        return decoded;
    }

    @Test
    void pingPacketSurvivesCodecRoundTrip() {
        PingPacket original = new PingPacket();
        Packet decoded = encodeThenDecode(original, PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.CLASSIC);
        assertInstanceOf(PingPacket.class, decoded);
    }

    @Test
    void messagePacketPreservesContent() {
        MessagePacket original = new MessagePacket((byte) 0, "Hello World");
        Packet decoded = encodeThenDecode(original, PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.CLASSIC);
        assertInstanceOf(MessagePacket.class, decoded);
        assertEquals("Hello World", ((MessagePacket) decoded).getMessage().trim());
    }

    @Test
    void setBlockServerPacketPreservesCoordinates() {
        SetBlockServerPacket original = new SetBlockServerPacket(100, 50, 200, 42);
        Packet decoded = encodeThenDecode(original, PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.CLASSIC);
        assertInstanceOf(SetBlockServerPacket.class, decoded);
        SetBlockServerPacket sb = (SetBlockServerPacket) decoded;
        assertEquals(100, sb.getX());
        assertEquals(50, sb.getY());
        assertEquals(200, sb.getZ());
        assertEquals(42, sb.getBlockType());
    }

    @Test
    void setBlockClientPacketPreservesFields() {
        SetBlockClientPacket original = new SetBlockClientPacket(10, 20, 30, 1, 5);
        Packet decoded = encodeThenDecode(original, PacketDirection.CLIENT_TO_SERVER, ProtocolVersion.CLASSIC);
        assertInstanceOf(SetBlockClientPacket.class, decoded);
        SetBlockClientPacket sb = (SetBlockClientPacket) decoded;
        assertEquals(10, sb.getX());
        assertEquals(20, sb.getY());
        assertEquals(30, sb.getZ());
        assertEquals(1, sb.getMode());
        assertEquals(5, sb.getBlockType());
    }

    @Test
    void playerIdentificationSurvivesCodecRoundTrip() {
        PlayerIdentificationPacket original = new PlayerIdentificationPacket(7, "TestUser", "verkey");
        Packet decoded = encodeThenDecode(original, PacketDirection.CLIENT_TO_SERVER, ProtocolVersion.CLASSIC);
        assertInstanceOf(PlayerIdentificationPacket.class, decoded);
        PlayerIdentificationPacket pid = (PlayerIdentificationPacket) decoded;
        assertEquals(7, pid.getProtocolVersion());
        assertEquals("TestUser", pid.getUsername().trim());
    }

    @Test
    void serverIdentificationSurvivesCodecRoundTrip() {
        ServerIdentificationPacket original = new ServerIdentificationPacket(
                7, "TestServer", "Welcome!", ServerIdentificationPacket.USER_TYPE_NORMAL);
        Packet decoded = encodeThenDecode(original, PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.CLASSIC);
        assertInstanceOf(ServerIdentificationPacket.class, decoded);
        ServerIdentificationPacket sid = (ServerIdentificationPacket) decoded;
        assertEquals(7, sid.getProtocolVersion());
        assertEquals("TestServer", sid.getServerName().trim());
        assertEquals("Welcome!", sid.getServerMotd().trim());
    }

    @Test
    void multiplePacketsDecodedSequentially() {
        // Encode two packets into a single buffer
        EmbeddedChannel encoder = outboundChannel();
        assertTrue(encoder.writeOutbound(new PingPacket()));
        assertTrue(encoder.writeOutbound(new MessagePacket((byte) 0, "Msg1")));

        ByteBuf buf1 = encoder.readOutbound();
        ByteBuf buf2 = encoder.readOutbound();

        ByteBuf combined = Unpooled.wrappedBuffer(buf1, buf2);

        // Decode both from the combined buffer
        EmbeddedChannel decoder = inboundChannel(PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.CLASSIC);
        assertTrue(decoder.writeInbound(combined));

        Packet first = decoder.readInbound();
        Packet second = decoder.readInbound();

        assertInstanceOf(PingPacket.class, first);
        assertInstanceOf(MessagePacket.class, second);
        assertEquals("Msg1", ((MessagePacket) second).getMessage().trim());

        encoder.finishAndReleaseAll();
        decoder.finishAndReleaseAll();
    }

    @Test
    void encodedPacketHasLengthPrefix() {
        EmbeddedChannel encoder = outboundChannel();
        assertTrue(encoder.writeOutbound(new PingPacket()));
        ByteBuf encoded = encoder.readOutbound();
        assertNotNull(encoded);

        // First 4 bytes = length prefix
        int length = encoded.readInt();
        assertEquals(encoded.readableBytes(), length);
        assertEquals(0x01, encoded.readByte()); // PingPacket ID

        encoded.release();
        encoder.finishAndReleaseAll();
    }

    @Test
    void incompletePacketWaitsForMoreData() {
        EmbeddedChannel decoder = inboundChannel(PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.CLASSIC);

        // Write only the length prefix, not the full packet
        ByteBuf partial = Unpooled.buffer();
        partial.writeInt(10); // Claim 10 bytes of payload
        partial.writeByte(0x01); // Only 1 byte of payload (need 10)

        assertFalse(decoder.writeInbound(partial)); // Not enough data → nothing decoded
        assertNull(decoder.readInbound());

        decoder.finishAndReleaseAll();
    }

    @Test
    void disconnectPacketPreservesReason() {
        com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket original =
                new com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket("Kicked!");
        Packet decoded = encodeThenDecode(original, PacketDirection.SERVER_TO_CLIENT, ProtocolVersion.CLASSIC);
        assertInstanceOf(com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket.class, decoded);
        assertEquals("Kicked!",
                ((com.github.martinambrus.rdforward.protocol.packet.classic.DisconnectPacket) decoded).getReason().trim());
    }
}
