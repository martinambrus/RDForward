package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Inventory extends java.lang.Iterable {
    int getSize();
    int getMaxStackSize();
    void setMaxStackSize(int arg0);
    org.bukkit.inventory.ItemStack getItem(int arg0);
    void setItem(int arg0, org.bukkit.inventory.ItemStack arg1);
    java.util.HashMap addItem(org.bukkit.inventory.ItemStack[] arg0) throws java.lang.IllegalArgumentException;
    java.util.HashMap removeItem(org.bukkit.inventory.ItemStack[] arg0) throws java.lang.IllegalArgumentException;
    java.util.HashMap removeItemAnySlot(org.bukkit.inventory.ItemStack[] arg0) throws java.lang.IllegalArgumentException;
    org.bukkit.inventory.ItemStack[] getContents();
    void setContents(org.bukkit.inventory.ItemStack[] arg0) throws java.lang.IllegalArgumentException;
    org.bukkit.inventory.ItemStack[] getStorageContents();
    void setStorageContents(org.bukkit.inventory.ItemStack[] arg0) throws java.lang.IllegalArgumentException;
    boolean contains(org.bukkit.Material arg0) throws java.lang.IllegalArgumentException;
    boolean contains(org.bukkit.inventory.ItemStack arg0);
    boolean contains(org.bukkit.Material arg0, int arg1) throws java.lang.IllegalArgumentException;
    boolean contains(org.bukkit.inventory.ItemStack arg0, int arg1);
    boolean containsAtLeast(org.bukkit.inventory.ItemStack arg0, int arg1);
    java.util.HashMap all(org.bukkit.Material arg0) throws java.lang.IllegalArgumentException;
    java.util.HashMap all(org.bukkit.inventory.ItemStack arg0);
    int first(org.bukkit.Material arg0) throws java.lang.IllegalArgumentException;
    int first(org.bukkit.inventory.ItemStack arg0);
    int firstEmpty();
    boolean isEmpty();
    void remove(org.bukkit.Material arg0) throws java.lang.IllegalArgumentException;
    void remove(org.bukkit.inventory.ItemStack arg0);
    void clear(int arg0);
    void clear();
    int close();
    java.util.List getViewers();
    org.bukkit.event.inventory.InventoryType getType();
    org.bukkit.inventory.InventoryHolder getHolder();
    org.bukkit.inventory.InventoryHolder getHolder(boolean arg0);
    java.util.ListIterator iterator();
    java.util.ListIterator iterator(int arg0);
    org.bukkit.Location getLocation();
}
