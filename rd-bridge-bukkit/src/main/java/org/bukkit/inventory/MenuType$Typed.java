package org.bukkit.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MenuType$Typed extends org.bukkit.inventory.MenuType {
    default org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.MenuType$Typed.create(Lorg/bukkit/entity/HumanEntity;)Lorg/bukkit/inventory/InventoryView;");
        return null;
    }
    org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity arg0, java.lang.String arg1);
    org.bukkit.inventory.InventoryView create(org.bukkit.entity.HumanEntity arg0, net.kyori.adventure.text.Component arg1);
    org.bukkit.inventory.view.builder.InventoryViewBuilder builder();
}
