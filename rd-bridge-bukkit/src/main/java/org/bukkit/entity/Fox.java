package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Fox extends org.bukkit.entity.Animals, org.bukkit.entity.Sittable {
    org.bukkit.entity.Fox$Type getFoxType();
    void setFoxType(org.bukkit.entity.Fox$Type arg0);
    boolean isCrouching();
    void setCrouching(boolean arg0);
    void setSleeping(boolean arg0);
    org.bukkit.entity.AnimalTamer getFirstTrustedPlayer();
    void setFirstTrustedPlayer(org.bukkit.entity.AnimalTamer arg0);
    org.bukkit.entity.AnimalTamer getSecondTrustedPlayer();
    void setSecondTrustedPlayer(org.bukkit.entity.AnimalTamer arg0);
    boolean isFaceplanted();
    void setInterested(boolean arg0);
    boolean isInterested();
    void setLeaping(boolean arg0);
    boolean isLeaping();
    void setDefending(boolean arg0);
    boolean isDefending();
    void setFaceplanted(boolean arg0);
}
