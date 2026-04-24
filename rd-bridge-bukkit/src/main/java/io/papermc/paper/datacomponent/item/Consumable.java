package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Consumable extends io.papermc.paper.datacomponent.BuildableDataComponent {
    static io.papermc.paper.datacomponent.item.Consumable$Builder consumable() {
        return null;
    }
    float consumeSeconds();
    io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation animation();
    net.kyori.adventure.key.Key sound();
    boolean hasConsumeParticles();
    java.util.List consumeEffects();
}
