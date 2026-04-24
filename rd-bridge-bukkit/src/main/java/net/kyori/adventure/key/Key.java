package net.kyori.adventure.key;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Key extends java.lang.Comparable, net.kyori.examination.Examinable, net.kyori.adventure.key.Namespaced, net.kyori.adventure.key.Keyed {
    public static final java.lang.String MINECRAFT_NAMESPACE = "minecraft";
    public static final char DEFAULT_SEPARATOR = (char) 58;
    static net.kyori.adventure.key.Key key(java.lang.String arg0) {
        return null;
    }
    static net.kyori.adventure.key.Key key(java.lang.String arg0, char arg1) {
        return null;
    }
    static net.kyori.adventure.key.Key key(net.kyori.adventure.key.Namespaced arg0, java.lang.String arg1) {
        return null;
    }
    static net.kyori.adventure.key.Key key(java.lang.String arg0, java.lang.String arg1) {
        return null;
    }
    static java.util.Comparator comparator() {
        return null;
    }
    static boolean parseable(java.lang.String arg0) {
        return false;
    }
    static boolean parseableNamespace(java.lang.String arg0) {
        return false;
    }
    static java.util.OptionalInt checkNamespace(java.lang.String arg0) {
        return null;
    }
    static boolean parseableValue(java.lang.String arg0) {
        return false;
    }
    static java.util.OptionalInt checkValue(java.lang.String arg0) {
        return null;
    }
    static boolean allowedInNamespace(char arg0) {
        return false;
    }
    static boolean allowedInValue(char arg0) {
        return false;
    }
    java.lang.String namespace();
    java.lang.String value();
    java.lang.String asString();
    default java.lang.String asMinimalString() {
        return null;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
    default int compareTo(net.kyori.adventure.key.Key arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.key.Key.compareTo(Lnet/kyori/adventure/key/Key;)I");
        return 0;
    }
    default net.kyori.adventure.key.Key key() {
        return null;
    }
    default int compareTo(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.key.Key.compareTo(Ljava/lang/Object;)I");
        return 0;
    }
}
