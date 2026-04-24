package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Equippable extends io.papermc.paper.datacomponent.BuildableDataComponent {
    static io.papermc.paper.datacomponent.item.Equippable$Builder equippable(org.bukkit.inventory.EquipmentSlot arg0) {
        return null;
    }
    org.bukkit.inventory.EquipmentSlot slot();
    net.kyori.adventure.key.Key equipSound();
    net.kyori.adventure.key.Key assetId();
    net.kyori.adventure.key.Key cameraOverlay();
    io.papermc.paper.registry.set.RegistryKeySet allowedEntities();
    boolean dispensable();
    boolean swappable();
    boolean damageOnHurt();
    boolean equipOnInteract();
    boolean canBeSheared();
    net.kyori.adventure.key.Key shearSound();
}
