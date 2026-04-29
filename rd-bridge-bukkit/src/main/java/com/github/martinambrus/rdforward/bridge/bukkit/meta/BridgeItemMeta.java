// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.meta;

import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Concrete {@link ItemMeta} backing used by {@link org.bukkit.inventory.ItemStack#getItemMeta()}.
 *
 * <p>Real Bukkit returns a CraftItemMeta instance with full state support;
 * RDForward's stub previously returned {@code null}, which broke any plugin
 * (Essentials's /skull, /book, /lore, etc.) that immediately calls a method
 * on the result. This class persists {@code displayName} + {@code lore} —
 * the only fields touched by current bridged plugins — and stubs every
 * other ItemMeta surface so the cast and method invocation succeed.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BridgeItemMeta implements ItemMeta, Cloneable {

    protected String displayName;
    protected List<String> lore;

    public boolean hasCustomName() { return false; }
    public net.kyori.adventure.text.Component customName() { return null; }
    public void customName(net.kyori.adventure.text.Component arg0) {}

    @Override public boolean hasDisplayName() { return displayName != null; }
    @Override public net.kyori.adventure.text.Component displayName() { return null; }
    @Override public void displayName(net.kyori.adventure.text.Component arg0) {}

    public String getDisplayName() { return displayName == null ? "" : displayName; }
    public net.md_5.bungee.api.chat.BaseComponent[] getDisplayNameComponent() {
        return new net.md_5.bungee.api.chat.BaseComponent[0];
    }
    public void setDisplayName(String arg0) { this.displayName = arg0; }
    public void setDisplayNameComponent(net.md_5.bungee.api.chat.BaseComponent[] arg0) {}

    public boolean hasItemName() { return false; }
    public net.kyori.adventure.text.Component itemName() { return null; }
    public void itemName(net.kyori.adventure.text.Component arg0) {}
    public String getItemName() { return ""; }
    public void setItemName(String arg0) {}

    public boolean hasLocalizedName() { return false; }
    public String getLocalizedName() { return ""; }
    public void setLocalizedName(String arg0) {}

    public boolean hasLore() { return lore != null && !lore.isEmpty(); }
    public List lore() { return null; }
    public void lore(List arg0) {}
    public List getLore() { return lore == null ? null : new ArrayList<>(lore); }
    public List getLoreComponents() { return null; }
    public void setLore(List arg0) { this.lore = arg0 == null ? null : new ArrayList<>(arg0); }
    public void setLoreComponents(List arg0) {}

    public boolean hasCustomModelData() { return false; }
    public int getCustomModelData() { return 0; }
    public org.bukkit.inventory.meta.components.CustomModelDataComponent getCustomModelDataComponent() { return null; }
    public void setCustomModelData(Integer arg0) {}
    public boolean hasCustomModelDataComponent() { return false; }
    public void setCustomModelDataComponent(org.bukkit.inventory.meta.components.CustomModelDataComponent arg0) {}

    public boolean hasEnchantable() { return false; }
    public int getEnchantable() { return 0; }
    public void setEnchantable(Integer arg0) {}

    public boolean hasEnchants() { return false; }
    public boolean hasEnchant(org.bukkit.enchantments.Enchantment arg0) { return false; }
    public int getEnchantLevel(org.bukkit.enchantments.Enchantment arg0) { return 0; }
    public Map getEnchants() { return Collections.emptyMap(); }
    public boolean addEnchant(org.bukkit.enchantments.Enchantment arg0, int arg1, boolean arg2) { return false; }
    public boolean removeEnchant(org.bukkit.enchantments.Enchantment arg0) { return false; }
    public void removeEnchantments() {}
    public boolean hasConflictingEnchant(org.bukkit.enchantments.Enchantment arg0) { return false; }

    public void addItemFlags(org.bukkit.inventory.ItemFlag... arg0) {}
    public void removeItemFlags(org.bukkit.inventory.ItemFlag... arg0) {}
    public Set getItemFlags() { return Collections.emptySet(); }
    public boolean hasItemFlag(org.bukkit.inventory.ItemFlag arg0) { return false; }

    public boolean isHideTooltip() { return false; }
    public void setHideTooltip(boolean arg0) {}

    public boolean hasTooltipStyle() { return false; }
    public org.bukkit.NamespacedKey getTooltipStyle() { return null; }
    public void setTooltipStyle(org.bukkit.NamespacedKey arg0) {}

    public boolean hasItemModel() { return false; }
    public org.bukkit.NamespacedKey getItemModel() { return null; }
    public void setItemModel(org.bukkit.NamespacedKey arg0) {}

    public boolean isUnbreakable() { return false; }
    public void setUnbreakable(boolean arg0) {}

    public boolean hasEnchantmentGlintOverride() { return false; }
    public Boolean getEnchantmentGlintOverride() { return null; }
    public void setEnchantmentGlintOverride(Boolean arg0) {}

    public boolean isGlider() { return false; }
    public void setGlider(boolean arg0) {}

    public boolean isFireResistant() { return false; }
    public void setFireResistant(boolean arg0) {}

    public boolean hasDamageResistant() { return false; }
    public org.bukkit.Tag getDamageResistant() { return null; }
    public void setDamageResistant(org.bukkit.Tag arg0) {}
    public io.papermc.paper.registry.set.RegistryKeySet getDamageResistantTypes() { return null; }
    public void setDamageResistantTypes(io.papermc.paper.registry.set.RegistryKeySet arg0) {}

    public boolean hasMaxStackSize() { return false; }
    public int getMaxStackSize() { return 0; }
    public void setMaxStackSize(Integer arg0) {}

    public boolean hasRarity() { return false; }
    public org.bukkit.inventory.ItemRarity getRarity() { return null; }
    public void setRarity(org.bukkit.inventory.ItemRarity arg0) {}

    public boolean hasUseRemainder() { return false; }
    public org.bukkit.inventory.ItemStack getUseRemainder() { return null; }
    public void setUseRemainder(org.bukkit.inventory.ItemStack arg0) {}

    public boolean hasUseCooldown() { return false; }
    public org.bukkit.inventory.meta.components.UseCooldownComponent getUseCooldown() { return null; }
    public void setUseCooldown(org.bukkit.inventory.meta.components.UseCooldownComponent arg0) {}

    public boolean hasFood() { return false; }
    public org.bukkit.inventory.meta.components.FoodComponent getFood() { return null; }
    public void setFood(org.bukkit.inventory.meta.components.FoodComponent arg0) {}

    public boolean hasTool() { return false; }
    public org.bukkit.inventory.meta.components.ToolComponent getTool() { return null; }
    public void setTool(org.bukkit.inventory.meta.components.ToolComponent arg0) {}

    public boolean hasEquippable() { return false; }
    public org.bukkit.inventory.meta.components.EquippableComponent getEquippable() { return null; }
    public void setEquippable(org.bukkit.inventory.meta.components.EquippableComponent arg0) {}

    public boolean hasJukeboxPlayable() { return false; }
    public org.bukkit.inventory.meta.components.JukeboxPlayableComponent getJukeboxPlayable() { return null; }
    public void setJukeboxPlayable(org.bukkit.inventory.meta.components.JukeboxPlayableComponent arg0) {}

    public boolean hasAttributeModifiers() { return false; }
    public com.google.common.collect.Multimap getAttributeModifiers() { return null; }
    public com.google.common.collect.Multimap getAttributeModifiers(org.bukkit.inventory.EquipmentSlot arg0) { return null; }
    public Collection getAttributeModifiers(org.bukkit.attribute.Attribute arg0) { return null; }
    public boolean addAttributeModifier(org.bukkit.attribute.Attribute arg0, org.bukkit.attribute.AttributeModifier arg1) { return false; }
    public void setAttributeModifiers(com.google.common.collect.Multimap arg0) {}
    public boolean removeAttributeModifier(org.bukkit.attribute.Attribute arg0) { return false; }
    public boolean removeAttributeModifier(org.bukkit.inventory.EquipmentSlot arg0) { return false; }
    public boolean removeAttributeModifier(org.bukkit.attribute.Attribute arg0, org.bukkit.attribute.AttributeModifier arg1) { return false; }

    public String getAsString() { return ""; }
    public String getAsComponentString() { return ""; }
    public org.bukkit.inventory.meta.tags.CustomItemTagContainer getCustomTagContainer() { return null; }
    public void setVersion(int arg0) {}

    public Set getCanDestroy() { return Collections.emptySet(); }
    public void setCanDestroy(Set arg0) {}
    public Set getCanPlaceOn() { return Collections.emptySet(); }
    public void setCanPlaceOn(Set arg0) {}
    public Set getDestroyableKeys() { return Collections.emptySet(); }
    public void setDestroyableKeys(Collection arg0) {}
    public Set getPlaceableKeys() { return Collections.emptySet(); }
    public void setPlaceableKeys(Collection arg0) {}
    public boolean hasPlaceableKeys() { return false; }
    public boolean hasDestroyableKeys() { return false; }

    public Map serialize() { return Collections.emptyMap(); }

    /** Returns null — RDForward's persistent-data container is not modelled
     *  for ItemStack. Plugins that read PDC see "no data", which matches
     *  the behaviour of an unmodified vanilla item. */
    public org.bukkit.persistence.PersistentDataContainer getPersistentDataContainer() { return null; }

    @Override
    public ItemMeta clone() {
        try {
            BridgeItemMeta copy = (BridgeItemMeta) super.clone();
            if (this.lore != null) copy.lore = new ArrayList<>(this.lore);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}
