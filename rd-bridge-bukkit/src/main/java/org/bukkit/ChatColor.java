// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit;

/**
 * Bukkit-shaped {@link ChatColor} enum with real {@link #toString()}
 * semantics. Plugins (LoginSecurity's {@code MessageTask}) build chat
 * lines via {@code ChatColor.RED + "Please register..."}; the auto-gen
 * stub returned {@code null} for {@code toString}, producing
 * {@code "nullPlease register..."} output.
 *
 * <p>Color codes match the upstream Bukkit/Spigot conventions so any
 * downstream Adventure or BungeeCord conversion picks up the right
 * legacy section symbol.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public enum ChatColor {
    BLACK('0', false),
    DARK_BLUE('1', false),
    DARK_GREEN('2', false),
    DARK_AQUA('3', false),
    DARK_RED('4', false),
    DARK_PURPLE('5', false),
    GOLD('6', false),
    GRAY('7', false),
    DARK_GRAY('8', false),
    BLUE('9', false),
    GREEN('a', false),
    AQUA('b', false),
    RED('c', false),
    LIGHT_PURPLE('d', false),
    YELLOW('e', false),
    WHITE('f', false),
    MAGIC('k', true),
    BOLD('l', true),
    STRIKETHROUGH('m', true),
    UNDERLINE('n', true),
    ITALIC('o', true),
    RESET('r', false);

    public static final char COLOR_CHAR = '§';

    private final char code;
    private final boolean format;
    private final String string;

    ChatColor(char code, boolean format) {
        this.code = code;
        this.format = format;
        this.string = "" + COLOR_CHAR + code;
    }

    public char getChar() { return code; }
    public boolean isFormat() { return format; }
    public boolean isColor() { return !format && this != RESET; }

    @Override
    public java.lang.String toString() { return string; }

    public net.md_5.bungee.api.ChatColor asBungee() { return null; }

    public static org.bukkit.ChatColor getByChar(char c) {
        for (ChatColor v : values()) if (v.code == c) return v;
        return null;
    }

    public static org.bukkit.ChatColor getByChar(java.lang.String s) {
        if (s == null || s.isEmpty()) return null;
        return getByChar(s.charAt(0));
    }

    public static java.lang.String stripColor(java.lang.String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == COLOR_CHAR && i + 1 < input.length()) { i++; continue; }
            sb.append(c);
        }
        return sb.toString();
    }

    private static final String CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";

    public static java.lang.String translateAlternateColorCodes(char altChar, java.lang.String input) {
        if (input == null) return null;
        char[] b = input.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altChar && CODES.indexOf(b[i + 1]) >= 0) {
                b[i] = COLOR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }
        return new String(b);
    }

    public static java.lang.String getLastColors(java.lang.String input) {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        for (int i = input.length() - 1; i > -1; i--) {
            char c = input.charAt(i);
            if (c == COLOR_CHAR && i < input.length() - 1) {
                ChatColor cc = getByChar(input.charAt(i + 1));
                if (cc != null) {
                    result.insert(0, cc.toString());
                    if (cc.isColor() || cc == RESET) break;
                }
            }
        }
        return result.toString();
    }
}
