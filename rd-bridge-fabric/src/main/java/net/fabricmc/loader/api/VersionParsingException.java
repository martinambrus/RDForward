// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.loader.api;

/**
 * Fabric version-parsing exception. Upstream extends a pre-0.4 relic at
 * {@code net.fabricmc.loader.util.version.VersionParsingException}; that
 * package is in the codegen REFUSED list (internal impl), so the bridge
 * collapses the inheritance onto {@link Exception} directly. Catch-site
 * behavior matches: callers catching {@code Exception} or
 * {@code VersionParsingException} still fire.
 */
public class VersionParsingException extends Exception {

    public VersionParsingException() {
        super();
    }

    public VersionParsingException(String message) {
        super(message);
    }

    public VersionParsingException(Throwable cause) {
        super(cause);
    }

    public VersionParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
