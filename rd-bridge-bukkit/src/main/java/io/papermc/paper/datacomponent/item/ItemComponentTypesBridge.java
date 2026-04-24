package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
interface ItemComponentTypesBridge {
    public static final java.util.Optional BRIDGE = java.util.Optional.empty();
    static io.papermc.paper.datacomponent.item.ItemComponentTypesBridge bridge() {
        return null;
    }
    io.papermc.paper.datacomponent.item.ChargedProjectiles$Builder chargedProjectiles();
    io.papermc.paper.datacomponent.item.PotDecorations$Builder potDecorations();
    io.papermc.paper.datacomponent.item.ItemLore$Builder lore();
    io.papermc.paper.datacomponent.item.ItemEnchantments$Builder enchantments();
    io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder modifiers();
    io.papermc.paper.datacomponent.item.FoodProperties$Builder food();
    io.papermc.paper.datacomponent.item.DyedItemColor$Builder dyedItemColor();
    io.papermc.paper.datacomponent.item.PotionContents$Builder potionContents();
    io.papermc.paper.datacomponent.item.BundleContents$Builder bundleContents();
    io.papermc.paper.datacomponent.item.SuspiciousStewEffects$Builder suspiciousStewEffects();
    io.papermc.paper.datacomponent.item.MapItemColor$Builder mapItemColor();
    io.papermc.paper.datacomponent.item.MapDecorations$Builder mapDecorations();
    io.papermc.paper.datacomponent.item.MapDecorations$DecorationEntry decorationEntry(org.bukkit.map.MapCursor$Type arg0, double arg1, double arg2, float arg3);
    io.papermc.paper.datacomponent.item.SeededContainerLoot$Builder seededContainerLoot(net.kyori.adventure.key.Key arg0);
    io.papermc.paper.datacomponent.item.WrittenBookContent$Builder writtenBookContent(io.papermc.paper.text.Filtered arg0, java.lang.String arg1);
    io.papermc.paper.datacomponent.item.WritableBookContent$Builder writeableBookContent();
    io.papermc.paper.datacomponent.item.ItemArmorTrim$Builder itemArmorTrim(org.bukkit.inventory.meta.trim.ArmorTrim arg0);
    io.papermc.paper.datacomponent.item.LodestoneTracker$Builder lodestoneTracker();
    io.papermc.paper.datacomponent.item.Fireworks$Builder fireworks();
    io.papermc.paper.datacomponent.item.ResolvableProfile$Builder resolvableProfile();
    io.papermc.paper.datacomponent.item.ResolvableProfile$SkinPatchBuilder skinPatch();
    io.papermc.paper.datacomponent.item.ResolvableProfile$SkinPatch emptySkinPatch();
    io.papermc.paper.datacomponent.item.ResolvableProfile resolvableProfile(com.destroystokyo.paper.profile.PlayerProfile arg0);
    io.papermc.paper.datacomponent.item.BannerPatternLayers$Builder bannerPatternLayers();
    io.papermc.paper.datacomponent.item.BlockItemDataProperties$Builder blockItemStateProperties();
    io.papermc.paper.datacomponent.item.ItemContainerContents$Builder itemContainerContents();
    io.papermc.paper.datacomponent.item.JukeboxPlayable$Builder jukeboxPlayable(org.bukkit.JukeboxSong arg0);
    io.papermc.paper.datacomponent.item.Tool$Builder tool();
    io.papermc.paper.datacomponent.item.Tool$Rule rule(io.papermc.paper.registry.set.RegistryKeySet arg0, java.lang.Float arg1, net.kyori.adventure.util.TriState arg2);
    io.papermc.paper.datacomponent.item.ItemAdventurePredicate$Builder itemAdventurePredicate();
    io.papermc.paper.datacomponent.item.CustomModelData$Builder customModelData();
    io.papermc.paper.datacomponent.item.MapId mapId(int arg0);
    io.papermc.paper.datacomponent.item.UseRemainder useRemainder(org.bukkit.inventory.ItemStack arg0);
    io.papermc.paper.datacomponent.item.Consumable$Builder consumable();
    io.papermc.paper.datacomponent.item.UseCooldown$Builder useCooldown(float arg0);
    io.papermc.paper.datacomponent.item.DamageResistant damageResistant(io.papermc.paper.registry.set.RegistryKeySet arg0);
    io.papermc.paper.datacomponent.item.Enchantable enchantable(int arg0);
    io.papermc.paper.datacomponent.item.Repairable repairable(io.papermc.paper.registry.set.RegistryKeySet arg0);
    io.papermc.paper.datacomponent.item.Equippable$Builder equippable(org.bukkit.inventory.EquipmentSlot arg0);
    io.papermc.paper.datacomponent.item.DeathProtection$Builder deathProtection();
    io.papermc.paper.datacomponent.item.OminousBottleAmplifier ominousBottleAmplifier(int arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder blocksAttacks();
    io.papermc.paper.datacomponent.item.TooltipDisplay$Builder tooltipDisplay();
    io.papermc.paper.datacomponent.item.Weapon$Builder weapon();
    io.papermc.paper.datacomponent.item.KineticWeapon$Builder kineticWeapon();
    io.papermc.paper.datacomponent.item.UseEffects$Builder useEffects();
    io.papermc.paper.datacomponent.item.PiercingWeapon$Builder piercingWeapon();
    io.papermc.paper.datacomponent.item.AttackRange$Builder attackRange();
    io.papermc.paper.datacomponent.item.SwingAnimation$Builder swingAnimation();
    io.papermc.paper.datacomponent.item.KineticWeapon$Condition kineticWeaponCondition(int arg0, float arg1, float arg2);
}
