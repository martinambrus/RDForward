package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ResolvableProfile$SkinPatch {
    static io.papermc.paper.datacomponent.item.ResolvableProfile$SkinPatch empty() {
        return null;
    }
    static io.papermc.paper.datacomponent.item.ResolvableProfile$SkinPatchBuilder skinPatch() {
        return null;
    }
    net.kyori.adventure.key.Key body();
    net.kyori.adventure.key.Key cape();
    net.kyori.adventure.key.Key elytra();
    org.bukkit.profile.PlayerTextures$SkinModel model();
    default boolean isEmpty() {
        return false;
    }
}
