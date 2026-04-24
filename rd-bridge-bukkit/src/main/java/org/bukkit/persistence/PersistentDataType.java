package org.bukkit.persistence;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PersistentDataType {
    public static final org.bukkit.persistence.PersistentDataType BYTE = null;
    public static final org.bukkit.persistence.PersistentDataType SHORT = null;
    public static final org.bukkit.persistence.PersistentDataType INTEGER = null;
    public static final org.bukkit.persistence.PersistentDataType LONG = null;
    public static final org.bukkit.persistence.PersistentDataType FLOAT = null;
    public static final org.bukkit.persistence.PersistentDataType DOUBLE = null;
    public static final org.bukkit.persistence.PersistentDataType BOOLEAN = null;
    public static final org.bukkit.persistence.PersistentDataType STRING = null;
    public static final org.bukkit.persistence.PersistentDataType BYTE_ARRAY = null;
    public static final org.bukkit.persistence.PersistentDataType INTEGER_ARRAY = null;
    public static final org.bukkit.persistence.PersistentDataType LONG_ARRAY = null;
    public static final org.bukkit.persistence.PersistentDataType TAG_CONTAINER_ARRAY = null;
    public static final org.bukkit.persistence.PersistentDataType TAG_CONTAINER = null;
    public static final org.bukkit.persistence.ListPersistentDataTypeProvider LIST = null;
    java.lang.Class getPrimitiveType();
    java.lang.Class getComplexType();
    java.lang.Object toPrimitive(java.lang.Object arg0, org.bukkit.persistence.PersistentDataAdapterContext arg1);
    java.lang.Object fromPrimitive(java.lang.Object arg0, org.bukkit.persistence.PersistentDataAdapterContext arg1);
}
