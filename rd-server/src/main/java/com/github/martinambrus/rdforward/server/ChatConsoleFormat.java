package com.github.martinambrus.rdforward.server;

/**
 * Strips Bukkit-style section-character colour codes ({@code §0}..{@code §f},
 * {@code §k}..{@code §r}) from chat messages before they are written to
 * the server console. Plugins like Bananas's profanity filter wrap their
 * replacement words in {@code ChatColor.GOLD + word + ChatColor.WHITE},
 * which becomes {@code §6word§f} on the wire. Java clients render those
 * codes correctly, but the Windows console shows the section sign as
 * {@code ?}, leaving operators staring at {@code [Chat] alpha: ?6LIME?f}.
 *
 * <p>The wire-level message broadcast to other players is left untouched
 * — only the console log line is stripped.
 */
public final class ChatConsoleFormat {

    private ChatConsoleFormat() {}

    /**
     * @return {@code msg} with every two-character colour code (a section
     *         sign followed by a hex digit / {@code k}-{@code r} formatting
     *         code) removed. {@code null} is passed through.
     */
    public static String strip(String msg) {
        if (msg == null || msg.isEmpty()) return msg;
        int len = msg.length();
        StringBuilder out = null;
        int i = 0;
        while (i < len) {
            char c = msg.charAt(i);
            if (c == '§' && i + 1 < len && isFormatChar(msg.charAt(i + 1))) {
                if (out == null) {
                    out = new StringBuilder(len);
                    out.append(msg, 0, i);
                }
                i += 2;
                continue;
            }
            if (out != null) out.append(c);
            i++;
        }
        return out == null ? msg : out.toString();
    }

    private static boolean isFormatChar(char c) {
        return (c >= '0' && c <= '9')
                || (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F')
                || (c >= 'k' && c <= 'o')
                || (c >= 'K' && c <= 'O')
                || c == 'r' || c == 'R'
                || c == 'x' || c == 'X';
    }
}
