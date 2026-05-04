package com.avaje.ebean;

/** No-op implementation for plugins that call {@code getDatabase()}. */
public class NoOpEbeanServer implements EbeanServer {

    @Override public Transaction beginTransaction() {
        throw new UnsupportedOperationException("EbeanServer not available");
    }

    @Override public void commitTransaction() {
        throw new UnsupportedOperationException("EbeanServer not available");
    }

    @Override public SqlUpdate createSqlUpdate(String sql) {
        throw new UnsupportedOperationException("EbeanServer not available");
    }

    @Override public SqlQuery createSqlQuery(String sql) {
        throw new UnsupportedOperationException("EbeanServer not available");
    }

    @Override public <T> Query<T> find(Class<T> beanType) {
        throw new UnsupportedOperationException("EbeanServer not available");
    }

    @Override public void save(Object bean) {
        throw new UnsupportedOperationException("EbeanServer not available");
    }
}
