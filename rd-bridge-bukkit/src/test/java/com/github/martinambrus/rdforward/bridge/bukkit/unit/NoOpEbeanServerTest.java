package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.avaje.ebean.NoOpEbeanServer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies NoOpEbeanServer throws UnsupportedOperationException on every
 * method. HomeSpawnPlus's Ebean storage path catches this and falls back
 * to YAML storage.
 */
class NoOpEbeanServerTest {

    private final NoOpEbeanServer server = new NoOpEbeanServer();

    @Test
    void beginTransactionThrows() {
        assertThrows(UnsupportedOperationException.class, server::beginTransaction);
    }

    @Test
    void commitTransactionThrows() {
        assertThrows(UnsupportedOperationException.class, server::commitTransaction);
    }

    @Test
    void createSqlUpdateThrows() {
        assertThrows(UnsupportedOperationException.class, () -> server.createSqlUpdate("SELECT 1"));
    }

    @Test
    void createSqlQueryThrows() {
        assertThrows(UnsupportedOperationException.class, () -> server.createSqlQuery("SELECT 1"));
    }

    @Test
    void findThrows() {
        assertThrows(UnsupportedOperationException.class, () -> server.find(Object.class));
    }

    @Test
    void saveThrows() {
        assertThrows(UnsupportedOperationException.class, () -> server.save(new Object()));
    }

    @Test
    void exceptionMessageContainsContext() {
        var ex = assertThrows(UnsupportedOperationException.class, server::beginTransaction);
        assertTrue(ex.getMessage().contains("EbeanServer"),
                "message should mention EbeanServer for operator clarity");
    }
}
