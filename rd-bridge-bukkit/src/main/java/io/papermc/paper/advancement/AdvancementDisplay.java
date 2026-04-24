package io.papermc.paper.advancement;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AdvancementDisplay {
    io.papermc.paper.advancement.AdvancementDisplay$Frame frame();
    net.kyori.adventure.text.Component title();
    net.kyori.adventure.text.Component description();
    org.bukkit.inventory.ItemStack icon();
    boolean doesShowToast();
    boolean doesAnnounceToChat();
    boolean isHidden();
    org.bukkit.NamespacedKey backgroundPath();
    net.kyori.adventure.text.Component displayName();
}
