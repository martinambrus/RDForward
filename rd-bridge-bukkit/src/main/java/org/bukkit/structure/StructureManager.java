package org.bukkit.structure;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface StructureManager {
    java.util.Map getStructures();
    org.bukkit.structure.Structure getStructure(org.bukkit.NamespacedKey arg0);
    org.bukkit.structure.Structure registerStructure(org.bukkit.NamespacedKey arg0, org.bukkit.structure.Structure arg1);
    org.bukkit.structure.Structure unregisterStructure(org.bukkit.NamespacedKey arg0);
    org.bukkit.structure.Structure loadStructure(org.bukkit.NamespacedKey arg0, boolean arg1);
    org.bukkit.structure.Structure loadStructure(org.bukkit.NamespacedKey arg0);
    void saveStructure(org.bukkit.NamespacedKey arg0);
    void saveStructure(org.bukkit.NamespacedKey arg0, org.bukkit.structure.Structure arg1) throws java.io.IOException;
    void deleteStructure(org.bukkit.NamespacedKey arg0) throws java.io.IOException;
    void deleteStructure(org.bukkit.NamespacedKey arg0, boolean arg1) throws java.io.IOException;
    java.io.File getStructureFile(org.bukkit.NamespacedKey arg0);
    org.bukkit.structure.Structure loadStructure(java.io.File arg0) throws java.io.IOException;
    org.bukkit.structure.Structure loadStructure(java.io.InputStream arg0) throws java.io.IOException;
    void saveStructure(java.io.File arg0, org.bukkit.structure.Structure arg1) throws java.io.IOException;
    void saveStructure(java.io.OutputStream arg0, org.bukkit.structure.Structure arg1) throws java.io.IOException;
    org.bukkit.structure.Structure createStructure();
    org.bukkit.structure.Structure copy(org.bukkit.structure.Structure arg0);
}
