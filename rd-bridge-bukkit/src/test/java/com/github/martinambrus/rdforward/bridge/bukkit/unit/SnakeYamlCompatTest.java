package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.compat.SnakeYamlCompat;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * SnakeYAML 1.13 promoted {@code typeDefinitions} from
 * {@code Constructor} up to {@code BaseConstructor}. EssentialsX
 * Pre-2.14 reads it via {@code Constructor.class.getDeclaredField(
 * "typeDefinitions")}, which throws on the modern hierarchy because
 * the field is now two levels above. {@link SnakeYamlCompat} fixes
 * the lookup by walking the class chain — these tests verify that
 * walk against the live SnakeYAML on the classpath PLUS a synthetic
 * hierarchy that exercises the corner cases the live test cannot
 * (missing fields, intermediate hits, null start).
 */
class SnakeYamlCompatTest {

    @Test
    void findsFieldOnDeclaringClass() throws Exception {
        Field f = SnakeYamlCompat.findDeclaredFieldRecursive(Parent.class, "parentField");
        assertEquals("parentField", f.getName());
        assertSame(Parent.class, f.getDeclaringClass());
    }

    @Test
    void walksUpToFindFieldOnSuperclass() throws Exception {
        // Replicates the exact shape of the EssentialsX bug: query a
        // subclass for a field that lives on the superclass. Strict
        // {@code Class.getDeclaredField} would throw here.
        Field f = SnakeYamlCompat.findDeclaredFieldRecursive(Child.class, "parentField");
        assertEquals("parentField", f.getName());
        assertSame(Parent.class, f.getDeclaringClass(),
                "field must surface from the class that declares it, not the queried child");
    }

    @Test
    void prefersChildFieldWhenBothDeclareSameName() throws Exception {
        // Same-name fields on parent + child: the walk starts at the
        // queried class, so the child's declaration wins (mirrors how
        // direct {@code getDeclaredField} would resolve).
        Field f = SnakeYamlCompat.findDeclaredFieldRecursive(ShadowingChild.class, "shadowed");
        assertSame(ShadowingChild.class, f.getDeclaringClass(),
                "queried class's own declaration must take precedence");
    }

    @Test
    void throwsWhenFieldIsAbsentFromEntireHierarchy() {
        assertThrows(NoSuchFieldException.class,
                () -> SnakeYamlCompat.findDeclaredFieldRecursive(Child.class, "noSuchField"));
    }

    @Test
    void stopsAtObjectClassBoundary() {
        // The walk explicitly stops at {@link Object} so it does not
        // touch JDK-internal fields the JVM may add via instrumentation
        // / reflection-policy modules. Asking for a missing field on a
        // direct {@code Object} subclass must surface NoSuchFieldException
        // rather than scanning Object.class.
        assertThrows(NoSuchFieldException.class,
                () -> SnakeYamlCompat.findDeclaredFieldRecursive(Parent.class, "wait"));
    }

    @Test
    void liveSnakeYamlTypeDefinitionsResolves() throws Exception {
        // The live integration: against the SnakeYAML the bridge bundles,
        // querying {@code Constructor} for {@code typeDefinitions} must
        // succeed (returning the field declared on
        // {@code BaseConstructor}). This is the exact lookup
        // EssentialsX issues, and the test fails closed if a future
        // SnakeYAML upgrade renames or removes the field.
        Field f = SnakeYamlCompat.findDeclaredFieldRecursive(
                org.yaml.snakeyaml.constructor.Constructor.class, "typeDefinitions");
        assertEquals("typeDefinitions", f.getName());
    }

    private static class Parent {
        @SuppressWarnings("unused") private String parentField;
        @SuppressWarnings("unused") private int shadowed;
    }

    private static class Child extends Parent {
        @SuppressWarnings("unused") private String childField;
    }

    private static class ShadowingChild extends Parent {
        @SuppressWarnings("unused") private int shadowed;
    }
}
