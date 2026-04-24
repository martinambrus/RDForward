package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Tool {
    static io.papermc.paper.datacomponent.item.Tool$Builder tool() {
        return null;
    }
    static io.papermc.paper.datacomponent.item.Tool$Rule rule(io.papermc.paper.registry.set.RegistryKeySet arg0, java.lang.Float arg1, net.kyori.adventure.util.TriState arg2) {
        return null;
    }
    float defaultMiningSpeed();
    int damagePerBlock();
    java.util.List rules();
    boolean canDestroyBlocksInCreative();
}
