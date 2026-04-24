package org.bukkit.potion;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class PotionEffect implements org.bukkit.configuration.serialization.ConfigurationSerializable {
    public static final int INFINITE_DURATION = -1;
    public PotionEffect(org.bukkit.potion.PotionEffectType arg0, int arg1, int arg2, boolean arg3, boolean arg4, boolean arg5, org.bukkit.potion.PotionEffect arg6) {}
    public PotionEffect(org.bukkit.potion.PotionEffectType arg0, int arg1, int arg2, boolean arg3, boolean arg4, boolean arg5) {}
    public PotionEffect(org.bukkit.potion.PotionEffectType arg0, int arg1, int arg2, boolean arg3, boolean arg4) {}
    public PotionEffect(org.bukkit.potion.PotionEffectType arg0, int arg1, int arg2, boolean arg3) {}
    public PotionEffect(org.bukkit.potion.PotionEffectType arg0, int arg1, int arg2) {}
    public PotionEffect(java.util.Map arg0) {}
    public PotionEffect() {}
    public org.bukkit.potion.PotionEffect withType(org.bukkit.potion.PotionEffectType arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.withType(Lorg/bukkit/potion/PotionEffectType;)Lorg/bukkit/potion/PotionEffect;");
        return this;
    }
    public org.bukkit.potion.PotionEffect withDuration(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.withDuration(I)Lorg/bukkit/potion/PotionEffect;");
        return this;
    }
    public org.bukkit.potion.PotionEffect withAmplifier(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.withAmplifier(I)Lorg/bukkit/potion/PotionEffect;");
        return this;
    }
    public org.bukkit.potion.PotionEffect withAmbient(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.withAmbient(Z)Lorg/bukkit/potion/PotionEffect;");
        return this;
    }
    public org.bukkit.potion.PotionEffect withParticles(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.withParticles(Z)Lorg/bukkit/potion/PotionEffect;");
        return this;
    }
    public org.bukkit.potion.PotionEffect withIcon(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.withIcon(Z)Lorg/bukkit/potion/PotionEffect;");
        return this;
    }
    public org.bukkit.potion.PotionEffect getHiddenPotionEffect() {
        return null;
    }
    public java.util.Map serialize() {
        return java.util.Collections.emptyMap();
    }
    public boolean apply(org.bukkit.entity.LivingEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.apply(Lorg/bukkit/entity/LivingEntity;)Z");
        return false;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int getAmplifier() {
        return 0;
    }
    public int getDuration() {
        return 0;
    }
    public boolean isInfinite() {
        return false;
    }
    public boolean isShorterThan(org.bukkit.potion.PotionEffect arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.potion.PotionEffect.isShorterThan(Lorg/bukkit/potion/PotionEffect;)Z");
        return false;
    }
    public org.bukkit.potion.PotionEffectType getType() {
        return null;
    }
    public boolean isAmbient() {
        return false;
    }
    public boolean hasParticles() {
        return false;
    }
    public org.bukkit.Color getColor() {
        return null;
    }
    public boolean hasIcon() {
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public java.lang.String toString() {
        return null;
    }
}
