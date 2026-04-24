package org.bukkit.advancement;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AdvancementDisplay {
    java.lang.String getTitle();
    java.lang.String getDescription();
    org.bukkit.inventory.ItemStack getIcon();
    boolean shouldShowToast();
    boolean shouldAnnounceChat();
    boolean isHidden();
    float getX();
    float getY();
    org.bukkit.advancement.AdvancementDisplayType getType();
}
