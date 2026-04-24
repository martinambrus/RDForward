package com.mojang.brigadier.context;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Brigadier execution context. Carries the command source and the resolved
 * argument values; the bridge populates the argument map from raw rd-api
 * command args during dispatch, then hands the context to {@code Command#run}.
 */
public final class CommandContext<S> {

    private final S source;
    private final Map<String, Object> arguments = new LinkedHashMap<>();

    public CommandContext(S source) {
        this.source = source;
    }

    public S getSource() { return source; }

    public <T> T getArgument(String name, Class<T> clazz) {
        Object v = arguments.get(name);
        if (v == null) return null;
        if (clazz.isInstance(v)) return clazz.cast(v);
        if (clazz == String.class) return clazz.cast(v.toString());
        throw new IllegalArgumentException(
                "argument '" + name + "' is " + v.getClass().getName()
                        + ", cannot cast to " + clazz.getName());
    }

    public void putArgument(String name, Object value) {
        arguments.put(name, value);
    }
}
