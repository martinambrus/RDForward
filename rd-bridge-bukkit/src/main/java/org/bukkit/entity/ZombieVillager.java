package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ZombieVillager extends org.bukkit.entity.Zombie {
    void setVillagerProfession(org.bukkit.entity.Villager$Profession arg0);
    org.bukkit.entity.Villager$Profession getVillagerProfession();
    org.bukkit.entity.Villager$Type getVillagerType();
    void setVillagerType(org.bukkit.entity.Villager$Type arg0);
    boolean isConverting();
    int getConversionTime();
    void setConversionTime(int arg0);
    org.bukkit.OfflinePlayer getConversionPlayer();
    void setConversionPlayer(org.bukkit.OfflinePlayer arg0);
    void setConversionTime(int arg0, boolean arg1);
}
