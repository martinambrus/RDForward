package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface KeybindComponent extends net.kyori.adventure.text.BuildableComponent, net.kyori.adventure.text.ScopedComponent {
    java.lang.String keybind();
    net.kyori.adventure.text.KeybindComponent keybind(java.lang.String arg0);
    default net.kyori.adventure.text.KeybindComponent keybind(net.kyori.adventure.text.KeybindComponent$KeybindLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.KeybindComponent.keybind(Lnet/kyori/adventure/text/KeybindComponent$KeybindLike;)Lnet/kyori/adventure/text/KeybindComponent;");
        return this;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}
