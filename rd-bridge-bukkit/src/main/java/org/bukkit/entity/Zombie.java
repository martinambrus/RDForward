package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Zombie extends org.bukkit.entity.Monster, org.bukkit.entity.Ageable {
    boolean isBaby();
    void setBaby(boolean arg0);
    boolean isVillager();
    void setVillager(boolean arg0);
    void setVillagerProfession(org.bukkit.entity.Villager$Profession arg0);
    org.bukkit.entity.Villager$Profession getVillagerProfession();
    boolean isConverting();
    int getConversionTime();
    void setConversionTime(int arg0);
    boolean canBreakDoors();
    void setCanBreakDoors(boolean arg0);
    boolean isDrowning();
    void startDrowning(int arg0);
    void stopDrowning();
    void setArmsRaised(boolean arg0);
    boolean isArmsRaised();
    boolean shouldBurnInDay();
    void setShouldBurnInDay(boolean arg0);
    default boolean supportsBreakingDoors() {
        return false;
    }
}
