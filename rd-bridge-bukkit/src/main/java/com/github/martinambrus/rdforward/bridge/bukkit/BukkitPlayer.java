// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.FieldValue;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.isAbstract;

/**
 * Factory for runtime-generated Bukkit {@link Player} instances.
 *
 * <p>Real Paper's {@code Player} is an interface with hundreds of
 * abstract methods that plugins call via {@code invokeinterface}; a
 * concrete class can't stand in (the JVM throws
 * {@link IncompatibleClassChangeError}). At the same time, plugins
 * (notably LuckPerms) reflectively read and write the
 * {@code perm} field of the CraftBukkit superclass
 * {@code org.bukkit.craftbukkit.entity.CraftHumanEntity} —
 * {@link java.lang.reflect.Field#get} requires the receiver to be an
 * instance of the field's declaring class, so a JDK {@link
 * java.lang.reflect.Proxy} (which can only implement interfaces, not
 * extend a class) fails with {@link IllegalArgumentException} on every
 * use.
 *
 * <p>This factory mints, at class-load time, a single ByteBuddy-generated
 * class that:
 * <ul>
 *   <li>extends {@link CraftHumanEntity} so the {@code perm} field is a
 *       real inherited instance field that survives reflective {@code
 *       Field.get/set};</li>
 *   <li>implements {@link Player} (and transitively {@code HumanEntity},
 *       {@code LivingEntity}, {@code Entity}, {@code OfflinePlayer},
 *       {@code CommandSender}, …) so {@code invokeinterface} dispatch
 *       on every Player API method has a real method to land on;</li>
 *   <li>delegates every abstract method to {@link Handler#invoke},
 *       reusing the prior Proxy dispatch logic.</li>
 * </ul>
 * Each player instance carries a {@link Handler} in a public {@code
 * handler} field. The factory constructs new instances per session.
 */
public final class BukkitPlayer {

    /** Generated class — extends {@link CraftHumanEntity} and implements
     *  {@link Player}. Loaded once per JVM. */
    private static final Class<? extends CraftHumanEntity> GENERATED_CLASS;

    static {
        try {
            GENERATED_CLASS = new ByteBuddy()
                    .subclass(CraftHumanEntity.class)
                    .implement(Player.class)
                    .name("com.github.martinambrus.rdforward.bridge.bukkit.GeneratedPlayer$ByteBuddy")
                    .defineField("handler", Handler.class, Visibility.PUBLIC)
                    .defineConstructor(Visibility.PUBLIC)
                        .withParameters(Handler.class)
                        .intercept(MethodCall.invoke(CraftHumanEntity.class.getDeclaredConstructor())
                                .andThen(FieldAccessor.ofField("handler").setsArgumentAt(0)))
                    .method(isAbstract())
                        .intercept(MethodDelegation.to(Interceptor.class))
                    .make()
                    .load(BukkitPlayer.class.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded();
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private BukkitPlayer() {}

    /** Per-name cache of generated Player instances. LuckPerms (and other
     *  Permission plugins) reflectively rewrite {@code CraftHumanEntity.perm}
     *  on the instance Adventure registered via {@link
     *  org.bukkit.event.player.PlayerJoinEvent}. If every {@code
     *  Bukkit.getPlayer(name)} / {@code BukkitPlayerAdapter.wrap} call
     *  minted a fresh instance, the freshly-minted one would carry the
     *  default {@link org.bukkit.permissions.PermissibleBase} rather than
     *  LP's injected {@code LuckPermsPermissible} — and {@code
     *  player.hasPermission("worldedit.region.set")} would silently fall
     *  back to the op-flag fallback, denying every command for non-op
     *  players in LP groups. Caching by lower-cased name (case-insensitive
     *  to match Bukkit's lookup semantics) means PJE, /we sender lookup,
     *  /lp output target, and {@code Bukkit.getOnlinePlayers()} all share
     *  the same instance, so LP's injection is visible everywhere. */
    private static final ConcurrentHashMap<String, Player> CACHE = new ConcurrentHashMap<>();

    /** Mirrors {@code rd-server PlayerManager.PLAYER_EYE_HEIGHT}. The
     *  rd-api {@code Location} stores Y at eye-level (feet + 1.62) but
     *  Bukkit's {@code Player.getLocation()} returns feet; subtract
     *  this when handing the location out through the bridge. */
    private static final double PLAYER_EYE_HEIGHT = (double) 1.62f;

    /** Classic yaw (0 = North) -> Bukkit yaw (0 = South). WorldEdit's
     *  {@code //hpos1 / //hpos2} ray-trace uses the Bukkit look vector
     *  computed from this yaw — without the +180 conversion the trace
     *  walks behind the player instead of along their line of sight. */
    private static float classicYawToBukkit(float classicYaw) {
        float v = (classicYaw + 180.0f) % 360.0f;
        return v < 0.0f ? v + 360.0f : v;
    }

    /** No-op {@link org.bukkit.inventory.PlayerInventory} stub. Real
     *  Bukkit's {@code Player.getInventory()} never returns null, so
     *  every plugin assumes a non-null inventory — WE 5.6.1's
     *  {@code BukkitPlayer.giveItem} is the canonical example, NPEing
     *  immediately on {@code //wand} otherwise. RDForward has no
     *  item-inventory model (block placements are tracked separately
     *  via {@code PlayerInventoryPacket}/cobblestone counters), so
     *  every method on this proxy returns a safe default: empty
     *  collections / arrays for queries, no-op for mutators. The
     *  WE wand will not actually appear in the hotbar, but the command
     *  no longer crashes — operators should use {@code //hpos1} /
     *  {@code //hpos2} or the chat-command {@code //pos1} / {@code //pos2}
     *  which work end-to-end. */
    private static final org.bukkit.inventory.PlayerInventory STUB_INVENTORY =
            (org.bukkit.inventory.PlayerInventory) Proxy.newProxyInstance(
                    BukkitPlayer.class.getClassLoader(),
                    new Class<?>[] { org.bukkit.inventory.PlayerInventory.class },
                    (proxy, method, args) -> {
                        Class<?> rt = method.getReturnType();
                        if (rt == java.util.HashMap.class) return new HashMap<>();
                        if (rt == java.util.List.class) return Collections.emptyList();
                        if (rt == java.util.ListIterator.class) return Collections.<Object>emptyList().listIterator();
                        if (rt == org.bukkit.inventory.ItemStack[].class) return new org.bukkit.inventory.ItemStack[0];
                        return defaultValue(rt);
                    });

    public static Player create(String name) {
        // Resolve the live rd-api backing (and the bridge's default world)
        // so events fired by the host — PlayerJoinEvent, PlayerQuitEvent,
        // PlayerMoveEvent, BlockBreakEvent, AsyncPlayerChatEvent — carry a
        // Player whose isOnline()/getAddress()/getLocation() reflect the
        // real session. Without this, plugins that gate logic on
        // isOnline() (LoginSecurity's isInvalidPlayer check) skip the
        // join handler and never set their per-player state, leading to
        // null lookups later in the auth flow.
        com.github.martinambrus.rdforward.api.server.Server rd = BukkitBridge.currentRdServer();
        if (rd != null) {
            com.github.martinambrus.rdforward.api.player.Player backing = rd.getPlayer(name);
            if (backing != null) {
                return create(name, backing, BukkitBridge.defaultWorld());
            }
        }
        return create(name, null, null);
    }

    public static Player create(String name,
                                com.github.martinambrus.rdforward.api.player.Player backing,
                                World world) {
        if (name == null) return mint(null, backing, world);
        String key = name.toLowerCase(java.util.Locale.ROOT);
        Player cached = CACHE.get(key);
        if (cached != null) {
            // Refresh the mutable backing/world so reconnects (rd-api Player
            // identity changes per session) are still observed by the cached
            // proxy. The {@code perm} field that LP rewrote stays put.
            try {
                Field f = GENERATED_CLASS.getDeclaredField("handler");
                Handler h = (Handler) f.get(cached);
                if (h != null) {
                    if (backing != null) h.backing = backing;
                    if (world != null) h.world = world;
                }
            } catch (ReflectiveOperationException ignored) {}
            return cached;
        }
        Player fresh = mint(name, backing, world);
        Player race = CACHE.putIfAbsent(key, fresh);
        return race != null ? race : fresh;
    }

    /** Drop the cached proxy for {@code name}. Called from the quit
     *  hook so a player who logs in again gets a fresh perm slot rather
     *  than inheriting the previous session's LP-injected Permissible
     *  (which holds a stale rd-api Player reference). Also clears the
     *  visibility registry for this name (both as recipient and as
     *  hidden sender) so a reconnect starts with a clean visibility
     *  state — matches Bukkit's per-session semantics. */
    public static void evict(String name) {
        if (name != null) {
            CACHE.remove(name.toLowerCase(java.util.Locale.ROOT));
            PlayerVisibilityRegistry.clearRecipient(name);
            PlayerVisibilityRegistry.clearSender(name);
        }
    }

    private static Player mint(String name,
                               com.github.martinambrus.rdforward.api.player.Player backing,
                               World world) {
        Handler h = new Handler(name, backing, world);
        try {
            return (Player) GENERATED_CLASS
                    .getConstructor(Handler.class)
                    .newInstance(h);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to instantiate generated Player class", e);
        }
    }

    /** @return rd-api backing player, or {@code null} if {@code p} is
     *  not one of our generated instances. */
    public static com.github.martinambrus.rdforward.api.player.Player backing(Player p) {
        if (p == null || !GENERATED_CLASS.isInstance(p)) return null;
        try {
            Field f = GENERATED_CLASS.getDeclaredField("handler");
            Handler h = (Handler) f.get(p);
            return h == null ? null : h.backing;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /** Static method-delegation target. ByteBuddy emits a forwarding
     *  stub for every abstract Player method that calls into here. */
    public static final class Interceptor {

        private Interceptor() {}

        @RuntimeType
        public static Object intercept(@This Object self,
                                       @Origin Method m,
                                       @AllArguments Object[] args,
                                       @FieldValue("handler") Handler h) {
            return h.invoke(self, m, args);
        }
    }

    /** Per-player dispatch state. Mirrors the prior Proxy
     *  InvocationHandler — the decision tree is unchanged. */
    public static final class Handler {

        final String name;
        // Non-final so {@link BukkitPlayer#create} can refresh the backing
        // and world fields when a cached proxy is reused across sessions
        // (e.g. a player rejoining gets a new rd-api Player; the proxy
        // identity has to stay stable so LP's reflectively-injected
        // Permissible survives the reconnect, but the rd-api pointer
        // itself must update).
        volatile com.github.martinambrus.rdforward.api.player.Player backing;
        volatile World world;
        volatile UUID cachedUuid;
        final ConcurrentHashMap<String, Object> metadata = new ConcurrentHashMap<>();
        // Lazy in-memory PDC. Vanish (and similar "remember per-player
        // state across the join handler chain") plugins write here on
        // join and read on later events; a single container per proxy
        // (keyed by name via the BukkitPlayer cache) keeps the writes
        // visible across handlers without persisting across restarts.
        volatile StubPersistentDataContainer pdc;

        Handler(String name,
                com.github.martinambrus.rdforward.api.player.Player backing,
                World world) {
            this.name = name;
            this.backing = backing;
            this.world = world;
        }

        public Object invoke(Object self, Method m, Object[] args) {
            String n = m.getName();
            int argc = args == null ? 0 : args.length;

            // Identity / OfflinePlayer surface
            switch (n) {
                case "getName":
                case "getDisplayName":
                case "getPlayerListName":
                case "getCustomName":
                    return name;
                case "isOnline":
                case "isConnected":
                    return backing != null;
                case "getUniqueId":
                    if (cachedUuid == null) {
                        cachedUuid = UUID.nameUUIDFromBytes(
                                ("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
                    }
                    return cachedUuid;
                case "isOp":
                    return backing != null && backing.isOp();
                case "getAddress":
                    return backing == null ? null : backing.getAddress();
                case "isBanned":
                case "isWhitelisted":
                case "hasPlayedBefore":
                case "isInvulnerable":
                case "isPersistent":
                    return false;
                case "getPlayer":
                    return self;
                case "hasPermission":
                    return checkPermission(self, args);
                case "isPermissionSet":
                    return checkPermissionSet(self, args);
                case "getEffectivePermissions":
                    return getEffectivePermissionsFromInjected(self);
                case "recalculatePermissions":
                    forwardToInjectedPermissible(self, "recalculatePermissions");
                    return null;
            }

            // World / Location / movement
            switch (n) {
                case "getWorld":
                    return world;
                case "getLocation": {
                    if (backing == null) return new Location(world, 0, 0, 0);
                    com.github.martinambrus.rdforward.api.world.Location loc = backing.getLocation();
                    if (loc == null) return new Location(world, 0, 0, 0);
                    // Y: eye-level -> feet (Bukkit convention).
                    // Yaw: Classic (0=North) -> Bukkit (0=South) = +180.
                    return new Location(world, loc.x(), loc.y() - PLAYER_EYE_HEIGHT, loc.z(),
                            classicYawToBukkit(loc.yaw()), loc.pitch());
                }
                case "getEyeLocation": {
                    if (backing == null) return new Location(world, 0, 0, 0);
                    com.github.martinambrus.rdforward.api.world.Location eye = backing.getLocation();
                    if (eye == null) return new Location(world, 0, 0, 0);
                    return new Location(world, eye.x(), eye.y(), eye.z(),
                            classicYawToBukkit(eye.yaw()), eye.pitch());
                }
                case "getEyeHeight":
                    return PLAYER_EYE_HEIGHT;
                case "teleport":
                    return doTeleport(args);
            }

            // Messaging / kicking
            switch (n) {
                case "sendMessage":
                case "sendRawMessage":
                    if (backing != null && argc > 0 && args[0] instanceof String s) backing.sendMessage(s);
                    return null;
                case "kickPlayer":
                case "kick":
                    if (backing != null && argc > 0 && args[0] instanceof String s) backing.kick(s);
                    return null;
            }

            // Metadata (Metadatable)
            switch (n) {
                case "hasMetadata":
                    return argc > 0 && metadata.containsKey(String.valueOf(args[0]));
                case "setMetadata":
                    if (argc >= 2) metadata.put(String.valueOf(args[0]), args[1]);
                    return null;
                case "getMetadata":
                    if (argc == 0) return Collections.emptyList();
                    Object v = metadata.get(String.valueOf(args[0]));
                    return v == null ? Collections.emptyList() : Collections.singletonList(v);
                case "removeMetadata":
                    if (argc > 0) metadata.remove(String.valueOf(args[0]));
                    return null;
            }

            // Health / stats — safe defaults
            switch (n) {
                case "getHealth":
                case "getMaxHealth":
                    return 20.0;
                case "getFoodLevel":
                    return 20;
                case "getSaturation":
                    return 5.0f;
                case "getExp":
                case "getExhaustion":
                case "getTotalExperience":
                    return 0.0f;
                case "getLevel":
                    return 0;
                case "getInventory":
                    return STUB_INVENTORY;
                case "getGameMode":
                case "getEnderChest":
                case "getOpenInventory":
                    return null;
                case "getPersistentDataContainer": {
                    StubPersistentDataContainer existing = pdc;
                    if (existing != null) return existing;
                    synchronized (this) {
                        if (pdc == null) pdc = new StubPersistentDataContainer();
                        return pdc;
                    }
                }
            }

            // Player visibility — VanishNoPacket (and any plugin that
            // wraps player visibility without ProtocolLib) calls these
            // to hide a vanished player from each viewer. The bridge
            // updates the per-recipient registry that the installed
            // PlayerVisibilityFilter consults on every per-sender
            // broadcast, and ALSO sends an immediate despawn/spawn
            // packet so the target disappears mid-session rather than
            // waiting for the next chunk reload.
            switch (n) {
                case "hidePlayer":
                    return doHidePlayer(args);
                case "showPlayer":
                    return doShowPlayer(args);
                case "canSee":
                    return doCanSee(args);
                case "isListed":
                    return doCanSee(args);  // listed iff visible
                case "unlistPlayer":
                    doHidePlayer(args);
                    return Boolean.TRUE;
                case "listPlayer":
                    doShowPlayer(args);
                    return Boolean.TRUE;
                case "hideEntity":
                    // (Plugin, Entity) form — only Player entities are
                    // tracked in the visibility registry; non-Player
                    // entity hiding is not supported (no entity model).
                    if (argc >= 2 && args[1] instanceof Player p) {
                        doHidePlayer(new Object[] { p });
                    }
                    return null;
                case "showEntity":
                    if (argc >= 2 && args[1] instanceof Player p) {
                        doShowPlayer(new Object[] { p });
                    }
                    return null;
            }

            // Anything else returns a type-safe default so the abstract
            // method contract is satisfied without throwing.
            return defaultValue(m.getReturnType());
        }

        private boolean checkPermission(Object self, Object[] args) {
            if (args == null || args.length == 0) return false;
            String permName = extractPermName(args[0]);
            if (permName == null || permName.isEmpty()) return true;
            // If LuckPerms (or any other plugin) installed a Permissible
            // into CraftHumanEntity.perm via reflection, route the check
            // through it so the plugin's resolver — group inheritance,
            // contexts, time-limited grants — actually runs. Without
            // this, hasPermission would silently bypass LuckPerms and
            // return only flat default-permission values.
            org.bukkit.permissions.Permissible injected = readInjectedPermissible(self);
            if (injected != null) return injected.hasPermission(permName);
            // Fallback to RDForward's permission manager when no plugin
            // has installed a custom Permissible (or only the default
            // PermissibleBase, which always returns false in our stub).
            com.github.martinambrus.rdforward.api.server.Server rd = BukkitBridge.currentRdServer();
            if (rd != null && rd.getPermissionManager() != null) {
                return rd.getPermissionManager().hasPermission(name, permName);
            }
            return backing != null && backing.isOp();
        }

        /** Route {@code Permissible.isPermissionSet} to the LP-injected
         *  Permissible. WorldEdit's WEPIF DinnerPermsResolver checks
         *  {@code isPermissionSet} BEFORE {@code hasPermission} — if
         *  isPermissionSet returns false, the resolver short-circuits to
         *  the registered-permission default lookup and returns 0 (deny)
         *  even when LP would grant via wildcard. So a hard-coded
         *  {@code false} here is what's been blocking //set despite LP
         *  having {@code worldedit.*} on the player's group. Route it
         *  through LP so the per-node check sees the same group/wildcard
         *  resolution as hasPermission. */
        private boolean checkPermissionSet(Object self, Object[] args) {
            if (args == null || args.length == 0) return false;
            String permName = extractPermName(args[0]);
            if (permName == null || permName.isEmpty()) return false;
            org.bukkit.permissions.Permissible injected = readInjectedPermissible(self);
            if (injected != null) return injected.isPermissionSet(permName);
            // No injected permissible — defer to op flag so console-style
            // operators still satisfy resolvers that rely on
            // isPermissionSet for op-default bypasses.
            return backing != null && backing.isOp();
        }

        /** Forward {@code getEffectivePermissions} to the LP-injected
         *  Permissible. WEPIF's DinnerPermsResolver iterates this set to
         *  enumerate {@code group.X} entries when answering
         *  {@code getGroups(player)}. */
        private java.util.Set<org.bukkit.permissions.PermissionAttachmentInfo>
                getEffectivePermissionsFromInjected(Object self) {
            org.bukkit.permissions.Permissible injected = readInjectedPermissible(self);
            if (injected != null) return injected.getEffectivePermissions();
            return java.util.Collections.emptySet();
        }

        private void forwardToInjectedPermissible(Object self, String methodName) {
            org.bukkit.permissions.Permissible injected = readInjectedPermissible(self);
            if (injected == null) return;
            try {
                injected.getClass().getMethod(methodName).invoke(injected);
            } catch (ReflectiveOperationException ignored) {}
        }

        /** @return LP-injected (or otherwise non-default)
         *  {@link org.bukkit.permissions.Permissible} stored in the
         *  inherited {@code CraftHumanEntity.perm} field, or {@code null}
         *  if the field is unset / still the {@link PermissibleBase}
         *  default that returns false for everything. */
        private org.bukkit.permissions.Permissible readInjectedPermissible(Object self) {
            try {
                java.lang.reflect.Field permField =
                        org.bukkit.craftbukkit.entity.CraftHumanEntity.class.getDeclaredField("perm");
                permField.setAccessible(true);
                Object perm = permField.get(self);
                if (perm instanceof org.bukkit.permissions.Permissible permissible
                        && perm.getClass() != org.bukkit.permissions.PermissibleBase.class) {
                    return permissible;
                }
            } catch (ReflectiveOperationException ignored) {}
            return null;
        }

        private static String extractPermName(Object a) {
            if (a instanceof String s) return s;
            if (a instanceof org.bukkit.permissions.Permission p) return p.getName();
            return null;
        }

        private Object doTeleport(Object[] args) {
            if (backing == null || args == null || args.length == 0 || !(args[0] instanceof Location loc)) {
                return false;
            }
            backing.teleport(new com.github.martinambrus.rdforward.api.world.Location(
                    loc.getWorld() == null ? null : loc.getWorld().getName(),
                    loc.getX(), loc.getY(), loc.getZ(),
                    loc.getYaw(), loc.getPitch()));
            return true;
        }

        /** Resolve the target player's name from {@code hidePlayer} /
         *  {@code showPlayer} args. Both take a single Player; the
         *  (Plugin, Player) overloads are default methods on the Player
         *  interface that we intercept separately. */
        private static String targetName(Object[] args) {
            if (args == null || args.length == 0) return null;
            Object first = args[0];
            if (first instanceof Player p) return p.getName();
            return null;
        }

        private Object doHidePlayer(Object[] args) {
            String target = targetName(args);
            if (name == null || target == null) return null;
            PlayerVisibilityRegistry.hide(name, target);
            com.github.martinambrus.rdforward.api.server.Server rd = BukkitBridge.currentRdServer();
            if (rd != null) {
                // Order matters: tab-list REMOVE first (so the entry is
                // gone before clients render it again on the next tick),
                // then despawn so the entity disappears in-world.
                rd.sendPlayerListRemoveTo(name, target);
                rd.sendPlayerDespawnTo(name, target);
            }
            return null;
        }

        private Object doShowPlayer(Object[] args) {
            String target = targetName(args);
            if (name == null || target == null) return null;
            boolean wasHidden = PlayerVisibilityRegistry.show(name, target);
            // Only resend if the target was actually hidden — sending
            // unconditionally would duplicate the entity / entry for
            // clients that already had it.
            if (wasHidden) {
                com.github.martinambrus.rdforward.api.server.Server rd = BukkitBridge.currentRdServer();
                if (rd != null) {
                    // Tab-list ADD first, then spawn, mirroring the join
                    // sequence: clients on 1.8+ resolve player names from
                    // tab list by UUID and silently drop a SpawnPlayer
                    // that arrives before the matching list entry.
                    rd.sendPlayerListAddTo(name, target);
                    rd.sendPlayerSpawnTo(name, target);
                }
            }
            return null;
        }

        private Object doCanSee(Object[] args) {
            String target = targetName(args);
            if (name == null || target == null) return Boolean.TRUE;
            return !PlayerVisibilityRegistry.isHidden(name, target);
        }
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == void.class) return null;
        if (returnType.isPrimitive()) {
            if (returnType == boolean.class) return Boolean.FALSE;
            if (returnType == byte.class) return (byte) 0;
            if (returnType == short.class) return (short) 0;
            if (returnType == int.class) return 0;
            if (returnType == long.class) return 0L;
            if (returnType == float.class) return 0f;
            if (returnType == double.class) return 0.0d;
            if (returnType == char.class) return '\0';
            return null;
        }
        // Real Bukkit's collection-returning methods never return null —
        // plugins iterate the result without a guard. VanishNoPacket's
        // toggleVanishQuiet calls Player.getNearbyEntities(...).iterator()
        // unconditionally; without a non-null List default the join
        // listener NPEs and the player skips Vanish's per-player state
        // setup. Same shape applies to getPassengers, getTrackedBy,
        // getScoreboardTags, etc. — return empty collections so plugin
        // iteration runs cleanly even when our stub has no real backing.
        if (returnType == java.util.List.class
                || returnType == java.util.Collection.class
                || returnType == java.lang.Iterable.class) return Collections.emptyList();
        if (returnType == java.util.Set.class) return Collections.emptySet();
        if (returnType == java.util.Map.class) return Collections.emptyMap();
        return null;
    }
}
