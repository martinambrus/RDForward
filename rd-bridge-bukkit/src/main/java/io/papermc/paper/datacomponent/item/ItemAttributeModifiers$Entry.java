package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ItemAttributeModifiers$Entry {
    org.bukkit.attribute.Attribute attribute();
    org.bukkit.attribute.AttributeModifier modifier();
    default org.bukkit.inventory.EquipmentSlotGroup getGroup() {
        return null;
    }
    io.papermc.paper.datacomponent.item.attribute.AttributeModifierDisplay display();
}
