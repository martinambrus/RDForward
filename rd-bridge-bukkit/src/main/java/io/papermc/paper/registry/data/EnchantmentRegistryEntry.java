package io.papermc.paper.registry.data;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EnchantmentRegistryEntry {
    net.kyori.adventure.text.Component description();
    io.papermc.paper.registry.set.RegistryKeySet supportedItems();
    io.papermc.paper.registry.set.RegistryKeySet primaryItems();
    int weight();
    int maxLevel();
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$EnchantmentCost minimumCost();
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$EnchantmentCost maximumCost();
    int anvilCost();
    java.util.List activeSlots();
    io.papermc.paper.registry.set.RegistryKeySet exclusiveWith();
}
