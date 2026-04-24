package net.kyori.adventure.text.format;

/**
 * Adventure's sixteen named colours. Each carries the legacy single-character
 * code so {@code LegacyComponentSerializer} can emit the {@code §0..§f}
 * sequences Minecraft chat understands.
 */
public final class NamedTextColor implements TextColor {

    public static final NamedTextColor BLACK        = new NamedTextColor('0', "black");
    public static final NamedTextColor DARK_BLUE    = new NamedTextColor('1', "dark_blue");
    public static final NamedTextColor DARK_GREEN   = new NamedTextColor('2', "dark_green");
    public static final NamedTextColor DARK_AQUA    = new NamedTextColor('3', "dark_aqua");
    public static final NamedTextColor DARK_RED     = new NamedTextColor('4', "dark_red");
    public static final NamedTextColor DARK_PURPLE  = new NamedTextColor('5', "dark_purple");
    public static final NamedTextColor GOLD         = new NamedTextColor('6', "gold");
    public static final NamedTextColor GRAY         = new NamedTextColor('7', "gray");
    public static final NamedTextColor DARK_GRAY    = new NamedTextColor('8', "dark_gray");
    public static final NamedTextColor BLUE         = new NamedTextColor('9', "blue");
    public static final NamedTextColor GREEN        = new NamedTextColor('a', "green");
    public static final NamedTextColor AQUA         = new NamedTextColor('b', "aqua");
    public static final NamedTextColor RED          = new NamedTextColor('c', "red");
    public static final NamedTextColor LIGHT_PURPLE = new NamedTextColor('d', "light_purple");
    public static final NamedTextColor YELLOW       = new NamedTextColor('e', "yellow");
    public static final NamedTextColor WHITE        = new NamedTextColor('f', "white");

    private final char code;
    private final String name;

    private NamedTextColor(char code, String name) {
        this.code = code;
        this.name = name;
    }

    public char code() { return code; }

    public String name() { return name; }

    public String toLegacyString() { return "§" + code; }
}
