package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface JukeboxSong extends org.bukkit.Keyed, org.bukkit.Translatable {
    public static final org.bukkit.JukeboxSong ELEVEN = null;
    public static final org.bukkit.JukeboxSong THIRTEEN = null;
    public static final org.bukkit.JukeboxSong FIVE = null;
    public static final org.bukkit.JukeboxSong BLOCKS = null;
    public static final org.bukkit.JukeboxSong CAT = null;
    public static final org.bukkit.JukeboxSong CHIRP = null;
    public static final org.bukkit.JukeboxSong CREATOR = null;
    public static final org.bukkit.JukeboxSong CREATOR_MUSIC_BOX = null;
    public static final org.bukkit.JukeboxSong FAR = null;
    public static final org.bukkit.JukeboxSong LAVA_CHICKEN = null;
    public static final org.bukkit.JukeboxSong MALL = null;
    public static final org.bukkit.JukeboxSong MELLOHI = null;
    public static final org.bukkit.JukeboxSong OTHERSIDE = null;
    public static final org.bukkit.JukeboxSong PIGSTEP = null;
    public static final org.bukkit.JukeboxSong PRECIPICE = null;
    public static final org.bukkit.JukeboxSong RELIC = null;
    public static final org.bukkit.JukeboxSong STAL = null;
    public static final org.bukkit.JukeboxSong STRAD = null;
    public static final org.bukkit.JukeboxSong TEARS = null;
    public static final org.bukkit.JukeboxSong WAIT = null;
    public static final org.bukkit.JukeboxSong WARD = null;
    java.lang.String getTranslationKey();
    org.bukkit.Sound getSound();
    net.kyori.adventure.text.Component getDescription();
    float getLengthInSeconds();
    int getComparatorOutput();
}
