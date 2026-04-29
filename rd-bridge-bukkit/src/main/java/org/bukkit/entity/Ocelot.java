package org.bukkit.entity;

// @rdforward:preserve - hand-tuned: kept legacy Tameable parent
/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar, with
 *  pre-1.14 {@link org.bukkit.entity.Tameable} parent re-added.
 *
 *  <p>Pre-1.14 Bukkit had {@code Ocelot extends Tameable}; the rewrite
 *  that introduced cats moved the taming surface from Ocelot to Cat.
 *  Essentials Pre-2.14's {@code Commandkittycannon} still calls
 *  {@code ocelot.setTamed(true)} after spawning, so the symbolic link
 *  must continue to resolve — bring Tameable back as a parent (pure
 *  type-system edit; no behavioural dependency on the modern split). */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Ocelot extends org.bukkit.entity.Animals, org.bukkit.entity.Tameable {
    boolean isTrusting();
    void setTrusting(boolean arg0);
    org.bukkit.entity.Ocelot$Type getCatType();
    void setCatType(org.bukkit.entity.Ocelot$Type arg0);
}
