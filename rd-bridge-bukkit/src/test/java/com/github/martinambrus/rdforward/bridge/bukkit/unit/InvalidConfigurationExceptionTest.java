package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies InvalidConfigurationException constructors pass through
 * messages and causes. Previously these were no-ops that discarded args.
 * HomeSpawnPlus's YAML storage relies on the message being preserved.
 */
class InvalidConfigurationExceptionTest {

    @Test
    void messageConstructorPreservesMessage() {
        InvalidConfigurationException ex = new InvalidConfigurationException("test message");
        assertEquals("test message", ex.getMessage());
    }

    @Test
    void causeConstructorPreservesCause() {
        Throwable cause = new RuntimeException("root cause");
        InvalidConfigurationException ex = new InvalidConfigurationException(cause);
        assertSame(cause, ex.getCause());
    }

    @Test
    void messageAndCauseConstructor() {
        Throwable cause = new RuntimeException("root");
        InvalidConfigurationException ex = new InvalidConfigurationException("msg", cause);
        assertEquals("msg", ex.getMessage());
        assertSame(cause, ex.getCause());
    }

    @Test
    void defaultConstructorNoCrash() {
        assertDoesNotThrow(() -> new InvalidConfigurationException());
    }
}
