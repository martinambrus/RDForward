package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Boat extends org.bukkit.entity.Vehicle, io.papermc.paper.entity.Leashable {
    org.bukkit.TreeSpecies getWoodType();
    void setWoodType(org.bukkit.TreeSpecies arg0);
    org.bukkit.entity.Boat$Type getBoatType();
    void setBoatType(org.bukkit.entity.Boat$Type arg0);
    double getMaxSpeed();
    void setMaxSpeed(double arg0);
    double getOccupiedDeceleration();
    void setOccupiedDeceleration(double arg0);
    double getUnoccupiedDeceleration();
    void setUnoccupiedDeceleration(double arg0);
    boolean getWorkOnLand();
    void setWorkOnLand(boolean arg0);
    org.bukkit.entity.Boat$Status getStatus();
    org.bukkit.Material getBoatMaterial();
}
