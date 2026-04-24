package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Ageable extends org.bukkit.entity.Creature {
    int getAge();
    void setAge(int arg0);
    void setAgeLock(boolean arg0);
    boolean getAgeLock();
    void setBaby();
    void setAdult();
    boolean isAdult();
    boolean canBreed();
    void setBreed(boolean arg0);
}
