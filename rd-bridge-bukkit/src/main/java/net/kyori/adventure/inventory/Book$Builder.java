package net.kyori.adventure.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Book$Builder extends net.kyori.adventure.builder.AbstractBuilder, net.kyori.adventure.util.Buildable$Builder {
    net.kyori.adventure.inventory.Book$Builder title(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.inventory.Book$Builder author(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.inventory.Book$Builder addPage(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.inventory.Book$Builder pages(net.kyori.adventure.text.Component[] arg0);
    net.kyori.adventure.inventory.Book$Builder pages(java.util.Collection arg0);
    net.kyori.adventure.inventory.Book build();
}
