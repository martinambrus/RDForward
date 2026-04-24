package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface UnsafeValues {
    net.kyori.adventure.text.flattener.ComponentFlattener componentFlattener();
    net.kyori.adventure.text.serializer.plain.PlainComponentSerializer plainComponentSerializer();
    net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainTextSerializer();
    net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gsonComponentSerializer();
    net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGsonComponentSerializer();
    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyComponentSerializer();
    net.kyori.adventure.text.Component resolveWithContext(net.kyori.adventure.text.Component arg0, org.bukkit.command.CommandSender arg1, org.bukkit.entity.Entity arg2, boolean arg3) throws java.io.IOException;
    org.bukkit.Material toLegacy(org.bukkit.Material arg0);
    org.bukkit.Material fromLegacy(org.bukkit.Material arg0);
    org.bukkit.Material fromLegacy(org.bukkit.material.MaterialData arg0);
    org.bukkit.Material fromLegacy(org.bukkit.material.MaterialData arg0, boolean arg1);
    org.bukkit.block.data.BlockData fromLegacy(org.bukkit.Material arg0, byte arg1);
    org.bukkit.Material getMaterial(java.lang.String arg0, int arg1);
    int getDataVersion();
    org.bukkit.inventory.ItemStack modifyItemStack(org.bukkit.inventory.ItemStack arg0, java.lang.String arg1);
    void checkSupported(org.bukkit.plugin.PluginDescriptionFile arg0) throws org.bukkit.plugin.InvalidPluginException;
    byte[] processClass(org.bukkit.plugin.PluginDescriptionFile arg0, java.lang.String arg1, byte[] arg2);
    org.bukkit.advancement.Advancement loadAdvancement(org.bukkit.NamespacedKey arg0, java.lang.String arg1);
    boolean removeAdvancement(org.bukkit.NamespacedKey arg0);
    com.google.common.collect.Multimap getDefaultAttributeModifiers(org.bukkit.Material arg0, org.bukkit.inventory.EquipmentSlot arg1);
    org.bukkit.inventory.CreativeCategory getCreativeCategory(org.bukkit.Material arg0);
    java.lang.String getBlockTranslationKey(org.bukkit.Material arg0);
    java.lang.String getItemTranslationKey(org.bukkit.Material arg0);
    java.lang.String getTranslationKey(org.bukkit.entity.EntityType arg0);
    java.lang.String getTranslationKey(org.bukkit.inventory.ItemStack arg0);
    java.lang.String getTranslationKey(org.bukkit.attribute.Attribute arg0);
    org.bukkit.potion.PotionType$InternalPotionData getInternalPotionData(org.bukkit.NamespacedKey arg0);
    org.bukkit.damage.DamageSource$Builder createDamageSourceBuilder(org.bukkit.damage.DamageType arg0);
    java.lang.String get(java.lang.Class arg0, java.lang.String arg1);
    org.bukkit.Keyed get(io.papermc.paper.registry.RegistryKey arg0, org.bukkit.NamespacedKey arg1);
    boolean isSupportedApiVersion(java.lang.String arg0);
    static boolean isLegacyPlugin(org.bukkit.plugin.Plugin arg0) {
        return false;
    }
    default com.destroystokyo.paper.util.VersionFetcher getVersionFetcher() {
        return null;
    }
    byte[] serializeItem(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack deserializeItem(byte[] arg0);
    com.google.gson.JsonObject serializeItemAsJson(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack deserializeItemFromJson(com.google.gson.JsonObject arg0) throws java.lang.IllegalArgumentException;
    default byte[] serializeEntity(org.bukkit.entity.Entity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.UnsafeValues.serializeEntity(Lorg/bukkit/entity/Entity;)[B");
        return new byte[0];
    }
    byte[] serializeEntity(org.bukkit.entity.Entity arg0, io.papermc.paper.entity.EntitySerializationFlag[] arg1);
    default org.bukkit.entity.Entity deserializeEntity(byte[] arg0, org.bukkit.World arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.UnsafeValues.deserializeEntity([BLorg/bukkit/World;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    default org.bukkit.entity.Entity deserializeEntity(byte[] arg0, org.bukkit.World arg1, boolean arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.UnsafeValues.deserializeEntity([BLorg/bukkit/World;Z)Lorg/bukkit/entity/Entity;");
        return null;
    }
    org.bukkit.entity.Entity deserializeEntity(byte[] arg0, org.bukkit.World arg1, boolean arg2, boolean arg3);
    int nextEntityId();
    java.lang.String getMainLevelName();
    int getProtocolVersion();
    boolean isValidRepairItemStack(org.bukkit.inventory.ItemStack arg0, org.bukkit.inventory.ItemStack arg1);
    boolean hasDefaultEntityAttributes(org.bukkit.NamespacedKey arg0);
    org.bukkit.attribute.Attributable getDefaultEntityAttributes(org.bukkit.NamespacedKey arg0);
    org.bukkit.NamespacedKey getBiomeKey(org.bukkit.RegionAccessor arg0, int arg1, int arg2, int arg3);
    void setBiomeKey(org.bukkit.RegionAccessor arg0, int arg1, int arg2, int arg3, org.bukkit.NamespacedKey arg4);
    java.lang.String getStatisticCriteriaKey(org.bukkit.Statistic arg0);
    org.bukkit.Color getSpawnEggLayerColor(org.bukkit.entity.EntityType arg0, int arg1);
    io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager createPluginLifecycleEventManager(org.bukkit.plugin.java.JavaPlugin arg0, java.util.function.BooleanSupplier arg1);
    java.util.List computeTooltipLines(org.bukkit.inventory.ItemStack arg0, io.papermc.paper.inventory.tooltip.TooltipContext arg1, org.bukkit.entity.Player arg2);
    org.bukkit.inventory.ItemStack createEmptyStack();
    java.util.Map serializeStack(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack deserializeStack(java.util.Map arg0);
    org.bukkit.inventory.ItemStack deserializeItemHover(net.kyori.adventure.text.event.HoverEvent$ShowItem arg0);
}
