package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Merchant {
    java.util.List getRecipes();
    void setRecipes(java.util.List arg0);
    org.bukkit.inventory.MerchantRecipe getRecipe(int arg0) throws java.lang.IndexOutOfBoundsException;
    void setRecipe(int arg0, org.bukkit.inventory.MerchantRecipe arg1) throws java.lang.IndexOutOfBoundsException;
    int getRecipeCount();
    boolean isTrading();
    org.bukkit.entity.HumanEntity getTrader();
}
