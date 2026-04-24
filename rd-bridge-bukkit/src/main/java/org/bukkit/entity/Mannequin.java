package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Mannequin extends org.bukkit.entity.LivingEntity {
    static java.util.Set validPoses() {
        return java.util.Collections.emptySet();
    }
    static io.papermc.paper.datacomponent.item.ResolvableProfile defaultProfile() {
        return null;
    }
    static net.kyori.adventure.text.Component defaultDescription() {
        return null;
    }
    io.papermc.paper.datacomponent.item.ResolvableProfile getProfile();
    void setProfile(io.papermc.paper.datacomponent.item.ResolvableProfile arg0);
    com.destroystokyo.paper.SkinParts$Mutable getSkinParts();
    void setSkinParts(com.destroystokyo.paper.SkinParts arg0);
    boolean isImmovable();
    void setImmovable(boolean arg0);
    net.kyori.adventure.text.Component getDescription();
    void setDescription(net.kyori.adventure.text.Component arg0);
    org.bukkit.inventory.MainHand getMainHand();
    void setMainHand(org.bukkit.inventory.MainHand arg0);
    org.bukkit.inventory.EntityEquipment getEquipment();
}
