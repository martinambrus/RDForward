package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.PluginMCVersionResolver;
import com.github.martinambrus.rdforward.bridge.bukkit.PluginMCVersionResolver.MCVersionTuple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers PluginMCVersionResolver: precedence, api-version table,
 * stack-walk classloader matching, parent-chain walk, default
 * fallback, and concurrent register/unregister.
 */
class PluginMCVersionResolverTest {

    @BeforeEach
    @AfterEach
    void clean() {
        PluginMCVersionResolver.resetForTests();
    }

    @Test
    void defaultTupleMatchesPriorHardcodedStrings() {
        assertEquals("1.21.4", PluginMCVersionResolver.DEFAULT.mc());
        assertEquals("RDForward-bridge-1.0 (MC: 1.21.4)", PluginMCVersionResolver.DEFAULT.getVersion());
        assertEquals("1.21.4-R0.1-SNAPSHOT", PluginMCVersionResolver.DEFAULT.getBukkitVersion());
    }

    @Test
    void currentVersionReturnsDefaultWhenNoPluginsRegistered() {
        // Test method itself isn't loaded by any registered plugin loader,
        // so the resolver falls back to the default.
        assertEquals(PluginMCVersionResolver.DEFAULT, PluginMCVersionResolver.currentVersion());
    }

    @Test
    void apiVersionTableMapsMinorReleasesToLatestPatch() throws Exception {
        ClassLoader cl13 = new URLClassLoader("p13", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(cl13, "P13", "1.13");
        MCVersionTuple t = invokeOnLoader(cl13, PluginMCVersionResolver::currentVersion);
        assertEquals("1.13.2", t.mc());

        ClassLoader cl19 = new URLClassLoader("p19", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(cl19, "P19", "1.19");
        assertEquals("1.19.4", invokeOnLoader(cl19, PluginMCVersionResolver::currentVersion).mc());

        ClassLoader cl21 = new URLClassLoader("p21", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(cl21, "P21", "1.21");
        assertEquals("1.21.4", invokeOnLoader(cl21, PluginMCVersionResolver::currentVersion).mc());
    }

    @Test
    void apiVersionDottedZeroAndPrefixNormalisation() throws Exception {
        // "1.21.0" should normalise to "1.21" → 1.21.4
        ClassLoader a = new URLClassLoader("a", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(a, "A", "1.21.0");
        assertEquals("1.21.4", invokeOnLoader(a, PluginMCVersionResolver::currentVersion).mc());

        // "1.16.5" should fall through to "1.16" prefix → 1.16.5
        ClassLoader b = new URLClassLoader("b", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(b, "B", "1.16.5");
        assertEquals("1.16.5", invokeOnLoader(b, PluginMCVersionResolver::currentVersion).mc());
    }

    @Test
    void overrideMapWinsOverApiVersion() throws Exception {
        PluginMCVersionResolver.putOverrideForTests("Citizens", "1.7.9");
        ClassLoader cl = new URLClassLoader("citizens", new URL[0], ClassLoader.getSystemClassLoader());
        // Even with api-version 1.21 declared, the name override pins to 1.7.9.
        PluginMCVersionResolver.register(cl, "Citizens", "1.21");
        MCVersionTuple t = invokeOnLoader(cl, PluginMCVersionResolver::currentVersion);
        assertEquals("1.7.9", t.mc());
        assertEquals("RDForward-bridge-1.0 (MC: 1.7.9)", t.getVersion());
        assertEquals("1.7.9-R0.1-SNAPSHOT", t.getBukkitVersion());
    }

    @Test
    void unknownApiVersionFallsBackToDefault() throws Exception {
        ClassLoader cl = new URLClassLoader("u", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(cl, "U", "9.99");
        MCVersionTuple t = invokeOnLoader(cl, PluginMCVersionResolver::currentVersion);
        assertEquals(PluginMCVersionResolver.DEFAULT, t);
    }

    @Test
    void parentChainWalkRecognisesChildLoader() throws Exception {
        ClassLoader pluginLoader = new URLClassLoader("plugin", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(pluginLoader, "P", "1.13");
        // Child loader (e.g. shaded library) — not directly registered,
        // but parent is. Resolver must walk the chain.
        ClassLoader child = new URLClassLoader("child", new URL[0], pluginLoader);
        MCVersionTuple t = invokeOnLoader(child, PluginMCVersionResolver::currentVersion);
        assertEquals("1.13.2", t.mc());
    }

    @Test
    void unregisterRemovesMapping() throws Exception {
        ClassLoader cl = new URLClassLoader("x", new URL[0], ClassLoader.getSystemClassLoader());
        PluginMCVersionResolver.register(cl, "X", "1.16");
        assertEquals("1.16.5", invokeOnLoader(cl, PluginMCVersionResolver::currentVersion).mc());
        PluginMCVersionResolver.unregister(cl);
        assertEquals(PluginMCVersionResolver.DEFAULT,
                invokeOnLoader(cl, PluginMCVersionResolver::currentVersion));
    }

    @Test
    void concurrentRegisterUnregisterDoesNotThrow() throws Exception {
        int threads = 8;
        int iters = 200;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger errors = new AtomicInteger();
        for (int i = 0; i < threads; i++) {
            final int id = i;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int k = 0; k < iters; k++) {
                        ClassLoader cl = new URLClassLoader("c" + id + "-" + k,
                                new URL[0], ClassLoader.getSystemClassLoader());
                        PluginMCVersionResolver.register(cl, "P" + id + "-" + k, "1.21");
                        PluginMCVersionResolver.currentVersion();
                        PluginMCVersionResolver.unregister(cl);
                    }
                } catch (Throwable t) {
                    errors.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertEquals(0, errors.get());
    }

    /** Run {@code body} on a thread whose context class loader is
     *  {@code loader}, AND whose stack contains a frame whose declaring
     *  class is loaded by {@code loader}. The resolver walks the stack
     *  by declaring class — context-CL alone is not enough. We achieve
     *  this by defining a synthetic Proxy class in {@code loader} that
     *  forwards a Callable invocation. */
    @SuppressWarnings("unchecked")
    private static <T> T invokeOnLoader(ClassLoader loader, Callable<T> body) throws Exception {
        // Proxy class generated by Proxy.newProxyInstance is loaded by
        // the supplied classloader, so its methods appear on the stack
        // as frames whose declaring class belongs to that loader.
        InvocationHandler handler = (proxy, method, args) -> body.call();
        Class<?> proxyClass = Proxy.getProxyClass(loader, Callable.class);
        Callable<T> proxy = (Callable<T>) proxyClass
                .getDeclaredConstructor(InvocationHandler.class)
                .newInstance(handler);
        return proxy.call();
    }
}
