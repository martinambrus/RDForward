package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Horse extends org.bukkit.entity.AbstractHorse {
    org.bukkit.entity.Horse$Color getColor();
    void setColor(org.bukkit.entity.Horse$Color arg0);
    org.bukkit.entity.Horse$Style getStyle();
    void setStyle(org.bukkit.entity.Horse$Style arg0);
    boolean isCarryingChest();
    void setCarryingChest(boolean arg0);
    org.bukkit.inventory.HorseInventory getInventory();
}
