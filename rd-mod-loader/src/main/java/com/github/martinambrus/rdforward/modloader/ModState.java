package com.github.martinambrus.rdforward.modloader;

/**
 * Lifecycle state of a {@link ModContainer}.
 *
 * <pre>
 *   DISCOVERED --&gt; LOADING --&gt; ENABLED --&gt; DISABLING --&gt; DISABLED
 *                      |                                    |
 *                      +-------&gt; ERROR &lt;-------------------+
 * </pre>
 *
 * {@code ERROR} is terminal — mods that enter it are logged and skipped
 * on subsequent operations until the server restarts.
 */
public enum ModState {
    DISCOVERED,
    LOADING,
    ENABLED,
    DISABLING,
    DISABLED,
    ERROR
}
