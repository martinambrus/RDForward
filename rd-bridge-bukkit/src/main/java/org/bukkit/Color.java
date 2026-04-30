// @rdforward:preserve - real impl, not auto-generated stub
package org.bukkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable RGB(A) colour. Real implementation (not a stub) because plugins
 * like Essentials read colours from config and round-trip them through
 * {@code asRGB()} / {@code fromRGB(int)}; returning {@code null} from
 * {@code fromRGB} crashes any caller that chains {@code .asRGB()}.
 *
 * <p>Wire-compatible with Paper's {@code org.bukkit.Color} surface used by
 * Bukkit-style plugins: ARGB packing, range-checked factories, immutable
 * setters that return a new instance, and a {@link
 * org.bukkit.configuration.serialization.ConfigurationSerializable} map
 * shape that mirrors Bukkit's own.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class Color implements org.bukkit.configuration.serialization.ConfigurationSerializable {

    private static final int BIT_MASK = 0xff;

    public static final Color WHITE   = fromRGB(0xFFFFFF);
    public static final Color SILVER  = fromRGB(0xC0C0C0);
    public static final Color GRAY    = fromRGB(0x808080);
    public static final Color BLACK   = fromRGB(0x000000);
    public static final Color RED     = fromRGB(0xFF0000);
    public static final Color MAROON  = fromRGB(0x800000);
    public static final Color YELLOW  = fromRGB(0xFFFF00);
    public static final Color OLIVE   = fromRGB(0x808000);
    public static final Color LIME    = fromRGB(0x00FF00);
    public static final Color GREEN   = fromRGB(0x008000);
    public static final Color AQUA    = fromRGB(0x00FFFF);
    public static final Color TEAL    = fromRGB(0x008080);
    public static final Color BLUE    = fromRGB(0x0000FF);
    public static final Color NAVY    = fromRGB(0x000080);
    public static final Color FUCHSIA = fromRGB(0xFF00FF);
    public static final Color PURPLE  = fromRGB(0x800080);
    public static final Color ORANGE  = fromRGB(0xFFA500);

    private final byte alpha;
    private final byte red;
    private final byte green;
    private final byte blue;

    /** Default alpha=255, all colour channels=0. Public no-arg ctor exists
     *  on real Bukkit Color for serializer compatibility. */
    public Color() { this(255, 0, 0, 0); }

    private Color(int alpha, int red, int green, int blue) {
        check(alpha, "alpha");
        check(red, "red");
        check(green, "green");
        check(blue, "blue");
        this.alpha = (byte) alpha;
        this.red = (byte) red;
        this.green = (byte) green;
        this.blue = (byte) blue;
    }

    private static void check(int v, String name) {
        if (v < 0 || v > 255) {
            throw new IllegalArgumentException(name + " (" + v + ") is not in range 0-255");
        }
    }

    public static Color fromARGB(int alpha, int red, int green, int blue) {
        return new Color(alpha, red, green, blue);
    }

    public static Color fromRGB(int red, int green, int blue) {
        return new Color(255, red, green, blue);
    }

    public static Color fromBGR(int blue, int green, int red) {
        return new Color(255, red, green, blue);
    }

    public static Color fromRGB(int rgb) {
        if ((rgb & 0xFF000000) != 0) {
            throw new IllegalArgumentException("Extranous data in: " + rgb + ". Use fromARGB for parsing ARGB ints.");
        }
        return fromRGB((rgb >> 16) & BIT_MASK, (rgb >> 8) & BIT_MASK, rgb & BIT_MASK);
    }

    public static Color fromARGB(int argb) {
        return fromARGB((argb >> 24) & BIT_MASK, (argb >> 16) & BIT_MASK,
                (argb >> 8) & BIT_MASK, argb & BIT_MASK);
    }

    public static Color fromBGR(int bgr) {
        if ((bgr & 0xFF000000) != 0) {
            throw new IllegalArgumentException("Extranous data in: " + bgr + ". Use fromARGB for parsing ARGB ints.");
        }
        return fromBGR((bgr >> 16) & BIT_MASK, (bgr >> 8) & BIT_MASK, bgr & BIT_MASK);
    }

    public int getAlpha() { return BIT_MASK & alpha; }
    public int getRed()   { return BIT_MASK & red;   }
    public int getGreen() { return BIT_MASK & green; }
    public int getBlue()  { return BIT_MASK & blue;  }

    public Color setAlpha(int alpha) { return fromARGB(alpha, getRed(), getGreen(), getBlue()); }
    public Color setRed(int red)     { return fromARGB(getAlpha(), red, getGreen(), getBlue()); }
    public Color setGreen(int green) { return fromARGB(getAlpha(), getRed(), green, getBlue()); }
    public Color setBlue(int blue)   { return fromARGB(getAlpha(), getRed(), getGreen(), blue); }

    public int asRGB()  { return (getRed() << 16) | (getGreen() << 8) | getBlue(); }
    public int asARGB() { return (getAlpha() << 24) | (getRed() << 16) | (getGreen() << 8) | getBlue(); }
    public int asBGR()  { return (getBlue() << 16) | (getGreen() << 8) | getRed(); }

    /** Mixing not implemented in stub — returns {@code this}. */
    public Color mixDyes(DyeColor[] dyes) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null,
                "org.bukkit.Color.mixDyes([Lorg/bukkit/DyeColor;)Lorg/bukkit/Color;");
        return this;
    }

    /** Mixing not implemented in stub — returns {@code this}. */
    public Color mixColors(Color[] colors) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null,
                "org.bukkit.Color.mixColors([Lorg/bukkit/Color;)Lorg/bukkit/Color;");
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Color)) return false;
        Color other = (Color) o;
        return getAlpha() == other.getAlpha()
                && getRed() == other.getRed()
                && getGreen() == other.getGreen()
                && getBlue() == other.getBlue();
    }

    @Override
    public int hashCode() { return asARGB() ^ Color.class.hashCode(); }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> m = new HashMap<>();
        m.put("ALPHA", getAlpha());
        m.put("RED", getRed());
        m.put("BLUE", getBlue());
        m.put("GREEN", getGreen());
        return m;
    }

    public static Color deserialize(Map<String, Object> map) {
        return fromARGB(asInt("ALPHA", map, 255),
                asInt("RED", map, 0), asInt("GREEN", map, 0), asInt("BLUE", map, 0));
    }

    private static int asInt(String key, Map<String, Object> map, int def) {
        Object v = map.get(key);
        return v instanceof Number ? ((Number) v).intValue() : def;
    }

    @Override
    public String toString() {
        return "Color:[argb0x" + Integer.toHexString(asARGB()).toUpperCase() + "]";
    }
}
