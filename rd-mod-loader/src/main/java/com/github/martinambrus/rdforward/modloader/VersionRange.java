package com.github.martinambrus.rdforward.modloader;

/**
 * Minimal version-constraint matcher used by {@link DependencyResolver}.
 * Supported syntaxes:
 *
 * <ul>
 *   <li>{@code "*"} — matches any version.</li>
 *   <li>{@code ">=1.0"} / {@code ">1.0"} / {@code "<=1.0"} / {@code "<1.0"}
 *       — numeric comparison against a dotted version.</li>
 *   <li>{@code "1.0"} (no operator) — exact-prefix match: the candidate
 *       must start with the given version when dotted-segmented
 *       (so {@code "1.0"} matches {@code "1.0"}, {@code "1.0.3"} but not
 *       {@code "1.10"}).</li>
 * </ul>
 *
 * <p>Version strings compare segment-wise as integers where parseable,
 * otherwise lexicographically. Missing trailing segments are treated as
 * zero ({@code "1.2"} &lt; {@code "1.2.1"}).
 */
public final class VersionRange {

    private VersionRange() {}

    public static boolean matches(String constraint, String candidate) {
        if (constraint == null || constraint.isBlank() || "*".equals(constraint.trim())) {
            return true;
        }
        String c = constraint.trim();
        String op;
        String required;
        if (c.startsWith(">=")) { op = ">="; required = c.substring(2).trim(); }
        else if (c.startsWith("<=")) { op = "<="; required = c.substring(2).trim(); }
        else if (c.startsWith(">"))  { op = ">";  required = c.substring(1).trim(); }
        else if (c.startsWith("<"))  { op = "<";  required = c.substring(1).trim(); }
        else if (c.startsWith("="))  { op = "=";  required = c.substring(1).trim(); }
        else                          { op = "~";  required = c; }

        int cmp = compare(candidate, required);
        return switch (op) {
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            case ">" -> cmp > 0;
            case "<" -> cmp < 0;
            case "=" -> cmp == 0;
            case "~" -> prefixMatch(candidate, required);
            default -> false;
        };
    }

    private static boolean prefixMatch(String candidate, String required) {
        String[] a = candidate.split("\\.");
        String[] b = required.split("\\.");
        if (a.length < b.length) return false;
        for (int i = 0; i < b.length; i++) {
            if (!a[i].equals(b[i])) return false;
        }
        return true;
    }

    private static int compare(String a, String b) {
        String[] as = a.split("\\.");
        String[] bs = b.split("\\.");
        int n = Math.max(as.length, bs.length);
        for (int i = 0; i < n; i++) {
            String ap = i < as.length ? as[i] : "0";
            String bp = i < bs.length ? bs[i] : "0";
            int cmp;
            try {
                cmp = Integer.compare(Integer.parseInt(ap), Integer.parseInt(bp));
            } catch (NumberFormatException e) {
                cmp = ap.compareTo(bp);
            }
            if (cmp != 0) return cmp;
        }
        return 0;
    }
}
