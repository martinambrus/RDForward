// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.BlockBreakCallback;
import com.github.martinambrus.rdforward.api.event.server.BlockPlaceCallback;
import com.github.martinambrus.rdforward.api.event.server.ChatCallback;
import com.github.martinambrus.rdforward.api.event.server.PlayerMoveCallback;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Reflectively scans a Bukkit {@link Listener} for {@code @EventHandler}
 * methods and registers matching adapters on RDForward's
 * {@link ServerEvents}. Event registration happens inside the mod's
 * {@code EventOwnership} scope so the listeners are swept on mod disable.
 *
 * <p>Supported mappings:
 * <ul>
 *   <li>{@link BlockBreakEvent}      -&gt; {@link ServerEvents#BLOCK_BREAK}</li>
 *   <li>{@link BlockPlaceEvent}      -&gt; {@link ServerEvents#BLOCK_PLACE}</li>
 *   <li>{@link AsyncPlayerChatEvent} -&gt; {@link ServerEvents#CHAT}</li>
 *   <li>{@link PlayerJoinEvent}      -&gt; {@link ServerEvents#PLAYER_JOIN}</li>
 *   <li>{@link PlayerQuitEvent}      -&gt; {@link ServerEvents#PLAYER_LEAVE}</li>
 *   <li>{@link PlayerMoveEvent}      -&gt; {@link ServerEvents#PLAYER_MOVE}</li>
 * </ul>
 *
 * <p>Bukkit's {@code ignoreCancelled=false} (Spigot default) is intentionally
 * NOT honored: RDForward stops event dispatch on the first cancelling
 * listener, so later listeners never see cancelled events. When a plugin
 * registers a cancellable-event handler without {@code ignoreCancelled=true},
 * a one-time warning is logged per plugin name (see plan §4.4).
 */
public final class BukkitEventAdapter {

    private static final Logger LOG = Logger.getLogger("RDForward/BukkitBridge");

    /** Plugin names already warned about {@code ignoreCancelled=false}. */
    private static final Set<String> warnedPlugins = ConcurrentHashMap.newKeySet();

    /** Per-event-class binding entry — captures everything needed to
     *  dispatch a plugin-fired event back to either an
     *  {@code @EventHandler}-annotated {@link Method} or an
     *  {@link org.bukkit.plugin.EventExecutor} registered via the
     *  {@code PluginManager.registerEvent} executor form
     *  (adventure-platform-bukkit takes that path, no annotations). Exactly
     *  one of {@code method} / {@code executor} is non-null. */
    private record Bound(Listener listener, Method method,
                         org.bukkit.plugin.EventExecutor executor,
                         org.bukkit.event.EventPriority priority,
                         boolean ignoreCancelled) {}

    /** Listeners by exact event class declared on the {@code @EventHandler}
     *  parameter. Populated for every annotated method, regardless of
     *  whether the event type also has a matching ServerEvents callback —
     *  so plugin-fired custom events (LoginSecurity's
     *  {@code AuthActionEvent}, EssentialsX's per-command events, etc.)
     *  can be dispatched via {@link #dispatchPluginEvent}. */
    private static final ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Bound>> DIRECT =
            new ConcurrentHashMap<>();

    private BukkitEventAdapter() {}

    /** Walk a listener's declared methods and wire every {@code @EventHandler} to ServerEvents. */
    public static void register(Listener listener) {
        register(listener, (String) null);
    }

    /**
     * Walk a listener's declared methods and wire every {@code @EventHandler}
     * to ServerEvents. {@code pluginName} is used to tag the one-time
     * {@code ignoreCancelled=false} warning; may be null for test harnesses.
     */
    public static void register(Listener listener, String pluginName) {
        for (Method m : listener.getClass().getDeclaredMethods()) {
            EventHandler eh = m.getAnnotation(EventHandler.class);
            if (eh == null) continue;
            Class<?>[] params = m.getParameterTypes();
            if (params.length != 1) continue;
            m.setAccessible(true);

            Class<?> evtType = params[0];
            EventPriority prio = mapPriority(eh.priority());

            // Always remember the (event-class -> listener+method) binding
            // so plugin-fired events reach their handlers via
            // dispatchPluginEvent — independent of the ServerEvents-driven
            // path that translates real server actions into Bukkit events.
            DIRECT.computeIfAbsent(evtType, k -> new CopyOnWriteArrayList<>())
                    .add(new Bound(listener, m, null, eh.priority(), eh.ignoreCancelled()));

            if (isCancellable(evtType) && !eh.ignoreCancelled() && prio != EventPriority.MONITOR) {
                maybeWarnIgnoreCancelled(pluginName, listener);
            }

            if (evtType == BlockBreakEvent.class) {
                bindBlockBreak(listener, m, prio);
            } else if (evtType == BlockPlaceEvent.class) {
                bindBlockPlace(listener, m, prio);
            } else if (evtType == AsyncPlayerChatEvent.class
                    || evtType == org.bukkit.event.player.PlayerChatEvent.class) {
                bindChat(listener, m, prio, evtType);
            } else if (evtType == PlayerJoinEvent.class
                    || evtType == AsyncPlayerPreLoginEvent.class
                    || evtType == PlayerLoginEvent.class) {
                ensurePlayerJoinInstalled();
            } else if (evtType == PlayerQuitEvent.class) {
                ensurePlayerQuitInstalled();
            } else if (evtType == PlayerMoveEvent.class) {
                bindPlayerMove(listener, m);
            } else if (evtType == org.bukkit.event.server.ServerListPingEvent.class) {
                ensureServerListPingInstalled();
            }
        }
    }

    /** Reset the warned-plugins set. Test-only. */
    public static void resetWarnedPlugins() {
        warnedPlugins.clear();
    }

    /**
     * Dispatch a plugin-fired Bukkit event to every {@link Listener}
     * whose {@code @EventHandler} parameter type is assignable from
     * {@code event}'s runtime class. Listeners run in Bukkit priority
     * order ({@code LOWEST} → {@code MONITOR}); listeners marked
     * {@code ignoreCancelled = true} are skipped after another
     * listener cancels the event.
     *
     * <p>Real Bukkit dispatches via the event's static
     * {@code HandlerList}; RDForward registers handlers directly here,
     * so we do the type lookup ourselves. Used by plugins that fire
     * their own events through {@code PluginManager.callEvent} —
     * notably LoginSecurity's {@code AuthActionEvent} on registration
     * and login.
     */
    public static void dispatchPluginEvent(Event event) {
        if (event == null) return;
        Class<?> evtClass = event.getClass();
        List<Bound> matched = new ArrayList<>();
        for (Map.Entry<Class<?>, CopyOnWriteArrayList<Bound>> e : DIRECT.entrySet()) {
            if (e.getKey().isAssignableFrom(evtClass)) {
                matched.addAll(e.getValue());
            }
        }
        matched.sort(Comparator.comparingInt(b -> b.priority.ordinal()));
        for (Bound b : matched) {
            if (b.ignoreCancelled && event.isCancelled()) continue;
            if (b.method != null) {
                invokeListener(b.listener, b.method, event);
            } else if (b.executor != null) {
                invokeExecutor(b.executor, b.listener, event);
            }
        }
    }

    /** Test-only — clear every registered listener and dedup state so
     *  successive tests boot cleanly. The lazy single-installer guards
     *  for PLAYER_JOIN / PLAYER_QUIT must also be reset, otherwise a
     *  test that registered first leaves the AtomicBoolean=true and
     *  later tests skip the dispatcher registration entirely (a real
     *  CI failure: BukkitEventAdapterTest's join/quit assertions
     *  passed in isolation but failed under the full module run). */
    public static void clearAll() {
        DIRECT.clear();
        SEEN_LISTENER_ERRORS.clear();
        warnedPlugins.clear();
        PLAYER_JOIN_INSTALLED.set(false);
        PLAYER_QUIT_INSTALLED.set(false);
        SERVER_LIST_PING_INSTALLED.set(false);
        PIE_PRE_FIRE_INSTALLED.set(false);
    }

    private static boolean isCancellable(Class<?> evtType) {
        return evtType == BlockBreakEvent.class
                || evtType == BlockPlaceEvent.class
                || evtType == AsyncPlayerChatEvent.class
                || evtType == org.bukkit.event.player.PlayerChatEvent.class
                || evtType == PlayerMoveEvent.class;
    }

    private static void maybeWarnIgnoreCancelled(String pluginName, Listener listener) {
        String name = pluginName != null ? pluginName : listener.getClass().getName();
        if (warnedPlugins.add(name)) {
            LOG.warning(
                    "[BukkitBridge] Note: Plugin '" + name + "' registers event handlers without ignoreCancelled=true.\n"
                    + "In RDForward, cancelled events are NOT delivered to non-MONITOR listeners (unlike Spigot).\n"
                    + "If this plugin needs to see cancelled events, it should use MONITOR priority.");
        }
    }

    private static EventPriority mapPriority(org.bukkit.event.EventPriority p) {
        return switch (p) {
            case LOWEST -> EventPriority.LOWEST;
            case LOW -> EventPriority.LOW;
            case NORMAL -> EventPriority.NORMAL;
            case HIGH -> EventPriority.HIGH;
            case HIGHEST -> EventPriority.HIGHEST;
            case MONITOR -> EventPriority.MONITOR;
        };
    }

    private static void bindBlockBreak(Listener l, Method m, EventPriority prio) {
        ensurePlayerInteractPreFireInstalled();
        BlockBreakCallback cb = (name, x, y, z, blockType) -> {
            BlockBreakEvent ev = new BlockBreakEvent(BukkitPlayer.create(name), x, y, z, blockType);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_BREAK.register(prio, cb);
    }

    private static void bindBlockPlace(Listener l, Method m, EventPriority prio) {
        ensurePlayerInteractPreFireInstalled();
        BlockPlaceCallback cb = (name, x, y, z, newBlockType) -> {
            BlockPlaceEvent ev = new BlockPlaceEvent(BukkitPlayer.create(name), x, y, z, newBlockType);
            invokeListener(l, m, ev);
            return ev.isCancelled() ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.BLOCK_PLACE.register(prio, cb);
    }

    /** Install one-time LOWEST-priority callbacks on BLOCK_BREAK +
     *  BLOCK_PLACE that fire PlayerInteractEvent and return CANCEL if
     *  any PIE listener cancelled it. Must be at LOWEST so it runs
     *  before any per-listener Bukkit handler — and crucially before
     *  MONITOR-registered handlers (CoreProtect's BlockBreakListener
     *  and BlockPlaceListener), whose return values our PrioritizedEvent
     *  invoker discards by design. Without this hoisting, PIE
     *  cancellation set by inspector mode never reached the server's
     *  CANCEL-check at AlphaConnectionHandler.handleDigging /
     *  handleBlockPlacement and the world was mutated anyway. */
    private static final java.util.concurrent.atomic.AtomicBoolean PIE_PRE_FIRE_INSTALLED =
            new java.util.concurrent.atomic.AtomicBoolean(false);
    private static void ensurePlayerInteractPreFireInstalled() {
        if (!PIE_PRE_FIRE_INSTALLED.compareAndSet(false, true)) return;
        ServerEvents.BLOCK_BREAK.register(EventPriority.LOWEST,
                (name, x, y, z, blockType) -> {
                    org.bukkit.entity.Player player = BukkitPlayer.create(name);
                    boolean cancelled = firePlayerInteract(player, x, y, z, blockType,
                            org.bukkit.event.block.Action.LEFT_CLICK_BLOCK);
                    return cancelled ? EventResult.CANCEL : EventResult.PASS;
                });
        ServerEvents.BLOCK_PLACE.register(EventPriority.LOWEST,
                (name, x, y, z, newBlockType) -> {
                    org.bukkit.entity.Player player = BukkitPlayer.create(name);
                    boolean cancelled = firePlayerInteract(player, x, y, z, newBlockType,
                            org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK);
                    return cancelled ? EventResult.CANCEL : EventResult.PASS;
                });
    }

    /** Build a PlayerInteractEvent at the given coords and dispatch to
     *  every registered PIE listener via {@link #dispatchPluginEvent}.
     *  @return true iff the event was cancelled by any listener. */
    private static boolean firePlayerInteract(org.bukkit.entity.Player player,
                                              int x, int y, int z, int blockTypeId,
                                              org.bukkit.event.block.Action action) {
        org.bukkit.World world = player == null ? null : player.getWorld();
        org.bukkit.Material mat = MaterialMapper.fromApi(
                com.github.martinambrus.rdforward.api.world.BlockTypes.byId(blockTypeId));
        org.bukkit.block.Block block = new BukkitBlock(world, x, y, z, mat);
        org.bukkit.event.player.PlayerInteractEvent pie =
                new org.bukkit.event.player.PlayerInteractEvent(
                        player, action, null, block, org.bukkit.block.BlockFace.SELF);
        dispatchPluginEvent(pie);
        return pie.isCancelled();
    }

    private static void bindChat(Listener l, Method m, EventPriority prio, Class<?> evtType) {
        // Essentials 2.8.x's mute/format pipeline listens to the legacy
        // {@link org.bukkit.event.player.PlayerChatEvent}; modern plugins
        // listen to {@link AsyncPlayerChatEvent}. Fire whichever class
        // the listener registered for so cancellations from either
        // pipeline propagate back through {@code ServerEvents.CHAT}.
        //
        // For both event flavours we populate {@code getRecipients()} with
        // the live online roster so plugin pipelines (Essentials's
        // {@code /ignore}) can prune ignored players. After invoking the
        // listener we diff the surviving recipient set against the
        // original online set and forward the removed names — plus any
        // {@code event.setMessage(...)} rewrite — through
        // {@link com.github.martinambrus.rdforward.api.event.server.ChatContext}.
        boolean legacy = evtType == org.bukkit.event.player.PlayerChatEvent.class;
        ChatCallback cb = (name, message) -> {
            org.bukkit.entity.Player bukkitPlayer = BukkitPlayer.create(name);
            org.bukkit.Server server = org.bukkit.Bukkit.getServer();
            java.util.Set<org.bukkit.entity.Player> originalRecipients = new java.util.HashSet<>();
            if (server != null) originalRecipients.addAll(server.getOnlinePlayers());

            String finalMessage = message;
            boolean cancelled;
            java.util.Set<org.bukkit.entity.Player> survivingRecipients;
            if (legacy) {
                org.bukkit.event.player.PlayerChatEvent ev =
                        new org.bukkit.event.player.PlayerChatEvent(bukkitPlayer, message);
                ev.getRecipients().addAll(originalRecipients);
                invokeListener(l, m, ev);
                cancelled = ev.isCancelled();
                finalMessage = ev.getMessage();
                survivingRecipients = ev.getRecipients();
            } else {
                AsyncPlayerChatEvent ev = new AsyncPlayerChatEvent(bukkitPlayer, message);
                ev.getRecipients().addAll(originalRecipients);
                invokeListener(l, m, ev);
                cancelled = ev.isCancelled();
                finalMessage = ev.getMessage();
                survivingRecipients = ev.getRecipients();
            }
            com.github.martinambrus.rdforward.api.event.server.ChatContext ctx =
                    com.github.martinambrus.rdforward.api.event.server.ChatContext.current();
            if (ctx != null) {
                if (finalMessage != null) ctx.setMessage(finalMessage);
                for (org.bukkit.entity.Player p : originalRecipients) {
                    if (!survivingRecipients.contains(p)) {
                        ctx.excluded().add(p.getName());
                    }
                }
            }
            return cancelled ? EventResult.CANCEL : EventResult.PASS;
        };
        ServerEvents.CHAT.register(prio, cb);
    }

    /** Lazy single-installer guards. The composite PLAYER_JOIN dispatcher
     *  fires AsyncPlayerPreLoginEvent → PlayerLoginEvent → PlayerJoinEvent
     *  in sequence so plugins that listen for any of those events (LP for
     *  all three, adventure-platform for Join only) reliably observe the
     *  expected order. Without this ordering, LP's onPlayerLogin gates on
     *  a user record that onPlayerPreLogin would have created — if Join
     *  fires before PreLogin, every login is denied. */
    private static final java.util.concurrent.atomic.AtomicBoolean PLAYER_JOIN_INSTALLED =
            new java.util.concurrent.atomic.AtomicBoolean();
    private static final java.util.concurrent.atomic.AtomicBoolean PLAYER_QUIT_INSTALLED =
            new java.util.concurrent.atomic.AtomicBoolean();
    private static final java.util.concurrent.atomic.AtomicBoolean SERVER_LIST_PING_INSTALLED =
            new java.util.concurrent.atomic.AtomicBoolean();

    private static void ensurePlayerJoinInstalled() {
        if (!PLAYER_JOIN_INSTALLED.compareAndSet(false, true)) return;
        // Bridge dispatch fires on PLAYER_JOIN_ANNOUNCE (BEFORE the host
        // broadcasts) so plugins like VanishNoPacket that call
        // event.setJoinMessage("") inside the join handler can suppress
        // the broadcast. The handler returns the (possibly-rewritten)
        // joinMessage, which the rd-server PlayerManager uses as the
        // final announce string (or skips entirely on null/empty).
        ServerEvents.PLAYER_JOIN_ANNOUNCE.register((name, version, defaultMessage) -> {
            org.bukkit.entity.Player player = BukkitPlayer.create(name);
            java.util.UUID uuid = player == null ? null : player.getUniqueId();
            java.net.InetAddress addr = resolveAddressFor(player);

            AsyncPlayerPreLoginEvent preLogin = new AsyncPlayerPreLoginEvent(name, addr, uuid);
            dispatchPluginEvent(preLogin);
            if (preLogin.getLoginResult() != null
                    && preLogin.getLoginResult() != org.bukkit.event.player.AsyncPlayerPreLoginEvent$Result.ALLOWED) {
                return defaultMessage;
            }

            PlayerLoginEvent login = new PlayerLoginEvent(player, "", addr);
            dispatchPluginEvent(login);
            if (login.getResult() != null
                    && login.getResult() != org.bukkit.event.player.PlayerLoginEvent$Result.ALLOWED) {
                return defaultMessage;
            }

            PlayerJoinEvent join = new PlayerJoinEvent(player, defaultMessage);
            dispatchPluginEvent(join);
            return join.getJoinMessage();
        });
    }

    private static void ensurePlayerQuitInstalled() {
        if (!PLAYER_QUIT_INSTALLED.compareAndSet(false, true)) return;
        ServerEvents.PLAYER_LEAVE_ANNOUNCE.register((name, defaultMessage) -> {
            org.bukkit.entity.Player player = BukkitPlayer.create(name);
            PlayerQuitEvent quit = new PlayerQuitEvent(player, defaultMessage);
            dispatchPluginEvent(quit);
            String finalMessage = quit.getQuitMessage();
            // Drop the cached proxy AFTER plugins finish their quit
            // handling — LP needs the still-injected Permissible to
            // observe the disconnect — so the next login mints a fresh
            // perm slot rather than reusing one bound to a closed rd-api
            // session.
            BukkitPlayer.evict(name);
            return finalMessage;
        });
    }

    /** Lazy install for {@code SERVER_LIST_PING}. The handler wraps the
     *  rd-api {@link com.github.martinambrus.rdforward.api.event.server.ServerListPingHook.PingContext}
     *  in a Bukkit {@link org.bukkit.event.server.ServerListPingEvent}
     *  that shares the same mutable {@code playerNames} list — so when
     *  VanishNoPacket walks {@code event.iterator()} and removes vanished
     *  players, those removals propagate back into {@code ctx.playerNames}
     *  before the host serialises the pong. {@code setMotd}/{@code setMaxPlayers}
     *  are written back into the context after dispatch. */
    private static void ensureServerListPingInstalled() {
        if (!SERVER_LIST_PING_INSTALLED.compareAndSet(false, true)) return;
        com.github.martinambrus.rdforward.api.event.server.ServerEvents.SERVER_LIST_PING.register(ctx -> {
            org.bukkit.event.server.ServerListPingEvent event =
                    new org.bukkit.event.server.ServerListPingEvent(
                            ctx.address, ctx.playerNames, ctx.maxPlayers, ctx.motd);
            dispatchPluginEvent(event);
            ctx.maxPlayers = event.getMaxPlayers();
            ctx.motd = event.getMotd();
        });
    }

    private static java.net.InetAddress resolveAddressFor(org.bukkit.entity.Player player) {
        if (player == null) return null;
        try {
            java.net.InetSocketAddress sock = player.getAddress();
            return sock == null ? null : sock.getAddress();
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Wire an {@link org.bukkit.plugin.EventExecutor}-based registration
     *  (the form used by {@link org.bukkit.plugin.PluginManager#registerEvent})
     *  to a real ServerEvents callback. Plugins that go through the
     *  executor path skip the {@code @EventHandler}-scanning entry point —
     *  notably adventure-platform-bukkit, which registers anonymous
     *  listeners with executor-only dispatch to track player viewers.
     *  Without this wiring the executor is silently dropped and Adventure's
     *  {@code audiences.player(uuid)} returns the empty audience for every
     *  online player, so plugin output (LuckPerms /lp help, etc.) reaches
     *  no one in-game. */
    public static void registerExecutor(Class<?> evtType,
                                        org.bukkit.event.Listener listener,
                                        org.bukkit.event.EventPriority bukkitPriority,
                                        org.bukkit.plugin.EventExecutor executor,
                                        String pluginName,
                                        boolean ignoreCancelled) {
        if (evtType == null || executor == null || listener == null) return;
        org.bukkit.event.EventPriority prio = bukkitPriority == null
                ? org.bukkit.event.EventPriority.NORMAL : bukkitPriority;
        // Add a DIRECT entry so dispatchPluginEvent finds executor-bound
        // listeners alongside @EventHandler-annotated method bindings.
        DIRECT.computeIfAbsent(evtType, k -> new CopyOnWriteArrayList<>())
                .add(new Bound(listener, null, executor, prio, ignoreCancelled));
        // Ensure the corresponding ServerEvents callback is wired so the
        // event actually fires. The composite Join dispatcher covers
        // PreLogin/Login/Join in order; PlayerQuit has its own dispatcher.
        // Other event types remain on the per-listener bind* path which
        // ALSO routes through dispatchPluginEvent via the DIRECT map, so
        // executor-bound listeners for those types fire correctly even
        // though we don't install a separate ServerEvents callback here.
        if (evtType == PlayerJoinEvent.class
                || evtType == AsyncPlayerPreLoginEvent.class
                || evtType == PlayerLoginEvent.class) {
            ensurePlayerJoinInstalled();
        } else if (evtType == PlayerQuitEvent.class) {
            ensurePlayerQuitInstalled();
        } else if (evtType == org.bukkit.event.server.ServerListPingEvent.class) {
            ensureServerListPingInstalled();
        }
    }

    private static void invokeExecutor(org.bukkit.plugin.EventExecutor executor,
                                       org.bukkit.event.Listener listener,
                                       org.bukkit.event.Event event) {
        try {
            executor.execute(listener, event);
        } catch (Throwable t) {
            String key = listener.getClass().getName() + "#executor:"
                    + t.getClass().getName() + ":"
                    + (t.getMessage() == null ? "" : t.getMessage());
            if (SEEN_LISTENER_ERRORS.putIfAbsent(key, Boolean.TRUE) == null) {
                System.err.println("[Bukkit] Executor for " + listener.getClass().getName()
                        + " threw " + t.getClass().getName()
                        + (t.getMessage() == null ? "" : ": " + t.getMessage())
                        + " (further occurrences silenced)");
                t.printStackTrace(System.err);
            }
        }
    }

    private static void bindPlayerMove(Listener l, Method m) {
        PlayerMoveCallback cb = (name, x, y, z, yaw, pitch) -> {
            double dx = x / 32.0;
            double dy = y / 32.0;
            double dz = z / 32.0;
            float fyaw = (yaw & 0xFF) * 360f / 256f;
            float fpitch = pitch * 360f / 256f;
            Location loc = new Location(null, dx, dy, dz, fyaw, fpitch);
            PlayerMoveEvent ev = new PlayerMoveEvent(BukkitPlayer.create(name), loc, loc);
            invokeListener(l, m, ev);
        };
        ServerEvents.PLAYER_MOVE.register(cb);
    }

    /** Dedup map for listener-throw warnings. Keys are
     *  {@code listenerClass#method:exceptionClass:message} so the same
     *  class+message combination only logs the first time it surfaces.
     *  Real Bukkit prints the full stack on every event-listener failure;
     *  RDForward's auto-generated stubs surface predictable per-player
     *  exceptions (e.g. LuckPerms's reflective {@code Field.get} on the
     *  Player Proxy) that would otherwise flood the log on every join. */
    private static final java.util.concurrent.ConcurrentHashMap<String, Boolean> SEEN_LISTENER_ERRORS =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static void invokeListener(Listener l, Method m, Object event) {
        try {
            m.invoke(l, event);
        } catch (InvocationTargetException ite) {
            // Real Bukkit logs listener failures and continues — one bad
            // plugin must not abort the calling event chain or the
            // connection that triggered it. Surface the full stack so
            // missing stub APIs are visible, but dedup per
            // (listener-method, cause-class, cause-message) so a
            // repeating per-player failure floods the log only once.
            Throwable cause = ite.getTargetException();
            String key = l.getClass().getName() + "#" + m.getName() + ":"
                    + cause.getClass().getName() + ":"
                    + (cause.getMessage() == null ? "" : cause.getMessage());
            if (SEEN_LISTENER_ERRORS.putIfAbsent(key, Boolean.TRUE) == null) {
                System.err.println("[Bukkit] Listener " + l.getClass().getName() + "."
                        + m.getName() + " threw " + cause.getClass().getName()
                        + (cause.getMessage() == null ? "" : ": " + cause.getMessage())
                        + " (further occurrences silenced)");
                cause.printStackTrace(System.err);
            }
        } catch (IllegalAccessException iae) {
            String key = l.getClass().getName() + "#" + m.getName() + ":illegal-access";
            if (SEEN_LISTENER_ERRORS.putIfAbsent(key, Boolean.TRUE) == null) {
                System.err.println("[Bukkit] Cannot invoke " + l.getClass().getName() + "."
                        + m.getName() + ": " + iae.getMessage()
                        + " (further occurrences silenced)");
                iae.printStackTrace(System.err);
            }
        }
    }
}
