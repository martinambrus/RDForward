package io.papermc.paper.datacomponent.item.consumable;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
interface ConsumableTypesBridge {
    public static final java.util.Optional BRIDGE = java.util.Optional.empty();
    static io.papermc.paper.datacomponent.item.consumable.ConsumableTypesBridge bridge() {
        return null;
    }
    io.papermc.paper.datacomponent.item.consumable.ConsumeEffect$ApplyStatusEffects applyStatusEffects(java.util.List arg0, float arg1);
    io.papermc.paper.datacomponent.item.consumable.ConsumeEffect$RemoveStatusEffects removeStatusEffects(io.papermc.paper.registry.set.RegistryKeySet arg0);
    io.papermc.paper.datacomponent.item.consumable.ConsumeEffect$ClearAllStatusEffects clearAllStatusEffects();
    io.papermc.paper.datacomponent.item.consumable.ConsumeEffect$PlaySound playSoundEffect(net.kyori.adventure.key.Key arg0);
    io.papermc.paper.datacomponent.item.consumable.ConsumeEffect$TeleportRandomly teleportRandomlyEffect(float arg0);
}
