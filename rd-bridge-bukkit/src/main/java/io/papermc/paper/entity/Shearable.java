package io.papermc.paper.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Shearable extends org.bukkit.entity.Entity {
    default void shear() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.entity.Shearable.shear()V");
    }
    void shear(net.kyori.adventure.sound.Sound$Source arg0);
    boolean readyToBeSheared();
}
