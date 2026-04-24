package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface JoinConfiguration extends net.kyori.adventure.util.Buildable, net.kyori.examination.Examinable {
    static net.kyori.adventure.text.JoinConfiguration$Builder builder() {
        return null;
    }
    static net.kyori.adventure.text.JoinConfiguration noSeparators() {
        return null;
    }
    static net.kyori.adventure.text.JoinConfiguration newlines() {
        return null;
    }
    static net.kyori.adventure.text.JoinConfiguration spaces() {
        return null;
    }
    static net.kyori.adventure.text.JoinConfiguration commas(boolean arg0) {
        return null;
    }
    static net.kyori.adventure.text.JoinConfiguration arrayLike() {
        return null;
    }
    static net.kyori.adventure.text.JoinConfiguration separator(net.kyori.adventure.text.ComponentLike arg0) {
        return null;
    }
    static net.kyori.adventure.text.JoinConfiguration separators(net.kyori.adventure.text.ComponentLike arg0, net.kyori.adventure.text.ComponentLike arg1) {
        return null;
    }
    net.kyori.adventure.text.Component prefix();
    net.kyori.adventure.text.Component suffix();
    net.kyori.adventure.text.Component separator();
    net.kyori.adventure.text.Component lastSeparator();
    net.kyori.adventure.text.Component lastSeparatorIfSerial();
    java.util.function.Function convertor();
    java.util.function.Predicate predicate();
    net.kyori.adventure.text.format.Style parentStyle();
}
