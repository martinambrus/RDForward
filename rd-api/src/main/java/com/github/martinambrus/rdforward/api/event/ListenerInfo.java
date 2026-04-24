package com.github.martinambrus.rdforward.api.event;

/**
 * Snapshot of a registered listener for admin tooling.
 *
 * @param modId          owning mod id ("__server__" for core)
 * @param priority       dispatch priority
 * @param listenerClass  fully-qualified listener class / lambda descriptor
 */
public record ListenerInfo(String modId, EventPriority priority, String listenerClass) {}
