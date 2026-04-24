package net.kyori.adventure.sound;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Sound extends net.kyori.examination.Examinable {
    static net.kyori.adventure.sound.Sound$Builder sound() {
        return null;
    }
    static net.kyori.adventure.sound.Sound$Builder sound(net.kyori.adventure.sound.Sound arg0) {
        return null;
    }
    static net.kyori.adventure.sound.Sound sound(java.util.function.Consumer arg0) {
        return null;
    }
    static net.kyori.adventure.sound.Sound sound(net.kyori.adventure.key.Key arg0, net.kyori.adventure.sound.Sound$Source arg1, float arg2, float arg3) {
        return null;
    }
    static net.kyori.adventure.sound.Sound sound(net.kyori.adventure.sound.Sound$Type arg0, net.kyori.adventure.sound.Sound$Source arg1, float arg2, float arg3) {
        return null;
    }
    static net.kyori.adventure.sound.Sound sound(java.util.function.Supplier arg0, net.kyori.adventure.sound.Sound$Source arg1, float arg2, float arg3) {
        return null;
    }
    static net.kyori.adventure.sound.Sound sound(net.kyori.adventure.key.Key arg0, net.kyori.adventure.sound.Sound$Source$Provider arg1, float arg2, float arg3) {
        return null;
    }
    static net.kyori.adventure.sound.Sound sound(net.kyori.adventure.sound.Sound$Type arg0, net.kyori.adventure.sound.Sound$Source$Provider arg1, float arg2, float arg3) {
        return null;
    }
    static net.kyori.adventure.sound.Sound sound(java.util.function.Supplier arg0, net.kyori.adventure.sound.Sound$Source$Provider arg1, float arg2, float arg3) {
        return null;
    }
    net.kyori.adventure.key.Key name();
    net.kyori.adventure.sound.Sound$Source source();
    float volume();
    float pitch();
    java.util.OptionalLong seed();
    net.kyori.adventure.sound.SoundStop asStop();
}
