package com.github.martinambrus.rdforward.api.event.server;

import com.github.martinambrus.rdforward.api.event.Event;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.PrioritizedEvent;

/**
 * Registry of all server-side events that mods can listen to.
 *
 * <p>Each event is a static final {@link Event} instance. Mods register
 * callbacks via {@code Event.register()} and the server fires them via
 * {@code Event.invoker()}. Cancellable events ({@link #BLOCK_BREAK},
 * {@link #BLOCK_PLACE}, {@link #CHAT}) use {@link PrioritizedEvent} so mods
 * can override each other with explicit priorities; non-cancellable events
 * use plain {@link Event}.
 *
 * <p>Dispatch stops on the first non-PASS result from LOWEST..HIGHEST
 * listeners. MONITOR listeners always run and their return values are
 * ignored.
 *
 * <pre>
 *   ServerEvents.PLAYER_JOIN.register((name, version) -&gt; {
 *       System.out.println(name + " joined!");
 *   });
 * </pre>
 */
public final class ServerEvents {

    private ServerEvents() {}

    /** Fired before a block is broken. Cancellable. */
    public static final PrioritizedEvent<BlockBreakCallback> BLOCK_BREAK = PrioritizedEvent.create(
            (player, x, y, z, blockType) -> EventResult.PASS,
            (dispatch, monitor) -> (player, x, y, z, blockType) -> {
                EventResult outcome = EventResult.PASS;
                for (BlockBreakCallback l : dispatch) {
                    EventResult r = l.onBlockBreak(player, x, y, z, blockType);
                    if (r != EventResult.PASS) { outcome = r; break; }
                }
                for (BlockBreakCallback l : monitor) l.onBlockBreak(player, x, y, z, blockType);
                return outcome;
            }
    );

    /** Fired before a block is placed. Cancellable. */
    public static final PrioritizedEvent<BlockPlaceCallback> BLOCK_PLACE = PrioritizedEvent.create(
            (player, x, y, z, newBlockType) -> EventResult.PASS,
            (dispatch, monitor) -> (player, x, y, z, newBlockType) -> {
                EventResult outcome = EventResult.PASS;
                for (BlockPlaceCallback l : dispatch) {
                    EventResult r = l.onBlockPlace(player, x, y, z, newBlockType);
                    if (r != EventResult.PASS) { outcome = r; break; }
                }
                for (BlockPlaceCallback l : monitor) l.onBlockPlace(player, x, y, z, newBlockType);
                return outcome;
            }
    );

    /** Fired when a chat message is received. Cancellable. */
    public static final PrioritizedEvent<ChatCallback> CHAT = PrioritizedEvent.create(
            (player, message) -> EventResult.PASS,
            (dispatch, monitor) -> (player, message) -> {
                EventResult outcome = EventResult.PASS;
                for (ChatCallback l : dispatch) {
                    EventResult r = l.onChat(player, message);
                    if (r != EventResult.PASS) { outcome = r; break; }
                }
                for (ChatCallback l : monitor) l.onChat(player, message);
                return outcome;
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

    /** Fired once after the server has finished initializing. */
    public static final Event<ServerStartedCallback> SERVER_STARTED = Event.create(
            () -> {},
            listeners -> () -> {
                for (ServerStartedCallback l : listeners) l.onServerStarted();
            }
    );

    /** Fired once when the server begins shutting down. */
    public static final Event<ServerStoppingCallback> SERVER_STOPPING = Event.create(
            () -> {},
            listeners -> () -> {
                for (ServerStoppingCallback l : listeners) l.onServerStopping();
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

    /**
     * Clear all listeners from every event. Called during server shutdown
     * to prevent stale listeners from accumulating across restarts in the
     * same JVM (e.g. test suites).
     */
    public static void clearAll() {
        BLOCK_BREAK.clearListeners();
        BLOCK_PLACE.clearListeners();
        PLAYER_JOIN.clearListeners();
        PLAYER_LEAVE.clearListeners();
        PLAYER_MOVE.clearListeners();
        CHAT.clearListeners();
        SERVER_STARTED.clearListeners();
        SERVER_STOPPING.clearListeners();
        SERVER_TICK.clearListeners();
        WORLD_SAVE.clearListeners();
    }
}
