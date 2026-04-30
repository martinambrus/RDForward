// @rdforward:preserve - hand-tuned: builder() must return non-null so that
// plugin probes like Essentials's ModernDamageEventProvider can build a
// sample DamageSource at startup without NPE.
package org.bukkit.damage;

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

    static org.bukkit.damage.DamageSource$Builder builder(org.bukkit.damage.DamageType type) {
        return new BuilderImpl(type);
    }

    /** In-source builder. Stores the optional witnessing/source entities and
     *  location; {@link #build()} returns a {@link SourceImpl} snapshot. */
    final class BuilderImpl implements org.bukkit.damage.DamageSource$Builder {
        private final org.bukkit.damage.DamageType type;
        private org.bukkit.entity.Entity causing;
        private org.bukkit.entity.Entity direct;
        private org.bukkit.Location location;

        BuilderImpl(org.bukkit.damage.DamageType type) { this.type = type; }

        @Override public org.bukkit.damage.DamageSource$Builder withCausingEntity(org.bukkit.entity.Entity e) { this.causing = e; return this; }
        @Override public org.bukkit.damage.DamageSource$Builder withDirectEntity(org.bukkit.entity.Entity e) { this.direct = e; return this; }
        @Override public org.bukkit.damage.DamageSource$Builder withDamageLocation(org.bukkit.Location l) { this.location = l; return this; }
        @Override public org.bukkit.damage.DamageSource build() { return new SourceImpl(type, causing, direct, location); }
    }

    /** Immutable record of a built DamageSource. Defaults align with real
     *  Bukkit semantics where unknown: indirect=false, foodExhaustion=0,
     *  scalesWithDifficulty=false. */
    final class SourceImpl implements org.bukkit.damage.DamageSource {
        private final org.bukkit.damage.DamageType type;
        private final org.bukkit.entity.Entity causing;
        private final org.bukkit.entity.Entity direct;
        private final org.bukkit.Location location;

        SourceImpl(org.bukkit.damage.DamageType type,
                   org.bukkit.entity.Entity causing,
                   org.bukkit.entity.Entity direct,
                   org.bukkit.Location location) {
            this.type = type;
            this.causing = causing;
            this.direct = direct;
            this.location = location;
        }

        @Override public org.bukkit.damage.DamageType getDamageType() { return type; }
        @Override public org.bukkit.entity.Entity getCausingEntity() { return causing; }
        @Override public org.bukkit.entity.Entity getDirectEntity() { return direct; }
        @Override public org.bukkit.Location getDamageLocation() { return location; }
        @Override public org.bukkit.Location getSourceLocation() { return location; }
        @Override public boolean isIndirect() { return causing != null && causing != direct; }
        @Override public float getFoodExhaustion() { return 0f; }
        @Override public boolean scalesWithDifficulty() { return false; }
    }
}
