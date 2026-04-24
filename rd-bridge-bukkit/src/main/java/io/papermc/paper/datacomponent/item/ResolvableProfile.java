package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ResolvableProfile extends net.kyori.adventure.text.object.PlayerHeadObjectContents$SkinSource {
    static io.papermc.paper.datacomponent.item.ResolvableProfile resolvableProfile(com.destroystokyo.paper.profile.PlayerProfile arg0) {
        return null;
    }
    static io.papermc.paper.datacomponent.item.ResolvableProfile$Builder resolvableProfile() {
        return null;
    }
    java.util.UUID uuid();
    java.lang.String name();
    java.util.Collection properties();
    boolean dynamic();
    java.util.concurrent.CompletableFuture resolve();
    io.papermc.paper.datacomponent.item.ResolvableProfile$SkinPatch skinPatch();
}
