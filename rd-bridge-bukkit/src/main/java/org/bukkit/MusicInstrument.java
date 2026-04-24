package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class MusicInstrument implements org.bukkit.Keyed, net.kyori.adventure.translation.Translatable {
    public static final org.bukkit.MusicInstrument ADMIRE_GOAT_HORN = null;
    public static final org.bukkit.MusicInstrument CALL_GOAT_HORN = null;
    public static final org.bukkit.MusicInstrument DREAM_GOAT_HORN = null;
    public static final org.bukkit.MusicInstrument FEEL_GOAT_HORN = null;
    public static final org.bukkit.MusicInstrument PONDER_GOAT_HORN = null;
    public static final org.bukkit.MusicInstrument SEEK_GOAT_HORN = null;
    public static final org.bukkit.MusicInstrument SING_GOAT_HORN = null;
    public static final org.bukkit.MusicInstrument YEARN_GOAT_HORN = null;
    public MusicInstrument() {}
    public static org.bukkit.MusicInstrument create(java.util.function.Consumer arg0) {
        return null;
    }
    public static org.bukkit.MusicInstrument getByKey(org.bukkit.NamespacedKey arg0) {
        return null;
    }
    public static java.util.Collection values() {
        return java.util.Collections.emptyList();
    }
    public abstract float getDuration();
    public abstract float getRange();
    public abstract net.kyori.adventure.text.Component description();
    public abstract org.bukkit.Sound getSound();
    public abstract org.bukkit.NamespacedKey getKey();
    public net.kyori.adventure.key.Key key() {
        return null;
    }
    public abstract java.lang.String translationKey();
}
