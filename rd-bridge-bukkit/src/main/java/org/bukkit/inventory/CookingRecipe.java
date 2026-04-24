package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class CookingRecipe implements org.bukkit.inventory.Recipe, org.bukkit.Keyed {
    public CookingRecipe(org.bukkit.NamespacedKey arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.Material arg2, float arg3, int arg4) {}
    public CookingRecipe(org.bukkit.NamespacedKey arg0, org.bukkit.inventory.ItemStack arg1, org.bukkit.inventory.RecipeChoice arg2, float arg3, int arg4) {}
    protected CookingRecipe() {}
    public org.bukkit.inventory.CookingRecipe setInput(org.bukkit.Material arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CookingRecipe.setInput(Lorg/bukkit/Material;)Lorg/bukkit/inventory/CookingRecipe;");
        return this;
    }
    public org.bukkit.inventory.ItemStack getInput() {
        return null;
    }
    public org.bukkit.inventory.CookingRecipe setInputChoice(org.bukkit.inventory.RecipeChoice arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CookingRecipe.setInputChoice(Lorg/bukkit/inventory/RecipeChoice;)Lorg/bukkit/inventory/CookingRecipe;");
        return this;
    }
    public org.bukkit.inventory.RecipeChoice getInputChoice() {
        return null;
    }
    public org.bukkit.inventory.ItemStack getResult() {
        return null;
    }
    public void setExperience(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CookingRecipe.setExperience(F)V");
    }
    public float getExperience() {
        return 0.0f;
    }
    public void setCookingTime(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CookingRecipe.setCookingTime(I)V");
    }
    public int getCookingTime() {
        return 0;
    }
    public org.bukkit.NamespacedKey getKey() {
        return null;
    }
    public java.lang.String getGroup() {
        return null;
    }
    public void setGroup(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CookingRecipe.setGroup(Ljava/lang/String;)V");
    }
    public org.bukkit.inventory.recipe.CookingBookCategory getCategory() {
        return null;
    }
    public void setCategory(org.bukkit.inventory.recipe.CookingBookCategory arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.CookingRecipe.setCategory(Lorg/bukkit/inventory/recipe/CookingBookCategory;)V");
    }
}
