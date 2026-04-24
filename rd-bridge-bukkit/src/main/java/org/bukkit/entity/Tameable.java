package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Tameable extends org.bukkit.entity.Animals {
    boolean isTamed();
    void setTamed(boolean arg0);
    java.util.UUID getOwnerUniqueId();
    org.bukkit.entity.AnimalTamer getOwner();
    void setOwner(org.bukkit.entity.AnimalTamer arg0);
}
