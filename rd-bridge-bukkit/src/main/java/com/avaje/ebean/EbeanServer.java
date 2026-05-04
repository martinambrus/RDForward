package com.avaje.ebean;

public interface EbeanServer {
    Transaction beginTransaction();
    void commitTransaction();
    SqlUpdate createSqlUpdate(String sql);
    SqlQuery createSqlQuery(String sql);
    <T> Query<T> find(Class<T> beanType);
    void save(Object bean);
}
