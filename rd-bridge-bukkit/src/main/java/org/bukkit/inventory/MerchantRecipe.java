package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class MerchantRecipe implements org.bukkit.inventory.Recipe {
    public MerchantRecipe(org.bukkit.inventory.ItemStack arg0, int arg1) {}
    public MerchantRecipe(org.bukkit.inventory.ItemStack arg0, int arg1, int arg2, boolean arg3) {}
    public MerchantRecipe(org.bukkit.inventory.ItemStack arg0, int arg1, int arg2, boolean arg3, int arg4, float arg5) {}
    public MerchantRecipe(org.bukkit.inventory.ItemStack arg0, int arg1, int arg2, boolean arg3, int arg4, float arg5, int arg6, int arg7) {}
    public MerchantRecipe(org.bukkit.inventory.ItemStack arg0, int arg1, int arg2, boolean arg3, int arg4, float arg5, boolean arg6) {}
    public MerchantRecipe(org.bukkit.inventory.ItemStack arg0, int arg1, int arg2, boolean arg3, int arg4, float arg5, int arg6, int arg7, boolean arg8) {}
    public MerchantRecipe(org.bukkit.inventory.MerchantRecipe arg0) {}
    public MerchantRecipe() {}
    public org.bukkit.inventory.ItemStack getResult() {
        return null;
    }
    public void addIngredient(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.addIngredient(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public void removeIngredient(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.removeIngredient(I)V");
    }
    public void setIngredients(java.util.List arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setIngredients(Ljava/util/List;)V");
    }
    public java.util.List getIngredients() {
        return java.util.Collections.emptyList();
    }
    public org.bukkit.inventory.ItemStack getAdjustedIngredient1() {
        return null;
    }
    public void adjust(org.bukkit.inventory.ItemStack arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.adjust(Lorg/bukkit/inventory/ItemStack;)V");
    }
    public int getDemand() {
        return 0;
    }
    public void setDemand(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setDemand(I)V");
    }
    public int getSpecialPrice() {
        return 0;
    }
    public void setSpecialPrice(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setSpecialPrice(I)V");
    }
    public int getUses() {
        return 0;
    }
    public void setUses(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setUses(I)V");
    }
    public int getMaxUses() {
        return 0;
    }
    public void setMaxUses(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setMaxUses(I)V");
    }
    public boolean hasExperienceReward() {
        return false;
    }
    public void setExperienceReward(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setExperienceReward(Z)V");
    }
    public int getVillagerExperience() {
        return 0;
    }
    public void setVillagerExperience(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setVillagerExperience(I)V");
    }
    public float getPriceMultiplier() {
        return 0.0f;
    }
    public void setPriceMultiplier(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setPriceMultiplier(F)V");
    }
    public boolean shouldIgnoreDiscounts() {
        return false;
    }
    public void setIgnoreDiscounts(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MerchantRecipe.setIgnoreDiscounts(Z)V");
    }
}
