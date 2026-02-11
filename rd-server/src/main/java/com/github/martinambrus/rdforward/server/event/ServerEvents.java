package com.github.martinambrus.rdforward.server.event;

import com.github.martinambrus.rdforward.protocol.event.Event;
import com.github.martinambrus.rdforward.protocol.event.EventResult;

/**
 * Registry of all server-side events that mods can listen to.
 *
 * Events follow Fabric's pattern: each event is a static final {@link Event}
 * instance. Mods register callbacks via {@code Event.register()}, and the
 * server fires them via {@code Event.invoker()}.
 *
 * Example:
 * <pre>
 *   ServerEvents.PLAYER_JOIN.register((name, version) -> {
 *       System.out.println(name + " joined!");
 *   });
 * </pre>
 */
public final class ServerEvents {

    private ServerEvents() {}

    /** Fired before a block is broken. Cancellable. */
    public static final Event<BlockBreakCallback> BLOCK_BREAK = Event.create(
            (player, x, y, z, blockType) -> EventResult.PASS,
            listeners -> (player, x, y, z, blockType) -> {
                for (BlockBreakCallback l : listeners) {
                    EventResult result = l.onBlockBreak(player, x, y, z, blockType);
                    if (result != EventResult.PASS) return result;
                }
                return EventResult.PASS;
            }
    );

    /** Fired before a block is placed. Cancellable. */
    public static final Event<BlockPlaceCallback> BLOCK_PLACE = Event.create(
            (player, x, y, z, newBlockType) -> EventResult.PASS,
            listeners -> (player, x, y, z, newBlockType) -> {
                for (BlockPlaceCallback l : listeners) {
                    EventResult result = l.onBlockPlace(player, x, y, z, newBlockType);
                    if (result != EventResult.PASS) return result;
                }
                return EventResult.PASS;
            }
    );

    /** Fired when a player has finished logging in. */
    public static final Event<PlayerJoinCallback> PLAYER_JOIN = Event.create(
            (name, version) -> {},
            listeners -> (name, version) -> {
                for (PlayerJoinCallback l : listeners) l.onPlayerJoin(name, version);
            }
    );

    /** Fired when a player disconnects. */
    public static final Event<PlayerLeaveCallback> PLAYER_LEAVE = Event.create(
            name -> {},
            listeners -> name -> {
                for (PlayerLeaveCallback l : listeners) l.onPlayerLeave(name);
            }
    );

    /** Fired when a player sends a position update. */
    public static final Event<PlayerMoveCallback> PLAYER_MOVE = Event.create(
            (name, x, y, z, yaw, pitch) -> {},
            listeners -> (name, x, y, z, yaw, pitch) -> {
                for (PlayerMoveCallback l : listeners) l.onPlayerMove(name, x, y, z, yaw, pitch);
            }
    );

    /** Fired when a chat message is received. Cancellable. */
    public static final Event<ChatCallback> CHAT = Event.create(
            (player, message) -> EventResult.PASS,
            listeners -> (player, message) -> {
                for (ChatCallback l : listeners) {
                    EventResult result = l.onChat(player, message);
                    if (result != EventResult.PASS) return result;
                }
                return EventResult.PASS;
            }
    );

    /** Fired every server tick (20 TPS). */
    public static final Event<ServerTickCallback> SERVER_TICK = Event.create(
            tickCount -> {},
            listeners -> tickCount -> {
                for (ServerTickCallback l : listeners) l.onServerTick(tickCount);
            }
    );

    /** Fired before the world is saved. */
    public static final Event<WorldSaveCallback> WORLD_SAVE = Event.create(
            () -> {},
            listeners -> () -> {
                for (WorldSaveCallback l : listeners) l.onWorldSave();
            }
    );
}
