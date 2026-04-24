package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface KnowledgeBookMeta extends org.bukkit.inventory.meta.ItemMeta {
    boolean hasRecipes();
    java.util.List getRecipes();
    void setRecipes(java.util.List arg0);
    void addRecipe(org.bukkit.NamespacedKey[] arg0);
    org.bukkit.inventory.meta.KnowledgeBookMeta clone();
}
