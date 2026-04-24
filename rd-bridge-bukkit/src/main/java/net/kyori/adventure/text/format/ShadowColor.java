package net.kyori.adventure.text.format;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ShadowColor extends net.kyori.adventure.text.format.StyleBuilderApplicable, net.kyori.adventure.util.ARGBLike {
    static net.kyori.adventure.text.format.ShadowColor lerp(float arg0, net.kyori.adventure.util.ARGBLike arg1, net.kyori.adventure.util.ARGBLike arg2) {
        return null;
    }
    static net.kyori.adventure.text.format.ShadowColor none() {
        return null;
    }
    static net.kyori.adventure.text.format.ShadowColor shadowColor(int arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.ShadowColor shadowColor(int arg0, int arg1, int arg2, int arg3) {
        return null;
    }
    static net.kyori.adventure.text.format.ShadowColor shadowColor(net.kyori.adventure.util.RGBLike arg0, int arg1) {
        return null;
    }
    static net.kyori.adventure.text.format.ShadowColor shadowColor(net.kyori.adventure.util.ARGBLike arg0) {
        return null;
    }
    static net.kyori.adventure.text.format.ShadowColor fromHexString(java.lang.String arg0) {
        return null;
    }
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
    default int alpha() {
        return 0;
    }
    int value();
    default void styleApply(net.kyori.adventure.text.format.Style$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.format.ShadowColor.styleApply(Lnet/kyori/adventure/text/format/Style$Builder;)V");
    }
}
