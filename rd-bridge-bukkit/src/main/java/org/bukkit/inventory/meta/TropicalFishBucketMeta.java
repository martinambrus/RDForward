package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TropicalFishBucketMeta extends org.bukkit.inventory.meta.ItemMeta {
    org.bukkit.DyeColor getPatternColor();
    void setPatternColor(org.bukkit.DyeColor arg0);
    org.bukkit.DyeColor getBodyColor();
    void setBodyColor(org.bukkit.DyeColor arg0);
    org.bukkit.entity.TropicalFish$Pattern getPattern();
    void setPattern(org.bukkit.entity.TropicalFish$Pattern arg0);
    boolean hasVariant();
    org.bukkit.inventory.meta.TropicalFishBucketMeta clone();
}
