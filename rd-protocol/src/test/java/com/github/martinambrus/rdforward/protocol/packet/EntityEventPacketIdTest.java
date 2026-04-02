package com.github.martinambrus.rdforward.protocol.packet;

import com.github.martinambrus.rdforward.protocol.packet.netty.NettyEntityEventPacket;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that NettyEntityEventPacket is registered at the correct S2C
 * packet ID for each Netty protocol version. This is a regression test
 * for commit 5768ff6 which fixed EntityEvent S2C IDs for 1.13/1.15/1.17.
 *
 * IDs shift between versions as packets are added/removed.
 */
class EntityEventPacketIdTest {

    /**
     * Verify the S2C packet ID for EntityEvent across all Netty versions.
     * The expected IDs are from the NettyPacketRegistry registrations.
     */
    @ParameterizedTest(name = "v{0} EntityEvent S2C ID = 0x{1}")
    @CsvSource({
            // protocol version, expected hex ID
            "47,   1A",  // 1.8    (v47 base)
            "109,  1B",  // 1.9.4  (v109 overlay)
            "393,  1C",  // 1.13   (v393 overlay) — fixed in 5768ff6
            "477,  1B",  // 1.14   (v477 overlay)
            "573,  1C",  // 1.15   (v573 overlay) — fixed in 5768ff6
            "735,  1B",  // 1.16   (v735 overlay)
            "751,  1A",  // 1.16.2 (v751 overlay)
            "755,  1B",  // 1.17   (v755 overlay) — fixed in 5768ff6
            "760,  1A",  // 1.19.1 (v760 overlay)
            "761,  19",  // 1.19.3 (v761 overlay)
            "762,  1C",  // 1.19.4 (v762 overlay)
            "764,  1D",  // 1.20.2 (v764 overlay)
            "766,  1F",  // 1.20.5 (v766 overlay)
            "768,  20",  // 1.21.2 (v768 overlay)
            "770,  1E",  // 1.21.5 (v770 overlay)
            "773,  22",  // 1.21.9 (v773 overlay)
    })
    void entityEventHasCorrectPacketId(int protocolVersion, String expectedHex) {
        int expectedId = Integer.parseInt(expectedHex, 16);
        int actualId = NettyPacketRegistry.getPacketId(
                ConnectionState.PLAY,
                PacketDirection.SERVER_TO_CLIENT,
                NettyEntityEventPacket.class,
                protocolVersion);
        assertEquals(expectedId, actualId,
                "EntityEvent S2C ID for protocol " + protocolVersion
                        + " should be 0x" + expectedHex
                        + " but was 0x" + Integer.toHexString(actualId));
    }

    @Test
    void entityEventPacketFieldsRoundTrip() {
        NettyEntityEventPacket packet = new NettyEntityEventPacket(42, 26);
        io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
        try {
            packet.write(buf);

            NettyEntityEventPacket read = new NettyEntityEventPacket();
            read.read(buf);

            // Verify via write/read cycle by writing again and comparing bytes
            io.netty.buffer.ByteBuf buf2 = io.netty.buffer.Unpooled.buffer();
            try {
                read.write(buf2);
                assertEquals(buf.resetReaderIndex().readInt(), buf2.resetReaderIndex().readInt(),
                        "Entity ID should round-trip");
                assertEquals(buf.readByte(), buf2.readByte(),
                        "Event ID should round-trip");
            } finally {
                buf2.release();
            }
        } finally {
            buf.release();
        }
    }

    @Test
    void opLevelFactoryCreatesCorrectEvent() {
        NettyEntityEventPacket packet = NettyEntityEventPacket.opLevel(99, 2);
        io.netty.buffer.ByteBuf buf = io.netty.buffer.Unpooled.buffer();
        try {
            packet.write(buf);
            assertEquals(99, buf.readInt(), "Entity ID");
            assertEquals(26, buf.readByte(), "Event ID should be OP_PERMISSION_BASE(24) + level(2)");
        } finally {
            buf.release();
        }
    }
}
