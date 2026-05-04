package com.avaje.ebean;

import java.util.Set;

public interface Query<T> {
    ExpressionList<T> where();
    Set<T> findSet();
    T findUnique();
    int findRowCount();
}
