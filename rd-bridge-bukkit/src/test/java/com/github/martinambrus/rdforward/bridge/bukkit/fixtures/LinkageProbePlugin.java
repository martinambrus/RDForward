package com.github.martinambrus.rdforward.bridge.bukkit.fixtures;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

/**
 * Synthetic plugin for {@code BukkitLinkageTest}. Its {@code onEnable}
 * intentionally touches a broad cross-section of generated stubs so that
 * class-load failures (missing transitive classes, mismatched
 * signatures, {@code NoClassDefFoundError}, {@code NoSuchMethodError})
 * surface as a test failure. The plugin does no real work — each call
 * site exists only to force the JVM to resolve the referenced class at
 * runtime.
 */
public class LinkageProbePlugin extends JavaPlugin {

    public static final String PROP_ENABLE_OK = "rdforward.linkage.enable_ok";
    public static final String PROP_TOUCH_COUNT = "rdforward.linkage.touch_count";
    public static final String PROP_FAILURE = "rdforward.linkage.failure";

    @Override
    public void onEnable() {
        int touched = 0;
        try {
            ItemStack stone = new ItemStack(Material.STONE);
            ItemStack cobble = new ItemStack(Material.COBBLESTONE);
            ItemFlag hideFlag = ItemFlag.HIDE_ATTRIBUTES;
            touched += 3;

            NamespacedKey key = new NamespacedKey("rdforward", "probe");
            NamespacedKey mcKey = NamespacedKey.minecraft("stone");
            touched += 2;

            Vector v = new Vector(1.0, 2.0, 3.0);
            Vector mid = v.clone();
            touched += 2;

            BoundingBox box = BoundingBox.of(new Vector(0, 0, 0), new Vector(1, 1, 1));
            touched++;

            BlockFace face = BlockFace.NORTH;
            EntityType type = EntityType.ZOMBIE;
            Attribute attr = Attribute.MAX_HEALTH;
            touched += 3;

            PotionEffectType potType = PotionEffectType.SPEED;
            PotionEffect effect = new PotionEffect(potType, 200, 1);
            touched += 2;

            Component comp = Component.text("linkage-probe");
            touched++;

            Bukkit.broadcastMessage("probe");
            touched++;

            // Force resolution of PluginManager + Plugin stubs without
            // actually invoking registerEvents (our JavaPlugin preserved
            // facade doesn't extend Plugin, so the 2-arg overload can't
            // accept `this`). Listener wiring flows through the
            // preserved JavaPlugin.registerListener path below.
            PluginManager pm = Bukkit.getServer().getPluginManager();
            Plugin pluginRef = null;
            if (pm == null || pluginRef != null) throw new IllegalStateException();
            registerListener(new ProbeListener());
            touched += 2;

            System.setProperty(PROP_TOUCH_COUNT, String.valueOf(touched));
            System.setProperty(PROP_ENABLE_OK, "true");
        } catch (Throwable t) {
            System.setProperty(PROP_FAILURE,
                    t.getClass().getName() + ":" + t.getMessage());
            throw t;
        }
    }

    private static final class ProbeListener implements Listener {
        @EventHandler
        public void onPlace(BlockPlaceEvent ev) {
            // never invoked in the link probe, exists only to force
            // BlockPlaceEvent resolution at class-load time.
        }
    }
}
