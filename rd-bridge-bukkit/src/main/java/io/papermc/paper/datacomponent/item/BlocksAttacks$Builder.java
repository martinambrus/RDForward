package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlocksAttacks$Builder extends io.papermc.paper.datacomponent.DataComponentBuilder {
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder blockDelaySeconds(float arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder disableCooldownScale(float arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder addDamageReduction(io.papermc.paper.datacomponent.item.blocksattacks.DamageReduction arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder damageReductions(java.util.List arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder itemDamage(io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder bypassedBy(io.papermc.paper.registry.set.RegistryKeySet arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder blockSound(net.kyori.adventure.key.Key arg0);
    io.papermc.paper.datacomponent.item.BlocksAttacks$Builder disableSound(net.kyori.adventure.key.Key arg0);
}
