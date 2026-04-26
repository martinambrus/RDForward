package com.github.martinambrus.rdforward.modloader;

import com.github.martinambrus.rdforward.api.permission.PermissionDefault;
import com.github.martinambrus.rdforward.api.permission.PermissionRegistry;
import com.github.martinambrus.rdforward.api.permission.RegisteredPermission;
import com.github.martinambrus.rdforward.modloader.impl.RDPermissionManager;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link PermissionRegistry} contract and the
 * {@link PermissionDefault#appliesTo} matrix. The registry under test is
 * the {@code InMemoryPermissionRegistry} reachable via
 * {@link RDPermissionManager#getRegistry()}.
 */
class PermissionRegistryUnitTest {

    @Test
    void appliesToMatrix() {
        assertTrue(PermissionDefault.TRUE.appliesTo(true));
        assertTrue(PermissionDefault.TRUE.appliesTo(false));
        assertFalse(PermissionDefault.FALSE.appliesTo(true));
        assertFalse(PermissionDefault.FALSE.appliesTo(false));
        assertTrue(PermissionDefault.OP.appliesTo(true));
        assertFalse(PermissionDefault.OP.appliesTo(false));
        assertFalse(PermissionDefault.NOT_OP.appliesTo(true));
        assertTrue(PermissionDefault.NOT_OP.appliesTo(false));
    }

    @Test
    void registeredPermissionRejectsNullName() {
        assertThrows(NullPointerException.class,
                () -> new RegisteredPermission(null, PermissionDefault.OP));
    }

    @Test
    void registeredPermissionRejectsNullDefault() {
        assertThrows(NullPointerException.class,
                () -> new RegisteredPermission("a", null));
    }

    @Test
    void registeredPermissionDefensivelyCopiesChildren() {
        java.util.HashMap<String, Boolean> mutable = new java.util.HashMap<>();
        mutable.put("child.x", true);
        RegisteredPermission rp = new RegisteredPermission("parent", PermissionDefault.OP, mutable);
        mutable.put("child.y", false); // mutate the original after construction
        assertEquals(1, rp.children().size(),
                "registered permission must hold a defensive copy of the children map");
        assertTrue(rp.children().containsKey("child.x"));
        assertFalse(rp.children().containsKey("child.y"));
    }

    @Test
    void registerLookupUnregisterRoundTrip() {
        PermissionRegistry registry = new RDPermissionManager().getRegistry();
        registry.register(new RegisteredPermission("custom.foo", PermissionDefault.OP));
        RegisteredPermission round = registry.lookup("custom.foo");
        assertEquals("custom.foo", round.name());
        assertEquals(PermissionDefault.OP, round.defaultValue());

        registry.unregister("custom.foo");
        assertNull(registry.lookup("custom.foo"));
    }

    @Test
    void lookupNullNameReturnsNull() {
        PermissionRegistry registry = new RDPermissionManager().getRegistry();
        assertNull(registry.lookup(null));
    }

    @Test
    void defaultsForReturnsAppropriateNamesPerOpState() {
        PermissionRegistry registry = new RDPermissionManager().getRegistry();
        registry.register(new RegisteredPermission("always", PermissionDefault.TRUE));
        registry.register(new RegisteredPermission("never", PermissionDefault.FALSE));
        registry.register(new RegisteredPermission("ops_only", PermissionDefault.OP));
        registry.register(new RegisteredPermission("non_ops_only", PermissionDefault.NOT_OP));

        var forOps = registry.defaultsFor(true);
        assertTrue(forOps.contains("always"));
        assertFalse(forOps.contains("never"));
        assertTrue(forOps.contains("ops_only"));
        assertFalse(forOps.contains("non_ops_only"));

        var forNonOps = registry.defaultsFor(false);
        assertTrue(forNonOps.contains("always"));
        assertFalse(forNonOps.contains("never"));
        assertFalse(forNonOps.contains("ops_only"));
        assertTrue(forNonOps.contains("non_ops_only"));
    }

    @Test
    void replacingExistingPermissionUpdatesDefaultsCache() {
        PermissionRegistry registry = new RDPermissionManager().getRegistry();
        registry.register(new RegisteredPermission("toggle", PermissionDefault.OP));
        assertTrue(registry.defaultsFor(true).contains("toggle"));
        assertFalse(registry.defaultsFor(false).contains("toggle"));

        // Re-register with the opposite default — caches must reflect the new state.
        registry.register(new RegisteredPermission("toggle", PermissionDefault.NOT_OP));
        assertFalse(registry.defaultsFor(true).contains("toggle"));
        assertTrue(registry.defaultsFor(false).contains("toggle"));
    }

    @Test
    void allReturnsEverythingRegistered() {
        PermissionRegistry registry = new RDPermissionManager().getRegistry();
        registry.register(new RegisteredPermission("a", PermissionDefault.TRUE));
        registry.register(new RegisteredPermission("b", PermissionDefault.OP, Map.of("a", true)));
        assertEquals(2, registry.all().size());
    }
}
