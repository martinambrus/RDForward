package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Wolf extends org.bukkit.entity.Tameable, org.bukkit.entity.Sittable, io.papermc.paper.entity.CollarColorable {
    boolean isAngry();
    void setAngry(boolean arg0);
    org.bukkit.DyeColor getCollarColor();
    void setCollarColor(org.bukkit.DyeColor arg0);
    boolean isWet();
    float getTailAngle();
    boolean isInterested();
    void setInterested(boolean arg0);
    org.bukkit.entity.Wolf$Variant getVariant();
    void setVariant(org.bukkit.entity.Wolf$Variant arg0);
    org.bukkit.entity.Wolf$SoundVariant getSoundVariant();
    void setSoundVariant(org.bukkit.entity.Wolf$SoundVariant arg0);
}
