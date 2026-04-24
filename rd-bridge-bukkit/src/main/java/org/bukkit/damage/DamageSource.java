package org.bukkit.damage;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DamageSource {
    org.bukkit.damage.DamageType getDamageType();
    org.bukkit.entity.Entity getCausingEntity();
    org.bukkit.entity.Entity getDirectEntity();
    org.bukkit.Location getDamageLocation();
    org.bukkit.Location getSourceLocation();
    boolean isIndirect();
    float getFoodExhaustion();
    boolean scalesWithDifficulty();
    static org.bukkit.damage.DamageSource$Builder builder(org.bukkit.damage.DamageType arg0) {
        return null;
    }
}
