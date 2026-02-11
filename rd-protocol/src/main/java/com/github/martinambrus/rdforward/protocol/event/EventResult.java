package com.github.martinambrus.rdforward.protocol.event;

/**
 * Result of handling a cancellable event.
 *
 * Used by events like BlockBreak, BlockPlace, and Chat where a mod
 * may want to prevent the default action from occurring.
 */
public enum EventResult {

    /** Continue processing — pass to next handler, then proceed with default action. */
    PASS,

    /** Event handled successfully — stop processing, proceed with default action. */
    SUCCESS,

    /** Cancel the action — stop processing, do NOT perform the default action. */
    CANCEL
}
