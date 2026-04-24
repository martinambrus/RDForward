package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MenuType extends org.bukkit.Keyed, io.papermc.paper.world.flag.FeatureDependant {
    public static final org.bukkit.inventory.MenuType$Typed GENERIC_9X1 = null;
    public static final org.bukkit.inventory.MenuType$Typed GENERIC_9X2 = null;
    public static final org.bukkit.inventory.MenuType$Typed GENERIC_9X3 = null;
    public static final org.bukkit.inventory.MenuType$Typed GENERIC_9X4 = null;
    public static final org.bukkit.inventory.MenuType$Typed GENERIC_9X5 = null;
    public static final org.bukkit.inventory.MenuType$Typed GENERIC_9X6 = null;
    public static final org.bukkit.inventory.MenuType$Typed GENERIC_3X3 = null;
    public static final org.bukkit.inventory.MenuType$Typed CRAFTER_3X3 = null;
    public static final org.bukkit.inventory.MenuType$Typed ANVIL = null;
    public static final org.bukkit.inventory.MenuType$Typed BEACON = null;
    public static final org.bukkit.inventory.MenuType$Typed BLAST_FURNACE = null;
    public static final org.bukkit.inventory.MenuType$Typed BREWING_STAND = null;
    public static final org.bukkit.inventory.MenuType$Typed CRAFTING = null;
    public static final org.bukkit.inventory.MenuType$Typed ENCHANTMENT = null;
    public static final org.bukkit.inventory.MenuType$Typed FURNACE = null;
    public static final org.bukkit.inventory.MenuType$Typed GRINDSTONE = null;
    public static final org.bukkit.inventory.MenuType$Typed HOPPER = null;
    public static final org.bukkit.inventory.MenuType$Typed LECTERN = null;
    public static final org.bukkit.inventory.MenuType$Typed LOOM = null;
    public static final org.bukkit.inventory.MenuType$Typed MERCHANT = null;
    public static final org.bukkit.inventory.MenuType$Typed SHULKER_BOX = null;
    public static final org.bukkit.inventory.MenuType$Typed SMITHING = null;
    public static final org.bukkit.inventory.MenuType$Typed SMOKER = null;
    public static final org.bukkit.inventory.MenuType$Typed CARTOGRAPHY_TABLE = null;
    public static final org.bukkit.inventory.MenuType$Typed STONECUTTER = null;
    org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity arg0, net.kyori.adventure.text.Component arg1);
    org.bukkit.inventory.MenuType$Typed typed();
    org.bukkit.inventory.MenuType$Typed typed(java.lang.Class arg0) throws java.lang.IllegalArgumentException;
    java.lang.Class getInventoryViewClass();
}
