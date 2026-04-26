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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
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

    public static Player create(String name) {
        return create(name, null, null);
    }

    public static Player create(String name,
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
        final com.github.martinambrus.rdforward.api.player.Player backing;
        volatile World world;
        volatile UUID cachedUuid;
        final ConcurrentHashMap<String, Object> metadata = new ConcurrentHashMap<>();

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
                    return false;
            }

            // World / Location / movement
            switch (n) {
                case "getWorld":
                    return world;
                case "getLocation":
                    if (backing == null) return new Location(world, 0, 0, 0);
                    com.github.martinambrus.rdforward.api.world.Location loc = backing.getLocation();
                    if (loc == null) return new Location(world, 0, 0, 0);
                    return new Location(world, loc.x(), loc.y(), loc.z(), loc.yaw(), loc.pitch());
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
                case "getGameMode":
                case "getInventory":
                case "getEnderChest":
                case "getOpenInventory":
                    return null;
            }

            // Anything else returns a type-safe default so the abstract
            // method contract is satisfied without throwing.
            return defaultValue(m.getReturnType());
        }

        private boolean checkPermission(Object self, Object[] args) {
            if (args == null || args.length == 0) return false;
            String permName = null;
            Object a = args[0];
            if (a instanceof String s) permName = s;
            else if (a instanceof org.bukkit.permissions.Permission p) permName = p.getName();
            if (permName == null || permName.isEmpty()) return true;
            // If LuckPerms (or any other plugin) installed a Permissible
            // into CraftHumanEntity.perm via reflection, route the check
            // through it so the plugin's resolver — group inheritance,
            // contexts, time-limited grants — actually runs. Without
            // this, hasPermission would silently bypass LuckPerms and
            // return only flat default-permission values.
            try {
                java.lang.reflect.Field permField =
                        org.bukkit.craftbukkit.entity.CraftHumanEntity.class.getDeclaredField("perm");
                permField.setAccessible(true);
                Object perm = permField.get(self);
                if (perm instanceof org.bukkit.permissions.Permissible permissible
                        && perm.getClass() != org.bukkit.permissions.PermissibleBase.class) {
                    return permissible.hasPermission(permName);
                }
            } catch (ReflectiveOperationException ignored) {}
            // Fallback to RDForward's permission manager when no plugin
            // has installed a custom Permissible (or only the default
            // PermissibleBase, which always returns false in our stub).
            com.github.martinambrus.rdforward.api.server.Server rd = BukkitBridge.currentRdServer();
            if (rd != null && rd.getPermissionManager() != null) {
                return rd.getPermissionManager().hasPermission(name, permName);
            }
            return backing != null && backing.isOp();
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
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == void.class) return null;
        if (!returnType.isPrimitive()) return null;
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
}
