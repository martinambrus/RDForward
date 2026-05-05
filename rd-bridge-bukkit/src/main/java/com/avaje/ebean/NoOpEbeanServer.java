package com.avaje.ebean;

import javax.persistence.PersistenceException;

/** No-op implementation for plugins that call {@code getDatabase()}.
 *  All methods throw {@link PersistenceException} so that plugins which
 *  properly catch it (as real Bukkit Ebean failures would produce) can
 *  fall back to YAML or file-based storage. */
public class NoOpEbeanServer implements EbeanServer {

    private static final String MSG = "EbeanServer not available";

    @Override public Transaction beginTransaction() {
        throw new PersistenceException(MSG);
    }

    @Override public void commitTransaction() {
        throw new PersistenceException(MSG);
    }

    @Override public SqlUpdate createSqlUpdate(String sql) {
        throw new PersistenceException(MSG);
    }

    @Override public SqlQuery createSqlQuery(String sql) {
        throw new PersistenceException(MSG);
    }

    @Override public <T> Query<T> find(Class<T> beanType) {
        throw new PersistenceException(MSG);
    }

    @Override public void save(Object bean) {
        throw new PersistenceException(MSG);
    }
}
