package com.github.martinambrus.rdforward.api.event;

/**
 * Marker interface for event callback types whose listeners may return
 * {@link EventResult#FAIL} to cancel the event.
 *
 * <p>Non-cancellable events (e.g. tick, lifecycle) have void-returning
 * callback signatures and do not implement this marker.
 */
public interface Cancellable {}
