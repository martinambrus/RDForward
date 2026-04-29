// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Dynamic-proxy {@link Entity} stub. Returned from
 * {@code World.spawnEntity}/{@code World.spawn} so plugins that chain
 * {@code .getUniqueId()} on the result (VanishNoPacket spawns 10 fake
 * bats per /vanish toggle and stores their UUIDs for later cleanup)
 * don't NPE.
 *
 * <p>Honoured methods: {@code getUniqueId} (random per stub),
 * {@code getLocation} (the spawn location), {@code getWorld}
 * ({@code location.getWorld()}), {@code getType}, {@code isValid}
 * (false), {@code isDead} (true), {@code remove} (no-op).
 * Everything else hits {@link StubCallLog#logOnce} and returns the
 * default value for the method's return type. Object methods
 * ({@code equals}, {@code hashCode}, {@code toString}) are handled
 * directly so collections work.
 */
public final class StubEntity {

    private StubEntity() {}

    /** Build an Entity proxy seeded with a random UUID at the given location. */
    public static Entity create(EntityType type, Location location) {
        return create(Entity.class, type, location);
    }

    /**
     * Typed variant for {@code World.spawn(Location, Class<T>)}.
     * The supplied class must be an interface (most Bukkit entity types
     * are) — callers that pass a concrete class get back the parent
     * {@link Entity} interface, which still answers {@code getUniqueId}.
     *
     * <p>The proxy is generated with the supplied class's classloader so
     * downstream casts to interfaces in that loader's view (e.g.
     * Essentials's {@code (LivingEntity) world.spawn(loc, Pig.class)})
     * resolve to the same Class object the caller sees. The proxy is
     * also explicitly declared as implementing every transitive
     * super-interface in the {@link Entity} hierarchy — without that,
     * some JVMs raise {@link ClassCastException} on checkcast paths
     * that walk the interface tree across module boundaries.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T create(Class<T> clazz, EntityType type, Location location) {
        UUID uuid = UUID.randomUUID();
        Class<?> ifaceForProxy = clazz != null && clazz.isInterface() ? clazz : Entity.class;
        Class<?>[] interfaces = collectEntityInterfaces(ifaceForProxy);
        ClassLoader loader = ifaceForProxy.getClassLoader();
        if (loader == null) loader = StubEntity.class.getClassLoader();
        InvocationHandler handler = new EntityHandler(uuid, type, location, ifaceForProxy);
        Object proxy = Proxy.newProxyInstance(loader, interfaces, handler);
        return (T) proxy;
    }

    /** Collect {@code root} and every {@link Entity}-rooted super-interface
     *  (BFS) so the generated proxy class explicitly declares each one.
     *  Non-Entity interfaces (e.g. {@code Keyed}, {@code Lootable}) are
     *  skipped — they're outside the cast surface plugins use against
     *  spawned mobs and pulling them in would force the proxy to fulfil
     *  unrelated contracts. */
    private static Class<?>[] collectEntityInterfaces(Class<?> root) {
        Set<Class<?>> out = new LinkedHashSet<>();
        out.add(root);
        List<Class<?>> queue = new ArrayList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Class<?> cur = queue.remove(0);
            for (Class<?> superIface : cur.getInterfaces()) {
                if (Entity.class.isAssignableFrom(superIface) && out.add(superIface)) {
                    queue.add(superIface);
                }
            }
        }
        return out.toArray(new Class<?>[0]);
    }

    private static final class EntityHandler implements InvocationHandler {
        private final UUID uuid;
        private final EntityType type;
        private final Location location;
        private final Class<?> proxyInterface;

        EntityHandler(UUID uuid, EntityType type, Location location, Class<?> proxyInterface) {
            this.uuid = uuid;
            this.type = type;
            this.location = location;
            this.proxyInterface = proxyInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            switch (name) {
                case "getUniqueId":
                    return uuid;
                case "getLocation":
                    return location;
                case "getWorld":
                    return location == null ? null : location.getWorld();
                case "getType":
                    return type;
                case "isValid":
                case "isOnGround":
                case "isInWater":
                    return false;
                case "isDead":
                    return true;
                case "remove":
                case "setVelocity":
                case "setRotation":
                case "teleport":
                    return defaultReturn(method.getReturnType());
                case "equals":
                    return args != null && args.length == 1 && proxy == args[0];
                case "hashCode":
                    return uuid.hashCode();
                case "toString":
                    return "StubEntity[" + type + ", " + uuid + "]";
            }
            StubCallLog.logOnce(null,
                    proxyInterface.getName() + "." + name
                            + signature(method.getParameterTypes()));
            return defaultReturn(method.getReturnType());
        }

        private static String signature(Class<?>[] params) {
            StringBuilder sb = new StringBuilder("(");
            for (Class<?> p : params) {
                sb.append(p.getName()).append(';');
            }
            return sb.append(')').toString();
        }

        private static Object defaultReturn(Class<?> rt) {
            if (rt == void.class || !rt.isPrimitive()) return null;
            if (rt == boolean.class) return Boolean.FALSE;
            if (rt == byte.class) return (byte) 0;
            if (rt == short.class) return (short) 0;
            if (rt == int.class) return 0;
            if (rt == long.class) return 0L;
            if (rt == float.class) return 0f;
            if (rt == double.class) return 0d;
            if (rt == char.class) return '\0';
            return null;
        }
    }
}
