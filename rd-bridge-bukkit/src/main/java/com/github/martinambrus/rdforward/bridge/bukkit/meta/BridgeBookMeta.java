package com.github.martinambrus.rdforward.bridge.bukkit.meta;

import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta$BookMetaBuilder;
import org.bukkit.inventory.meta.BookMeta$Generation;
import org.bukkit.inventory.meta.BookMeta$Spigot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Book-shaped {@link BridgeItemMeta} subclass returned by
 * {@link org.bukkit.inventory.ItemStack#getItemMeta()} for book materials.
 * Plugins like EpicQuest cast the meta to {@link BookMeta} to set
 * title, author and pages.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BridgeBookMeta extends BridgeItemMeta implements BookMeta {

    private String title;
    private String author;
    private BookMeta$Generation generation;
    private final List<String> pages = new ArrayList<>();
    private final BookMeta$Spigot spigot = new BookMeta$Spigot();

    // --- BookMeta ---

    @Override public boolean hasTitle() { return title != null; }
    @Override public String getTitle() { return title; }
    @Override public boolean setTitle(String t) { this.title = t; return true; }
    @Override public boolean hasAuthor() { return author != null; }
    @Override public String getAuthor() { return author; }
    @Override public void setAuthor(String a) { this.author = a; }
    @Override public boolean hasGeneration() { return generation != null; }
    @Override public BookMeta$Generation getGeneration() { return generation; }
    @Override public void setGeneration(BookMeta$Generation g) { this.generation = g; }

    // --- WritableBookMeta ---

    @Override public boolean hasPages() { return !pages.isEmpty(); }
    @Override public String getPage(int i) { return i >= 0 && i < pages.size() ? pages.get(i) : ""; }
    @Override public void setPage(int i, String p) { if (i >= 0 && i < pages.size()) pages.set(i, p); }
    @Override public List getPages() { return new ArrayList<>(pages); }
    @Override public void setPages(List p) { pages.clear(); if (p != null) pages.addAll(p); }
    @Override public void setPages(String[] p) { pages.clear(); if (p != null) pages.addAll(Arrays.asList(p)); }
    @Override public void addPage(String[] p) { if (p != null) pages.addAll(Arrays.asList(p)); }
    @Override public int getPageCount() { return pages.size(); }

    // --- Adventure Book (net.kyori.adventure.inventory.Book) ---

    @Override public List pages() { return new ArrayList<>(pages); }
    @Override public net.kyori.adventure.inventory.Book pages(net.kyori.adventure.text.Component[] p) { return this; }
    @Override public net.kyori.adventure.inventory.Book pages(List p) { return this; }
    @Override public net.kyori.adventure.text.Component title() { return null; }
    @Override public BookMeta title(net.kyori.adventure.text.Component c) { return this; }
    @Override public net.kyori.adventure.text.Component author() { return null; }
    @Override public BookMeta author(net.kyori.adventure.text.Component c) { return this; }
    @Override public net.kyori.adventure.text.Component page(int i) { return null; }
    @Override public void page(int i, net.kyori.adventure.text.Component c) {}
    @Override public void addPages(net.kyori.adventure.text.Component[] p) {}

    // --- Builder / Spigot ---

    @Override public BookMeta$BookMetaBuilder toBuilder() { return null; }
    @Override public BookMeta$Spigot spigot() { return spigot; }

    // --- Clone ---

    @Override
    public BookMeta clone() {
        BridgeBookMeta copy = (BridgeBookMeta) super.clone();
        copy.pages.addAll(this.pages);
        return copy;
    }
}
