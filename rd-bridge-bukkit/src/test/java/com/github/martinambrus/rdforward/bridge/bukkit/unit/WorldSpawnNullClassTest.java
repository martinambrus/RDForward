package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the defensive null-class branch in
 * {@link World#spawn(Location, Class)}. Plugins that resolve an entity
 * class via {@code EntityType.getEntityClass()} can still pass null
 * (e.g. {@code EntityType.UNKNOWN}, or any future enum constant the
 * resolver has no mapping for). The original implementation handed the
 * null off to {@link com.github.martinambrus.rdforward.bridge.bukkit.StubEntity}
 * which returned a raw {@link Entity} proxy — fine until a plugin
 * checkcasts the result to {@link org.bukkit.entity.LivingEntity}, at
 * which point the JVM throws {@link ClassCastException}.
 *
 * <p>The fix returns null from World.spawn when clazz is null. JVM
 * checkcast accepts null for any reference type, so {@code (LivingEntity)
 * null} is a safe no-op and Essentials's null-check after the cast
 * handles the missing entity gracefully.
 */
class WorldSpawnNullClassTest {

    /** Minimal World stub that inherits the bridge's default
     *  {@code spawn(Location, Class)} implementation. Everything else is
     *  unimplemented — JDK Proxy delegates abstract method calls to the
     *  handler's default values. */
    private static World minimalWorld() {
        return (World) Proxy.newProxyInstance(
                WorldSpawnNullClassTest.class.getClassLoader(),
                new Class<?>[]{World.class},
                (proxy, method, args) -> {
                    if (method.isDefault()) {
                        return java.lang.invoke.MethodHandles.lookup()
                                .findSpecial(World.class, method.getName(),
                                        java.lang.invoke.MethodType.methodType(
                                                method.getReturnType(),
                                                method.getParameterTypes()),
                                        World.class)
                                .bindTo(proxy)
                                .invokeWithArguments(args);
                    }
                    Class<?> rt = method.getReturnType();
                    if (rt == void.class || !rt.isPrimitive()) return null;
                    if (rt == boolean.class) return Boolean.FALSE;
                    return 0;
                });
    }

    @Test
    void spawnReturnsNullForNullClass() {
        // Direct call exercises the default method on World — must not
        // throw, must not produce a stub proxy that fails downstream
        // casts.
        World w = minimalWorld();
        Object result = assertDoesNotThrow(() -> w.spawn(null, null),
                "World.spawn(null, null) must not throw NPE");
        assertNull(result, "null clazz must return null so plugin checkcast accepts the value");
    }

    @Test
    void defaultMethodLooksUpViaReflection() throws Exception {
        // Sanity: the default exists and the no-arg case (null clazz)
        // wasn't accidentally turned into an abstract method.
        Method m = World.class.getMethod("spawn", Location.class, Class.class);
        assertDoesNotThrow(() -> {
            // Method is declared on an interface; default-ness is the
            // important property for the bridge's stub strategy.
            if (!m.isDefault()) {
                throw new AssertionError("World.spawn(Location, Class) must remain a default method");
            }
        });
    }
}
