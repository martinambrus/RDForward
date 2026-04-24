package net.kyori.adventure.text.format;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface TextColor extends java.lang.Comparable, net.kyori.examination.Examinable, net.kyori.adventure.util.RGBLike, net.kyori.adventure.text.format.StyleBuilderApplicable, net.kyori.adventure.text.format.TextFormat {
    public static final char HEX_CHARACTER = (char) 35;
    public static final java.lang.String HEX_PREFIX = "#";
    static net.kyori.adventure.text.format.TextColor color(int arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.TextColor color(net.kyori.adventure.util.RGBLike arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.TextColor color(net.kyori.adventure.util.HSVLike arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.TextColor color(int arg0, int arg1, int arg2) {
        return null;
    }
    static net.kyori.adventure.text.format.TextColor color(float arg0, float arg1, float arg2) {
        return null;
    }
    static net.kyori.adventure.text.format.TextColor fromHexString(java.lang.String arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.TextColor fromCSSHexString(java.lang.String arg0) {
        return null;
    }
    int value();
    default java.lang.String asHexString() {
        return null;
    }
    default int red() {
        return 0;
    }
    default int green() {
        return 0;
    }
    default int blue() {
        return 0;
    }
    static net.kyori.adventure.text.format.TextColor lerp(float arg0, net.kyori.adventure.util.RGBLike arg1, net.kyori.adventure.util.RGBLike arg2) {
        return null;
    }
    static net.kyori.adventure.text.format.TextColor nearestColorTo(java.util.List arg0, net.kyori.adventure.text.format.TextColor arg1) {
        return null;
    }
    default void styleApply(net.kyori.adventure.text.format.Style$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.TextColor.styleApply(Lnet/kyori/adventure/text/format/Style$Builder;)V");
    }
    default int compareTo(net.kyori.adventure.text.format.TextColor arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.TextColor.compareTo(Lnet/kyori/adventure/text/format/TextColor;)I");
        return 0;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
    default int compareTo(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.TextColor.compareTo(Ljava/lang/Object;)I");
        return 0;
    }
}
