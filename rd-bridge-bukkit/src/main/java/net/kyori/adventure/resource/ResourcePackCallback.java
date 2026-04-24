package net.kyori.adventure.resource;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ResourcePackCallback {
    static net.kyori.adventure.resource.ResourcePackCallback noOp() {
        return null;
    }
    static net.kyori.adventure.resource.ResourcePackCallback onTerminal(java.util.function.BiConsumer arg0, java.util.function.BiConsumer arg1) {
        return null;
    }
    void packEventReceived(java.util.UUID arg0, net.kyori.adventure.resource.ResourcePackStatus arg1, net.kyori.adventure.audience.Audience arg2);
}
