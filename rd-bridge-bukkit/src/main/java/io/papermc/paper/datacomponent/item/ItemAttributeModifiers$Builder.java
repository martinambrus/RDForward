package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ItemAttributeModifiers$Builder extends io.papermc.paper.datacomponent.DataComponentBuilder {
    default io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder addModifier(org.bukkit.attribute.Attribute arg0, org.bukkit.attribute.AttributeModifier arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder.addModifier(Lorg/bukkit/attribute/Attribute;Lorg/bukkit/attribute/AttributeModifier;)Lio/papermc/paper/datacomponent/item/ItemAttributeModifiers$Builder;");
        return this;
    }
    default io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder addModifier(org.bukkit.attribute.Attribute arg0, org.bukkit.attribute.AttributeModifier arg1, org.bukkit.inventory.EquipmentSlotGroup arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder.addModifier(Lorg/bukkit/attribute/Attribute;Lorg/bukkit/attribute/AttributeModifier;Lorg/bukkit/inventory/EquipmentSlotGroup;)Lio/papermc/paper/datacomponent/item/ItemAttributeModifiers$Builder;");
        return this;
    }
    default io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder addModifier(org.bukkit.attribute.Attribute arg0, org.bukkit.attribute.AttributeModifier arg1, io.papermc.paper.datacomponent.item.attribute.AttributeModifierDisplay arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder.addModifier(Lorg/bukkit/attribute/Attribute;Lorg/bukkit/attribute/AttributeModifier;Lio/papermc/paper/datacomponent/item/attribute/AttributeModifierDisplay;)Lio/papermc/paper/datacomponent/item/ItemAttributeModifiers$Builder;");
        return this;
    }
    io.papermc.paper.datacomponent.item.ItemAttributeModifiers$Builder addModifier(org.bukkit.attribute.Attribute arg0, org.bukkit.attribute.AttributeModifier arg1, org.bukkit.inventory.EquipmentSlotGroup arg2, io.papermc.paper.datacomponent.item.attribute.AttributeModifierDisplay arg3);
}
