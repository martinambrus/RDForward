package org.bukkit.potion;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PotionBrewer {
    void addPotionMix(io.papermc.paper.potion.PotionMix arg0);
    void removePotionMix(org.bukkit.NamespacedKey arg0);
    void resetPotionMixes();
    default org.bukkit.potion.PotionEffect createEffect(org.bukkit.potion.PotionEffectType arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionBrewer.createEffect(Lorg/bukkit/potion/PotionEffectType;II)Lorg/bukkit/potion/PotionEffect;");
        return null;
    }
    default java.util.Collection getEffectsFromDamage(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionBrewer.getEffectsFromDamage(I)Ljava/util/Collection;");
        return java.util.Collections.emptyList();
    }
    java.util.Collection getEffects(org.bukkit.potion.PotionType arg0, boolean arg1, boolean arg2);
}
