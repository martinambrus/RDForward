package net.kyori.adventure.sound;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SoundStop extends net.kyori.examination.Examinable {
    static net.kyori.adventure.sound.SoundStop all() {
        return null;
    }
    static net.kyori.adventure.sound.SoundStop named(net.kyori.adventure.key.Key arg0) {
        return null;
    }
    static net.kyori.adventure.sound.SoundStop named(net.kyori.adventure.sound.Sound$Type arg0) {
        return null;
    }
    static net.kyori.adventure.sound.SoundStop named(java.util.function.Supplier arg0) {
        return null;
    }
    static net.kyori.adventure.sound.SoundStop source(net.kyori.adventure.sound.Sound$Source arg0) {
        return null;
    }
    static net.kyori.adventure.sound.SoundStop namedOnSource(net.kyori.adventure.key.Key arg0, net.kyori.adventure.sound.Sound$Source arg1) {
        return null;
    }
    static net.kyori.adventure.sound.SoundStop namedOnSource(net.kyori.adventure.sound.Sound$Type arg0, net.kyori.adventure.sound.Sound$Source arg1) {
        return null;
    }
    static net.kyori.adventure.sound.SoundStop namedOnSource(java.util.function.Supplier arg0, net.kyori.adventure.sound.Sound$Source arg1) {
        return null;
    }
    net.kyori.adventure.key.Key sound();
    net.kyori.adventure.sound.Sound$Source source();
}
