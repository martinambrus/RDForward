package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Cat extends org.bukkit.entity.Tameable, org.bukkit.entity.Sittable, io.papermc.paper.entity.CollarColorable {
    org.bukkit.entity.Cat$Type getCatType();
    void setCatType(org.bukkit.entity.Cat$Type arg0);
    org.bukkit.entity.Cat$SoundVariant getSoundVariant();
    void setSoundVariant(org.bukkit.entity.Cat$SoundVariant arg0);
    org.bukkit.DyeColor getCollarColor();
    void setCollarColor(org.bukkit.DyeColor arg0);
    void setLyingDown(boolean arg0);
    boolean isLyingDown();
    void setHeadUp(boolean arg0);
    boolean isHeadUp();
}
