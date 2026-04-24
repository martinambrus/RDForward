package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Villager extends org.bukkit.entity.AbstractVillager {
    org.bukkit.entity.Villager$Profession getProfession();
    void setProfession(org.bukkit.entity.Villager$Profession arg0);
    org.bukkit.entity.Villager$Type getVillagerType();
    void setVillagerType(org.bukkit.entity.Villager$Type arg0);
    int getVillagerLevel();
    void setVillagerLevel(int arg0);
    int getVillagerExperience();
    void setVillagerExperience(int arg0);
    boolean increaseLevel(int arg0);
    boolean addTrades(int arg0);
    int getRestocksToday();
    void setRestocksToday(int arg0);
    boolean sleep(org.bukkit.Location arg0);
    void wakeup();
    void shakeHead();
    org.bukkit.entity.ZombieVillager zombify();
    com.destroystokyo.paper.entity.villager.Reputation getReputation(java.util.UUID arg0);
    java.util.Map getReputations();
    void setReputation(java.util.UUID arg0, com.destroystokyo.paper.entity.villager.Reputation arg1);
    void setReputations(java.util.Map arg0);
    void clearReputations();
    void updateDemand();
    void restock();
}
