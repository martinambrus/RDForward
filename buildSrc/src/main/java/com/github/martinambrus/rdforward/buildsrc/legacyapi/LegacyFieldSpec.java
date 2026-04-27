package com.github.martinambrus.rdforward.buildsrc.legacyapi;

/**
 * One legacy static field to add to a compiled interface .class file.
 *
 * <p>Old Bukkit enums had short field names (e.g. {@code Sound.EXPLODE},
 * {@code Sound.BAT_TAKEOFF}) that were renamed to long namespaced names
 * around Bukkit 1.9 ({@code ENTITY_GENERIC_EXPLODE},
 * {@code ENTITY_BAT_TAKEOFF}). Plugins compiled against the old API
 * embed the short field name in their constant pool and crash with
 * {@link NoSuchFieldError} if only the modern names exist on the
 * stub. Adding the legacy fields with default {@code null} values lets
 * the plugin resolve the field reference; the resulting null is
 * forwarded to whatever Bukkit method the plugin invokes (mostly
 * {@code playSound} / {@code playEffect}, which already StubCallLog
 * the call).
 *
 * @param fieldName  the legacy short name (e.g. {@code "EXPLODE"})
 * @param descriptor JVM type descriptor of the field
 *                   ({@code "Lorg/bukkit/Sound;"})
 */
public record LegacyFieldSpec(String fieldName, String descriptor) {

    public static LegacyFieldSpec of(String fieldName, String descriptor) {
        return new LegacyFieldSpec(fieldName, descriptor);
    }
}
