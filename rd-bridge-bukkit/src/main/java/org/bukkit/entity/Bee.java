package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Bee extends org.bukkit.entity.Animals {
    org.bukkit.Location getHive();
    void setHive(org.bukkit.Location arg0);
    org.bukkit.Location getFlower();
    void setFlower(org.bukkit.Location arg0);
    boolean hasNectar();
    void setHasNectar(boolean arg0);
    boolean hasStung();
    void setHasStung(boolean arg0);
    int getAnger();
    void setAnger(int arg0);
    int getCannotEnterHiveTicks();
    void setCannotEnterHiveTicks(int arg0);
    void setRollingOverride(net.kyori.adventure.util.TriState arg0);
    net.kyori.adventure.util.TriState getRollingOverride();
    boolean isRolling();
    void setCropsGrownSincePollination(int arg0);
    int getCropsGrownSincePollination();
    void setTicksSincePollination(int arg0);
    int getTicksSincePollination();
    void setTimeSinceSting(int arg0);
    int getTimeSinceSting();
}
