package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Pinning regression test for LoginSecurity's {@code CommentConfiguration},
 * which calls {@code new StringBuilder(buildHeader())} during
 * {@code save()}. {@code StringBuilder(null)} throws
 * {@link NullPointerException} from the JDK with the message
 * {@code Cannot invoke "String.length()" because "str" is null}, so the
 * stub must return an empty string rather than {@code null}.
 *
 * <p>The accessor is {@code protected} on {@link FileConfiguration}, so
 * the test reaches it via reflection.
 */
class BuildHeaderNonNullTest {

    @Test
    void buildHeaderReturnsEmptyStringNotNull() throws Exception {
        FileConfiguration cfg = new YamlConfiguration();
        Method m = FileConfiguration.class.getDeclaredMethod("buildHeader");
        m.setAccessible(true);
        Object result = m.invoke(cfg);
        assertNotNull(result, "buildHeader() must not return null — feeds StringBuilder ctor");
        assertEquals("", result, "stub returns an empty header — RDForward never serialises YAML");
    }
}
