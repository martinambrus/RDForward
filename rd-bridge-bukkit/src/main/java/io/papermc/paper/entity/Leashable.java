package io.papermc.paper.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Leashable extends org.bukkit.entity.Entity {
    boolean isLeashed();
    org.bukkit.entity.Entity getLeashHolder() throws java.lang.IllegalStateException;
    boolean setLeashHolder(org.bukkit.entity.Entity arg0);
}
