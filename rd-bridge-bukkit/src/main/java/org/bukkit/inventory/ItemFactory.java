package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ItemFactory {
    org.bukkit.inventory.meta.ItemMeta getItemMeta(org.bukkit.Material arg0);
    boolean isApplicable(org.bukkit.inventory.meta.ItemMeta arg0, org.bukkit.inventory.ItemStack arg1) throws java.lang.IllegalArgumentException;
    boolean isApplicable(org.bukkit.inventory.meta.ItemMeta arg0, org.bukkit.Material arg1) throws java.lang.IllegalArgumentException;
    boolean equals(org.bukkit.inventory.meta.ItemMeta arg0, org.bukkit.inventory.meta.ItemMeta arg1) throws java.lang.IllegalArgumentException;
    org.bukkit.inventory.meta.ItemMeta asMetaFor(org.bukkit.inventory.meta.ItemMeta arg0, org.bukkit.inventory.ItemStack arg1) throws java.lang.IllegalArgumentException;
    org.bukkit.inventory.meta.ItemMeta asMetaFor(org.bukkit.inventory.meta.ItemMeta arg0, org.bukkit.Material arg1) throws java.lang.IllegalArgumentException;
    org.bukkit.Color getDefaultLeatherColor();
    org.bukkit.inventory.ItemStack createItemStack(java.lang.String arg0) throws java.lang.IllegalArgumentException;
    org.bukkit.Material getSpawnEgg(org.bukkit.entity.EntityType arg0);
    org.bukkit.inventory.ItemStack enchantItem(org.bukkit.entity.Entity arg0, org.bukkit.inventory.ItemStack arg1, int arg2, boolean arg3);
    org.bukkit.inventory.ItemStack enchantItem(org.bukkit.World arg0, org.bukkit.inventory.ItemStack arg1, int arg2, boolean arg3);
    org.bukkit.inventory.ItemStack enchantItem(org.bukkit.inventory.ItemStack arg0, int arg1, boolean arg2);
    net.kyori.adventure.text.event.HoverEvent asHoverEvent(org.bukkit.inventory.ItemStack arg0, java.util.function.UnaryOperator arg1);
    net.kyori.adventure.text.Component displayName(org.bukkit.inventory.ItemStack arg0);
    java.lang.String getI18NDisplayName(org.bukkit.inventory.ItemStack arg0);
    org.bukkit.inventory.ItemStack ensureServerConversions(org.bukkit.inventory.ItemStack arg0);
    net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.inventory.ItemStack arg0);
    net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity arg0);
    net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity arg0, java.lang.String arg1);
    net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity arg0, net.md_5.bungee.api.chat.BaseComponent arg1);
    net.md_5.bungee.api.chat.hover.content.Content hoverContentOf(org.bukkit.entity.Entity arg0, net.md_5.bungee.api.chat.BaseComponent[] arg1);
    org.bukkit.inventory.ItemStack enchantWithLevels(org.bukkit.inventory.ItemStack arg0, int arg1, boolean arg2, java.util.Random arg3);
    org.bukkit.inventory.ItemStack enchantWithLevels(org.bukkit.inventory.ItemStack arg0, int arg1, io.papermc.paper.registry.set.RegistryKeySet arg2, java.util.Random arg3);
}
