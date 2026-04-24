package io.papermc.paper.datapack;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface DiscoveredDatapack {
    java.lang.String getName();
    net.kyori.adventure.text.Component getTitle();
    net.kyori.adventure.text.Component getDescription();
    boolean isRequired();
    io.papermc.paper.datapack.Datapack$Compatibility getCompatibility();
    java.util.Set getRequiredFeatures();
    io.papermc.paper.datapack.DatapackSource getSource();
}
