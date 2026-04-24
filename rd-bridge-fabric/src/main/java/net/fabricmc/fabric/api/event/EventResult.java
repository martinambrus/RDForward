// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.fabricmc.fabric.api.event;

/**
 * Fabric-namespaced enum mirroring rd-api
 * {@link com.github.martinambrus.rdforward.api.event.EventResult}. Cannot
 * subclass the rd-api enum directly, so the constants are redeclared here
 * with conversion helpers. Mods that register against Fabric-typed events
 * can return these values and the bridge converts before dispatch.
 */
public enum EventResult {
    PASS,
    SUCCESS,
    FAIL;

    /** Alias for {@link #FAIL}, matching rd-api. */
    public static final EventResult CANCEL = FAIL;

    /** Convert this value to the rd-api EventResult. */
    public com.github.martinambrus.rdforward.api.event.EventResult toApi() {
        switch (this) {
            case PASS:    return com.github.martinambrus.rdforward.api.event.EventResult.PASS;
            case SUCCESS: return com.github.martinambrus.rdforward.api.event.EventResult.SUCCESS;
            case FAIL:    return com.github.martinambrus.rdforward.api.event.EventResult.FAIL;
            default: throw new IllegalStateException("unreachable");
        }
    }

    /** Convert an rd-api EventResult into the Fabric-namespaced equivalent. */
    public static EventResult fromApi(com.github.martinambrus.rdforward.api.event.EventResult r) {
        switch (r) {
            case PASS:    return PASS;
            case SUCCESS: return SUCCESS;
            case FAIL:    return FAIL;
            default: throw new IllegalStateException("unreachable");
        }
    }
}
