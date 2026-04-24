package net.kyori.adventure.resource;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ResourcePackRequest extends net.kyori.examination.Examinable, net.kyori.adventure.resource.ResourcePackRequestLike {
    static net.kyori.adventure.resource.ResourcePackRequest addingRequest(net.kyori.adventure.resource.ResourcePackInfoLike arg0, net.kyori.adventure.resource.ResourcePackInfoLike[] arg1) {
        return null;
    }
    static net.kyori.adventure.resource.ResourcePackRequest$Builder resourcePackRequest() {
        return null;
    }
    static net.kyori.adventure.resource.ResourcePackRequest$Builder resourcePackRequest(net.kyori.adventure.resource.ResourcePackRequest arg0) {
        return null;
    }
    java.util.List packs();
    net.kyori.adventure.resource.ResourcePackRequest packs(java.lang.Iterable arg0);
    net.kyori.adventure.resource.ResourcePackCallback callback();
    net.kyori.adventure.resource.ResourcePackRequest callback(net.kyori.adventure.resource.ResourcePackCallback arg0);
    boolean replace();
    net.kyori.adventure.resource.ResourcePackRequest replace(boolean arg0);
    boolean required();
    net.kyori.adventure.text.Component prompt();
    default net.kyori.adventure.resource.ResourcePackRequest asResourcePackRequest() {
        return null;
    }
}
