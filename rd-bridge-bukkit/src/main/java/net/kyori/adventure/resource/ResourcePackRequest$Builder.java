package net.kyori.adventure.resource;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ResourcePackRequest$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.resource.ResourcePackRequestLike {
    net.kyori.adventure.resource.ResourcePackRequest$Builder packs(net.kyori.adventure.resource.ResourcePackInfoLike arg0, net.kyori.adventure.resource.ResourcePackInfoLike[] arg1);
    net.kyori.adventure.resource.ResourcePackRequest$Builder packs(java.lang.Iterable arg0);
    net.kyori.adventure.resource.ResourcePackRequest$Builder callback(net.kyori.adventure.resource.ResourcePackCallback arg0);
    net.kyori.adventure.resource.ResourcePackRequest$Builder replace(boolean arg0);
    net.kyori.adventure.resource.ResourcePackRequest$Builder required(boolean arg0);
    net.kyori.adventure.resource.ResourcePackRequest$Builder prompt(net.kyori.adventure.text.Component arg0);
    default net.kyori.adventure.resource.ResourcePackRequest asResourcePackRequest() {
        return null;
    }
}
