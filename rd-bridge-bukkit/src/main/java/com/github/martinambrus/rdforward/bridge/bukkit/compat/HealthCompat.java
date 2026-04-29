package com.github.martinambrus.rdforward.bridge.bukkit.compat;

import org.bukkit.entity.Damageable;

/**
 * Static shim invoked by {@link LegacyHealthTransformer}-rewritten plugin
 * bytecode. Pre-1.6 Bukkit declared {@code int getHealth()} / {@code void
 * setHealth(int)} on {@code Player} and {@code LivingEntity}; modern
 * (1.6+) Bukkit changed those to {@code double}. Plugins compiled
 * against the legacy API emit {@code INVOKEINTERFACE Player.getHealth()I}
 * which doesn't resolve against the modern interface, so the JVM throws
 * {@code NoSuchMethodError: 'int org.bukkit.entity.Player.getHealth()'}.
 *
 * <p>The transformer rewrites those call sites to {@code INVOKESTATIC}
 * targets here; we round-trip via the modern double method.
 */
public final class HealthCompat {

    private HealthCompat() {}

    public static int getHealth(Damageable d) {
        return (int) Math.round(d.getHealth());
    }

    public static void setHealth(Damageable d, int health) {
        d.setHealth(health);
    }

    public static int getMaxHealth(Damageable d) {
        return (int) Math.round(d.getMaxHealth());
    }

    public static void setMaxHealth(Damageable d, int max) {
        d.setMaxHealth(max);
    }
}
