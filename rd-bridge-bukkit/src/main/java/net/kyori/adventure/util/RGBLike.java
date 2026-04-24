package net.kyori.adventure.util;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RGBLike {
    int red();
    int green();
    int blue();
    default net.kyori.adventure.util.HSVLike asHSV() {
        return null;
    }
}
