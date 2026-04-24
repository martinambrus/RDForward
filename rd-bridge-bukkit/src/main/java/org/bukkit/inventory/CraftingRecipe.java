package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class CraftingRecipe implements org.bukkit.inventory.Recipe, org.bukkit.Keyed {
    protected CraftingRecipe(org.bukkit.NamespacedKey arg0, org.bukkit.inventory.ItemStack arg1) {}
    protected CraftingRecipe() {}
    public org.bukkit.NamespacedKey getKey() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getResult() {
        return null;
    }
    public java.lang.String getGroup() {
        return null;
    }
    public void setGroup(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CraftingRecipe.setGroup(Ljava/lang/String;)V");
    }
    public org.bukkit.inventory.recipe.CraftingBookCategory getCategory() {
        return null;
    }
    public void setCategory(org.bukkit.inventory.recipe.CraftingBookCategory arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CraftingRecipe.setCategory(Lorg/bukkit/inventory/recipe/CraftingBookCategory;)V");
    }
    protected static org.bukkit.inventory.ItemStack checkResult(org.bukkit.inventory.ItemStack arg0) {
        return null;
    }
}
