package net.kyori.adventure.text.serializer.legacy;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CharacterAndFormat extends net.kyori.examination.Examinable {
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat BLACK = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat DARK_BLUE = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat DARK_GREEN = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat DARK_AQUA = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat DARK_RED = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat DARK_PURPLE = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat GOLD = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat GRAY = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat DARK_GRAY = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat BLUE = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat GREEN = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat AQUA = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat RED = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat LIGHT_PURPLE = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat YELLOW = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat WHITE = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat OBFUSCATED = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat BOLD = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat STRIKETHROUGH = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat UNDERLINED = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat ITALIC = null;
    public static final net.kyori.adventure.text.serializer.legacy.CharacterAndFormat RESET = null;
    static net.kyori.adventure.text.serializer.legacy.CharacterAndFormat characterAndFormat(char arg0, net.kyori.adventure.text.format.TextFormat arg1) {
        return null;
    }
    static net.kyori.adventure.text.serializer.legacy.CharacterAndFormat characterAndFormat(char arg0, net.kyori.adventure.text.format.TextFormat arg1, boolean arg2) {
        return null;
    }
    static java.util.List defaults() {
        return java.util.Collections.emptyList();
    }
    char character();
    net.kyori.adventure.text.format.TextFormat format();
    boolean caseInsensitive();
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}
