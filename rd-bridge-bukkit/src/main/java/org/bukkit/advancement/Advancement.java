package org.bukkit.advancement;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Advancement extends org.bukkit.Keyed {
    java.util.Collection getCriteria();
    org.bukkit.advancement.AdvancementRequirements getRequirements();
    io.papermc.paper.advancement.AdvancementDisplay getDisplay();
    net.kyori.adventure.text.Component displayName();
    org.bukkit.advancement.Advancement getParent();
    java.util.Collection getChildren();
    org.bukkit.advancement.Advancement getRoot();
}
