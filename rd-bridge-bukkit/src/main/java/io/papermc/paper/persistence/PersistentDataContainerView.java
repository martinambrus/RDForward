package io.papermc.paper.persistence;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PersistentDataContainerView {
    boolean has(org.bukkit.NamespacedKey arg0, org.bukkit.persistence.PersistentDataType arg1);
    boolean has(org.bukkit.NamespacedKey arg0);
    java.lang.Object get(org.bukkit.NamespacedKey arg0, org.bukkit.persistence.PersistentDataType arg1);
    java.lang.Object getOrDefault(org.bukkit.NamespacedKey arg0, org.bukkit.persistence.PersistentDataType arg1, java.lang.Object arg2);
    java.util.Set getKeys();
    boolean isEmpty();
    void copyTo(org.bukkit.persistence.PersistentDataContainer arg0, boolean arg1);
    org.bukkit.persistence.PersistentDataAdapterContext getAdapterContext();
    byte[] serializeToBytes() throws java.io.IOException;
    int getSize();
}
