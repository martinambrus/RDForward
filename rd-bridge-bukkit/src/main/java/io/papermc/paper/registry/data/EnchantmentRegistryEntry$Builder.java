package io.papermc.paper.registry.data;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EnchantmentRegistryEntry$Builder extends io.papermc.paper.registry.data.EnchantmentRegistryEntry, io.papermc.paper.registry.RegistryBuilder {
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder description(net.kyori.adventure.text.Component arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder supportedItems(io.papermc.paper.registry.set.RegistryKeySet arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder primaryItems(io.papermc.paper.registry.set.RegistryKeySet arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder weight(int arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder maxLevel(int arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder minimumCost(io.papermc.paper.registry.data.EnchantmentRegistryEntry$EnchantmentCost arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder maximumCost(io.papermc.paper.registry.data.EnchantmentRegistryEntry$EnchantmentCost arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder anvilCost(int arg0);
    default io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder activeSlots(org.bukkit.inventory.EquipmentSlotGroup[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder.activeSlots([Lorg/bukkit/inventory/EquipmentSlotGroup;)Lio/papermc/paper/registry/data/EnchantmentRegistryEntry$Builder;");
        return this;
    }
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder activeSlots(java.lang.Iterable arg0);
    io.papermc.paper.registry.data.EnchantmentRegistryEntry$Builder exclusiveWith(io.papermc.paper.registry.set.RegistryKeySet arg0);
}
