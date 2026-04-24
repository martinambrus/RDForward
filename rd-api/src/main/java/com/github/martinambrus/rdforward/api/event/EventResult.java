package com.github.martinambrus.rdforward.api.event;

/**
 * Result of handling a cancellable event.
 *
 * <p>PASS: continue processing, pass to next listener, proceed with default action.
 * <p>SUCCESS: handled successfully, stop processing, proceed with default action.
 * <p>FAIL: cancel the action, stop processing, do NOT perform the default action.
 *
 * <p>CANCEL is kept as an alias for FAIL for backward-compatibility with earlier callsites.
 */
public enum EventResult {
    PASS,
    SUCCESS,
    FAIL;

    /** Alias for FAIL — historical name used by early rd-server callsites. */
    public static final EventResult CANCEL = FAIL;
}
