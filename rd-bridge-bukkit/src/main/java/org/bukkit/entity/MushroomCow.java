package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MushroomCow extends org.bukkit.entity.AbstractCow, io.papermc.paper.entity.Shearable {
    boolean hasEffectsForNextStew();
    java.util.List getEffectsForNextStew();
    boolean addEffectToNextStew(org.bukkit.potion.PotionEffect arg0, boolean arg1);
    boolean addEffectToNextStew(io.papermc.paper.potion.SuspiciousEffectEntry arg0, boolean arg1);
    boolean removeEffectFromNextStew(org.bukkit.potion.PotionEffectType arg0);
    boolean hasEffectForNextStew(org.bukkit.potion.PotionEffectType arg0);
    void clearEffectsForNextStew();
    org.bukkit.entity.MushroomCow$Variant getVariant();
    void setVariant(org.bukkit.entity.MushroomCow$Variant arg0);
    default int getStewEffectDuration() {
        return 0;
    }
    default void setStewEffectDuration(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.MushroomCow.setStewEffectDuration(I)V");
    }
    default org.bukkit.potion.PotionEffectType getStewEffectType() {
        return null;
    }
    default void setStewEffect(org.bukkit.potion.PotionEffectType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.entity.MushroomCow.setStewEffect(Lorg/bukkit/potion/PotionEffectType;)V");
    }
    java.util.List getStewEffects();
    void setStewEffects(java.util.List arg0);
}
