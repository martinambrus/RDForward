package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Consumable$Builder extends io.papermc.paper.datacomponent.DataComponentBuilder {
    io.papermc.paper.datacomponent.item.Consumable$Builder consumeSeconds(float arg0);
    io.papermc.paper.datacomponent.item.Consumable$Builder animation(io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation arg0);
    io.papermc.paper.datacomponent.item.Consumable$Builder sound(net.kyori.adventure.key.Key arg0);
    io.papermc.paper.datacomponent.item.Consumable$Builder hasConsumeParticles(boolean arg0);
    io.papermc.paper.datacomponent.item.Consumable$Builder effects(java.util.List arg0);
    io.papermc.paper.datacomponent.item.Consumable$Builder addEffect(io.papermc.paper.datacomponent.item.consumable.ConsumeEffect arg0);
    io.papermc.paper.datacomponent.item.Consumable$Builder addEffects(java.util.List arg0);
}
