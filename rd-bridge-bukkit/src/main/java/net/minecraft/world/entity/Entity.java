// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.minecraft.world.entity;

/**
 * Minecraft NMS stub. Real Mojang's
 * {@code net.minecraft.world.entity.Entity} is the root of every
 * server-side entity. RDForward has no NMS layer; this stub
 * exists only so plugins which look the class up via
 * {@code Class.forName(...)} during their compatibility probe
 * (Citizens 2.0.42 NMS.loadBridge at line 1140) get a class
 * back rather than a {@link NoClassDefFoundError}.
 *
 * <p>Plugins that proceed to call NMS-only methods on a real
 * entity (Citizens NPC injection, EntityHider tricks, packet-level
 * mods) are out of scope for the bridge — those code paths still
 * fail. The stub merely lets the version-probe complete so the
 * plugin can decide its own fate after that.
 */
public abstract class Entity {
}
