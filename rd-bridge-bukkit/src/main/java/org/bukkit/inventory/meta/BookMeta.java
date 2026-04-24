package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BookMeta extends org.bukkit.inventory.meta.WritableBookMeta, net.kyori.adventure.inventory.Book {
    boolean hasTitle();
    java.lang.String getTitle();
    boolean setTitle(java.lang.String arg0);
    boolean hasAuthor();
    java.lang.String getAuthor();
    void setAuthor(java.lang.String arg0);
    boolean hasGeneration();
    org.bukkit.inventory.meta.BookMeta$Generation getGeneration();
    void setGeneration(org.bukkit.inventory.meta.BookMeta$Generation arg0);
    org.bukkit.inventory.meta.BookMeta clone();
    java.lang.String getPage(int arg0);
    void setPage(int arg0, java.lang.String arg1);
    java.util.List getPages();
    void setPages(java.util.List arg0);
    void setPages(java.lang.String[] arg0);
    void addPage(java.lang.String[] arg0);
    net.kyori.adventure.text.Component title();
    org.bukkit.inventory.meta.BookMeta title(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.text.Component author();
    org.bukkit.inventory.meta.BookMeta author(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.text.Component page(int arg0);
    void page(int arg0, net.kyori.adventure.text.Component arg1);
    void addPages(net.kyori.adventure.text.Component[] arg0);
    org.bukkit.inventory.meta.BookMeta$BookMetaBuilder toBuilder();
    org.bukkit.inventory.meta.BookMeta$Spigot spigot();
}
