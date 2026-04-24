package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BookMeta$BookMetaBuilder extends net.kyori.adventure.inventory.Book$Builder {
    org.bukkit.inventory.meta.BookMeta$BookMetaBuilder title(net.kyori.adventure.text.Component arg0);
    org.bukkit.inventory.meta.BookMeta$BookMetaBuilder author(net.kyori.adventure.text.Component arg0);
    org.bukkit.inventory.meta.BookMeta$BookMetaBuilder addPage(net.kyori.adventure.text.Component arg0);
    org.bukkit.inventory.meta.BookMeta$BookMetaBuilder pages(net.kyori.adventure.text.Component[] arg0);
    org.bukkit.inventory.meta.BookMeta$BookMetaBuilder pages(java.util.Collection arg0);
    org.bukkit.inventory.meta.BookMeta build();
}
