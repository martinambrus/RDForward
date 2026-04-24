package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Panda extends org.bukkit.entity.Animals, org.bukkit.entity.Sittable {
    org.bukkit.entity.Panda$Gene getMainGene();
    void setMainGene(org.bukkit.entity.Panda$Gene arg0);
    org.bukkit.entity.Panda$Gene getHiddenGene();
    void setHiddenGene(org.bukkit.entity.Panda$Gene arg0);
    boolean isRolling();
    void setRolling(boolean arg0);
    boolean isSneezing();
    void setSneezing(boolean arg0);
    boolean isOnBack();
    void setOnBack(boolean arg0);
    boolean isEating();
    void setEating(boolean arg0);
    boolean isScared();
    int getUnhappyTicks();
    void setSneezeTicks(int arg0);
    int getSneezeTicks();
    void setEatingTicks(int arg0);
    int getEatingTicks();
    void setUnhappyTicks(int arg0);
    default void setIsOnBack(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Panda.setIsOnBack(Z)V");
    }
    default void setIsSitting(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.Panda.setIsSitting(Z)V");
    }
    org.bukkit.entity.Panda$Gene getCombinedGene();
}
