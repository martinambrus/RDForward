package com.github.martinambrus.rdforward.server;

import com.github.martinambrus.rdforward.protocol.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbound handler that accumulates writes between flushes and
 * reorders them by priority. This ensures that during heavy write
 * bursts (e.g. chunk loading + entity updates + keep-alive), critical
 * packets like keep-alive and teleport corrections are sent first.
 *
 * Priority levels (lower = higher priority):
 *   0 - Critical: KeepAlive, Disconnect, LoginSuccess
 *   1 - Correction: PlayerPosition/teleport (self), TeleportConfirm
 *   2 - Confirmation: BlockChangedAck, AcknowledgePlayerDigging, BlockChange
 *   3 - Entity: EntityRelativeMove, EntityTeleport, SpawnPlayer, DestroyEntities
 *   4 - Bulk: MapChunk, UpdateLight, UpdateTags
 *
 * Within the same priority level, packets are written in FIFO order.
 */
public class PrioritizingOutboundHandler extends ChannelOutboundHandlerAdapter {

    private static final int PRIORITY_CRITICAL = 0;
    private static final int PRIORITY_CORRECTION = 1;
    private static final int PRIORITY_CONFIRMATION = 2;
    private static final int PRIORITY_ENTITY = 3;
    private static final int PRIORITY_BULK = 4;
    private static final int PRIORITY_DEFAULT = 3;

    /** Cache classification result per packet class to avoid repeated getSimpleName() allocations. */
    private static final ConcurrentHashMap<Class<?>, Integer> classifyCache = new ConcurrentHashMap<>();

    private final List<PendingWrite> buffer = new ArrayList<>();
    private int sequence = 0;

    private static final class PendingWrite implements Comparable<PendingWrite> {
        final Object msg;
        final ChannelPromise promise;
        final int priority;
        final int order;

        PendingWrite(Object msg, ChannelPromise promise, int priority, int order) {
            this.msg = msg;
            this.promise = promise;
            this.priority = priority;
            this.order = order;
        }

        @Override
        public int compareTo(PendingWrite other) {
            int cmp = Integer.compare(this.priority, other.priority);
            return cmp != 0 ? cmp : Integer.compare(this.order, other.order);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        int priority = classify(msg);
        buffer.add(new PendingWrite(msg, promise, priority, sequence++));
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (buffer.isEmpty()) {
            ctx.flush();
            return;
        }

        if (buffer.size() > 1) {
            buffer.sort(null); // natural ordering via Comparable
        }

        for (PendingWrite pw : buffer) {
            ctx.write(pw.msg, pw.promise);
        }
        buffer.clear();
        ctx.flush();
    }

    private static int classify(Object msg) {
        if (!(msg instanceof Packet)) {
            return PRIORITY_DEFAULT;
        }
        return classifyCache.computeIfAbsent(msg.getClass(), PrioritizingOutboundHandler::classifyByName);
    }

    private static int classifyByName(Class<?> clazz) {
        String name = clazz.getSimpleName();

        // Critical: keep-alive, disconnect, login
        if (name.startsWith("KeepAlive")
                || name.startsWith("Disconnect") || name.startsWith("NettyDisconnect")
                || name.startsWith("LoginDisconnect") || name.startsWith("LoginSuccess")) {
            return PRIORITY_CRITICAL;
        }

        // Correction: player position/teleport for self
        if (name.startsWith("PlayerPositionS2C") || name.startsWith("PlayerTeleportS2C")
                || name.equals("TeleportConfirmPacketV109")
                || name.startsWith("NettyPlayerPosition")
                || name.startsWith("SynchronizePlayerPosition")) {
            return PRIORITY_CORRECTION;
        }

        // Confirmation: block change acks
        if (name.startsWith("BlockChangedAck") || name.startsWith("AcknowledgePlayerDigging")
                || name.startsWith("BlockChange") || name.startsWith("NettyBlockChange")
                || name.startsWith("SetBlockServer")) {
            return PRIORITY_CONFIRMATION;
        }

        // Bulk: chunks, light, tags
        if (name.startsWith("MapChunk") || name.startsWith("UpdateLight")
                || name.startsWith("UpdateTags") || name.startsWith("ChunkBatch")
                || name.startsWith("LevelDataChunk") || name.startsWith("LevelInitialize")
                || name.startsWith("LevelFinalize")) {
            return PRIORITY_BULK;
        }

        // Everything else: entity updates, chat, etc.
        return PRIORITY_DEFAULT;
    }
}
