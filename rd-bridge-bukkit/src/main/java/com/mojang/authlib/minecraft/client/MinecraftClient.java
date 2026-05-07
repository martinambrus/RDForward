// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.mojang.authlib.minecraft.client;

/**
 * Mojang authlib stub. Real authlib's
 * {@code com.mojang.authlib.minecraft.client.MinecraftClient}
 * is the HTTP transport plugins use to talk to Mojang's session
 * service. RDForward does not contact Mojang at runtime, so this
 * stub exists only to keep plugin class init from failing with
 * {@link NoClassDefFoundError}.
 *
 * <p>Modelled as an abstract class (not interface) so it can carry
 * the {@code LOGGER} static field. Citizens 2.0.42 NMS clinit
 * reflectively overwrites this field with a no-op SLF4J logger to
 * silence authlib's HTTP chatter; if the field is missing, the
 * MethodHandle returned by {@code NMS.getFinalSetter} is null and
 * the invoke NPEs, which Citizens reads as "wrong Minecraft
 * version" and self-disables.
 */
public abstract class MinecraftClient {

    /** Real authlib uses this for HTTP debug logging. The bridge
     *  pre-initialises it to a no-op so plugins that fail to find
     *  / overwrite the field still observe a non-null logger. */
    public static org.slf4j.Logger LOGGER = org.slf4j.helpers.NOPLogger.NOP_LOGGER;
}
