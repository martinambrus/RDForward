package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlocksAttacks {
    static io.papermc.paper.datacomponent.item.BlocksAttacks$Builder blocksAttacks() {
        return null;
    }
    float blockDelaySeconds();
    float disableCooldownScale();
    java.util.List damageReductions();
    io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction itemDamage();
    io.papermc.paper.registry.set.RegistryKeySet bypassedBy();
    net.kyori.adventure.key.Key blockSound();
    net.kyori.adventure.key.Key disableSound();
}
