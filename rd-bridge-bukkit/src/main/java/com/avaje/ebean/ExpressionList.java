package com.avaje.ebean;

import java.util.Set;

public interface ExpressionList<T> {
    ExpressionList<T> ieq(String propertyName, String value);
    T findUnique();
    Set<T> findSet();
}
