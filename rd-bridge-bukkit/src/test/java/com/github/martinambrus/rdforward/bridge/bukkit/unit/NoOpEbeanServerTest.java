package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.avaje.ebean.NoOpEbeanServer;
import org.junit.jupiter.api.Test;

import javax.persistence.PersistenceException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies NoOpEbeanServer throws PersistenceException on every method.
 * Plugins like TimeShift catch PersistenceException (as real Bukkit Ebean
 * failures would produce) to fall back to YAML/file-based storage.
 */
class NoOpEbeanServerTest {

    private final NoOpEbeanServer server = new NoOpEbeanServer();

    @Test
    void beginTransactionThrows() {
        assertThrows(PersistenceException.class, server::beginTransaction);
    }

    @Test
    void commitTransactionThrows() {
        assertThrows(PersistenceException.class, server::commitTransaction);
    }

    @Test
    void createSqlUpdateThrows() {
        assertThrows(PersistenceException.class, () -> server.createSqlUpdate("SELECT 1"));
    }

    @Test
    void createSqlQueryThrows() {
        assertThrows(PersistenceException.class, () -> server.createSqlQuery("SELECT 1"));
    }

    @Test
    void findThrows() {
        assertThrows(PersistenceException.class, () -> server.find(Object.class));
    }

    @Test
    void saveThrows() {
        assertThrows(PersistenceException.class, () -> server.save(new Object()));
    }

    @Test
    void exceptionMessageContainsContext() {
        var ex = assertThrows(PersistenceException.class, server::beginTransaction);
        assertTrue(ex.getMessage().contains("EbeanServer"),
                "message should mention EbeanServer for operator clarity");
    }
}
