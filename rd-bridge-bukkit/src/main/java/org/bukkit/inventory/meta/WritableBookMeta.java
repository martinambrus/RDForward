package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface WritableBookMeta extends org.bukkit.inventory.meta.ItemMeta {
    boolean hasPages();
    java.lang.String getPage(int arg0);
    void setPage(int arg0, java.lang.String arg1);
    java.util.List getPages();
    void setPages(java.util.List arg0);
    void setPages(java.lang.String[] arg0);
    void addPage(java.lang.String[] arg0);
    int getPageCount();
    org.bukkit.inventory.meta.WritableBookMeta clone();
}
