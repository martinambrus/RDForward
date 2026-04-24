package net.kyori.adventure.resource;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ResourcePackInfo$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.resource.ResourcePackInfoLike {
    net.kyori.adventure.resource.ResourcePackInfo$Builder id(java.util.UUID arg0);
    net.kyori.adventure.resource.ResourcePackInfo$Builder uri(java.net.URI arg0);
    net.kyori.adventure.resource.ResourcePackInfo$Builder hash(java.lang.String arg0);
    net.kyori.adventure.resource.ResourcePackInfo build();
    default java.util.concurrent.CompletableFuture computeHashAndBuild() {
        return null;
    }
    java.util.concurrent.CompletableFuture computeHashAndBuild(java.util.concurrent.Executor arg0);
    default net.kyori.adventure.resource.ResourcePackInfo asResourcePackInfo() {
        return null;
    }
}
