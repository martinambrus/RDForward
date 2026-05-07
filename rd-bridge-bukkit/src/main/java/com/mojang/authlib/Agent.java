// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.mojang.authlib;

/**
 * Mojang authlib stub. Real authlib's {@code Agent} identifies the
 * application contacting Mojang services (Minecraft vs. Scrolls).
 * Plugins reference {@link #MINECRAFT} when calling
 * {@link GameProfileRepository#findProfilesByNames}. The stub
 * preserves the singleton + accessor shape so that compile-time
 * references resolve at runtime.
 */
public class Agent {

    public static final Agent MINECRAFT = new Agent("Minecraft", 1);

    private final String name;
    private final int version;

    public Agent(String name, int version) {
        this.name = name;
        this.version = version;
    }

    public String getName() { return name; }
    public int getVersion() { return version; }

    @Override
    public String toString() { return name + " v" + version; }
}
