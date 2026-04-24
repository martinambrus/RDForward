package org.bukkit.generator;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface WorldInfo extends io.papermc.paper.world.flag.FeatureFlagSetHolder, org.bukkit.Keyed {
    java.lang.String getName();
    java.util.UUID getUID();
    org.bukkit.World$Environment getEnvironment();
    long getSeed();
    int getMinHeight();
    int getMaxHeight();
    org.bukkit.generator.BiomeProvider vanillaBiomeProvider();
}
