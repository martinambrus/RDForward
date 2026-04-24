package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RecipeChoice extends java.util.function.Predicate, java.lang.Cloneable {
    static org.bukkit.inventory.RecipeChoice empty() {
        return null;
    }
    static org.bukkit.inventory.RecipeChoice$ItemTypeChoice itemType(org.bukkit.inventory.ItemType arg0, org.bukkit.inventory.ItemType[] arg1) {
        return null;
    }
    static org.bukkit.inventory.RecipeChoice$ItemTypeChoice itemType(io.papermc.paper.registry.set.RegistryKeySet arg0) {
        return null;
    }
    org.bukkit.inventory.ItemStack getItemStack();
    org.bukkit.inventory.RecipeChoice clone();
    boolean test(org.bukkit.inventory.ItemStack arg0);
    default org.bukkit.inventory.RecipeChoice validate(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.RecipeChoice.validate(Z)Lorg/bukkit/inventory/RecipeChoice;");
        return this;
    }
    default boolean test(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.RecipeChoice.test(Ljava/lang/Object;)Z");
        return false;
    }
}
