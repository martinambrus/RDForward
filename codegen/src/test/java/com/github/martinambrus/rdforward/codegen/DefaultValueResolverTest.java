package com.github.martinambrus.rdforward.codegen;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Type;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultValueResolverTest {

    private final DefaultValueResolver resolver = new DefaultValueResolver(Map.of());

    @Test
    void voidProducesEmptyExpression() {
        assertEquals("", resolver.defaultExpression(Type.VOID_TYPE));
    }

    @Test
    void booleanProducesFalse() {
        assertEquals("false", resolver.defaultExpression(Type.BOOLEAN_TYPE));
    }

    @Test
    void byteShortCharHaveExplicitCasts() {
        assertEquals("(byte) 0", resolver.defaultExpression(Type.BYTE_TYPE));
        assertEquals("(short) 0", resolver.defaultExpression(Type.SHORT_TYPE));
        assertEquals("(char) 0", resolver.defaultExpression(Type.CHAR_TYPE));
    }

    @Test
    void intLongFloatDoubleZeros() {
        assertEquals("0", resolver.defaultExpression(Type.INT_TYPE));
        assertEquals("0L", resolver.defaultExpression(Type.LONG_TYPE));
        assertEquals("0.0f", resolver.defaultExpression(Type.FLOAT_TYPE));
        assertEquals("0.0", resolver.defaultExpression(Type.DOUBLE_TYPE));
    }

    @Test
    void primitiveArrayProducesEmptyArrayNew() {
        assertEquals("new int[0]", resolver.defaultExpression(Type.getType("[I")));
        assertEquals("new byte[0]", resolver.defaultExpression(Type.getType("[B")));
        assertEquals("new double[0]", resolver.defaultExpression(Type.getType("[D")));
    }

    @Test
    void referenceArrayProducesEmptyArrayNewWithFqcn() {
        assertEquals("new java.lang.String[0]",
                resolver.defaultExpression(Type.getType("[Ljava/lang/String;")));
    }

    @Test
    void multiDimArrayProducesNull() {
        assertEquals("null", resolver.defaultExpression(Type.getType("[[I")));
    }

    @Test
    void optionalProducesEmpty() {
        assertEquals("java.util.Optional.empty()",
                resolver.defaultExpression(Type.getType("Ljava/util/Optional;")));
    }

    @Test
    void listProducesEmptyList() {
        assertEquals("java.util.Collections.emptyList()",
                resolver.defaultExpression(Type.getType("Ljava/util/List;")));
    }

    @Test
    void setProducesEmptySet() {
        assertEquals("java.util.Collections.emptySet()",
                resolver.defaultExpression(Type.getType("Ljava/util/Set;")));
    }

    @Test
    void mapProducesEmptyMap() {
        assertEquals("java.util.Collections.emptyMap()",
                resolver.defaultExpression(Type.getType("Ljava/util/Map;")));
    }

    @Test
    void collectionProducesEmptyList() {
        assertEquals("java.util.Collections.emptyList()",
                resolver.defaultExpression(Type.getType("Ljava/util/Collection;")));
    }

    @Test
    void unknownReferenceProducesNull() {
        assertEquals("null",
                resolver.defaultExpression(Type.getType("Ljava/lang/String;")));
    }

    @Test
    void sentinelOverrideWins() {
        DefaultValueResolver r = new DefaultValueResolver(Map.of(
                "org.bukkit.inventory.ItemStack", "org.bukkit.inventory.ItemStack.EMPTY",
                "net.kyori.adventure.text.Component", "net.kyori.adventure.text.Component.empty()"));
        assertEquals("org.bukkit.inventory.ItemStack.EMPTY",
                r.defaultExpression(Type.getType("Lorg/bukkit/inventory/ItemStack;")));
        assertEquals("net.kyori.adventure.text.Component.empty()",
                r.defaultExpression(Type.getType("Lnet/kyori/adventure/text/Component;")));
    }

    @Test
    void sentinelOverrideBeatsCollectionFallback() {
        DefaultValueResolver r = new DefaultValueResolver(Map.of(
                "java.util.List", "my.pkg.Lists.empty()"));
        assertEquals("my.pkg.Lists.empty()",
                r.defaultExpression(Type.getType("Ljava/util/List;")));
    }

    @Test
    void bundledResolverLoadsWithoutError() {
        DefaultValueResolver r = new DefaultValueResolver();
        assertEquals("null",
                r.defaultExpression(Type.getType("Ljava/lang/String;")));
    }
}
