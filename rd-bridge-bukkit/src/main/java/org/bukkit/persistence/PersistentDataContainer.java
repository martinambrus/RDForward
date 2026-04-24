package org.bukkit.persistence;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PersistentDataContainer extends io.papermc.paper.persistence.PersistentDataContainerView {
    void set(org.bukkit.NamespacedKey arg0, org.bukkit.persistence.PersistentDataType arg1, java.lang.Object arg2);
    void remove(org.bukkit.NamespacedKey arg0);
    void readFromBytes(byte[] arg0, boolean arg1) throws java.io.IOException;
    default void readFromBytes(byte[] arg0) throws java.io.IOException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.persistence.PersistentDataContainer.readFromBytes([B)V");
    }
}
