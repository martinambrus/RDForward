package com.github.martinambrus.rdforward.server.hytale;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import com.github.martinambrus.rdforward.server.ServerWorld;
import com.github.martinambrus.rdforward.world.alpha.AlphaChunk;

/**
 * Wraps a Hytale QUIC session for integration with RDForward's broadcast system.
 *
 * Follows the same pattern as BedrockSessionWrapper / MCPESessionWrapper:
 * - ConnectedPlayer.sendPacket() calls sendViaNonTcp() which calls translateAndSend()
 * - Classic broadcast packets are translated to Hytale format and sent
 * - Chunk data is converted and sent on the Chunks QUIC stream
 */
public class HytaleSessionWrapper {

    private final HytaleSession session;
    private final ClassicToHytaleTranslator translator;
    private final HytaleChunkConverter chunkConverter;

    public HytaleSessionWrapper(HytaleSession session, ClassicToHytaleTranslator translator,
                                 HytaleChunkConverter chunkConverter) {
        this.session = session;
        this.translator = translator;
        this.chunkConverter = chunkConverter;
    }

    /**
     * Translate a Classic broadcast packet to Hytale format and send.
     * Called from ConnectedPlayer.sendViaNonTcp().
     */
    public void translateAndSend(Packet packet) {
        if (!session.isActive()) return;

        HytalePacketBuffer[] translated = translator.translate(
                packet, session.getDefaultStream().alloc());
        if (translated != null) {
            for (HytalePacketBuffer pkt : translated) {
                session.sendPacket(pkt);
            }
        }
    }

    /**
     * Send chunk data to the Hytale client.
     * Converts internal AlphaChunk to Hytale SetChunk format.
     */
    public void sendChunkData(ServerWorld world, AlphaChunk chunk) {
        if (!session.isActive()) return;

        // Hytale chunks are 32x32x32. An AlphaChunk is 16x128x16.
        // Convert to multiple Hytale super-chunks.
        // For a 16x128x16 MC chunk column, we need:
        //   X: 1 super-chunk (16 < 32, fits in one)
        //   Y: 4 super-chunks (128 / 32 = 4)
        //   Z: 1 super-chunk (16 < 32, fits in one)
        // But since Hytale super-chunks are 32x32x32 and MC chunks are 16x16,
        // we'd need to combine adjacent MC chunks. For now, send each MC chunk
        // as a partial super-chunk (only 16x16 populated, rest is air).

        int chunkX = chunk.getXPos();
        int chunkZ = chunk.getZPos();
        int superChunkX = chunkX >> 1; // 2 MC chunks per super-chunk in X
        int superChunkZ = chunkZ >> 1;
        int maxY = (AlphaChunk.HEIGHT + 31) >> 5;

        for (int cy = 0; cy < maxY; cy++) {
            HytalePacketBuffer pkt = chunkConverter.convertChunk(
                    world, superChunkX, cy, superChunkZ,
                    session.getDefaultStream().alloc());
            if (pkt != null) {
                session.sendPacket(pkt);
            }
        }
    }

    /** Disconnect the Hytale client with a reason message. */
    public void disconnect(String reason) {
        session.disconnect(reason);
    }

    public HytaleSession getSession() { return session; }
}
