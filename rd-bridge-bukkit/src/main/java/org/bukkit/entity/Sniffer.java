package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Sniffer extends org.bukkit.entity.Animals {
    java.util.Collection getExploredLocations();
    void removeExploredLocation(org.bukkit.Location arg0);
    void addExploredLocation(org.bukkit.Location arg0);
    org.bukkit.entity.Sniffer$State getState();
    void setState(org.bukkit.entity.Sniffer$State arg0);
    org.bukkit.Location findPossibleDigLocation();
    boolean canDig();
}
