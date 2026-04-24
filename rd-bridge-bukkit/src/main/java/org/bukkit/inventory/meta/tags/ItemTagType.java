package org.bukkit.inventory.meta.tags;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ItemTagType {
    public static final org.bukkit.inventory.meta.tags.ItemTagType BYTE = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType SHORT = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType INTEGER = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType LONG = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType FLOAT = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType DOUBLE = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType STRING = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType BYTE_ARRAY = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType INTEGER_ARRAY = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType LONG_ARRAY = null;
    public static final org.bukkit.inventory.meta.tags.ItemTagType TAG_CONTAINER = null;
    java.lang.Class getPrimitiveType();
    java.lang.Class getComplexType();
    java.lang.Object toPrimitive(java.lang.Object arg0, org.bukkit.inventory.meta.tags.ItemTagAdapterContext arg1);
    java.lang.Object fromPrimitive(java.lang.Object arg0, org.bukkit.inventory.meta.tags.ItemTagAdapterContext arg1);
}
