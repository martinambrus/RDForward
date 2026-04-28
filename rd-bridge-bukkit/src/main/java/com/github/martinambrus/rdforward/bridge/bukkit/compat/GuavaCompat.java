// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import com.google.common.cache.Cache;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;

/**
 * Static shims invoked by {@link LegacyGuavaTransformer}-rewritten plugin
 * bytecode. Lets pre-Guava-11 plugins (Essentials 2.8.x compiled against
 * CraftBukkit 1.7.x's bundled Guava 10) link against modern Guava on the
 * server classpath.
 *
 * <p>Each method takes the original receiver as its first argument so the
 * rewriter can swap an {@code INVOKEINTERFACE} for an {@code INVOKESTATIC}
 * with no stack manipulation — receiver and argument flow through 1:1.
 */
public final class GuavaCompat {

    private GuavaCompat() {}

    /**
     * Replacement for legacy {@code Cache.get(Object)} — that method was
     * moved to {@link LoadingCache} in Guava 11. The plugin's field type
     * is still {@link Cache}, so we cast at call time. Throws the same
     * exception type the plugin already catches
     * ({@link ExecutionException}).
     */
    public static Object cacheGet(Cache<Object, Object> cache, Object key) throws ExecutionException {
        if (cache instanceof LoadingCache<?, ?> lc) {
            @SuppressWarnings("unchecked")
            LoadingCache<Object, Object> typed = (LoadingCache<Object, Object>) lc;
            return typed.get(key);
        }
        return cache.getIfPresent(key);
    }
}
