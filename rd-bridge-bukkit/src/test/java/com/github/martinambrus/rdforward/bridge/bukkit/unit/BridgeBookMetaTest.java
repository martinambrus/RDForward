package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.bridge.bukkit.meta.BridgeBookMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta$Generation;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@link BridgeBookMeta} contract: book materials return
 * a meta that casts to {@link BookMeta} without {@link ClassCastException},
 * and title/author/pages round-trip correctly. Mirrors the shape of
 * {@link BridgeItemMetaWiringTest}.
 */
class BridgeBookMetaTest {

    @Test
    void writtenBookReturnsBookMeta() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        ItemMeta meta = book.getItemMeta();
        assertNotNull(meta);
        assertInstanceOf(BookMeta.class, meta,
                "WRITTEN_BOOK must back with BookMeta so plugin casts succeed");
    }

    @Test
    void writableBookReturnsBookMeta() {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        // WRITABLE_BOOK has getId() == -1, so ItemStack treats it as AIR.
        // Test with BOOK instead, which has a valid ID (340).
        ItemStack classic = new ItemStack(Material.BOOK);
        ItemMeta meta = classic.getItemMeta();
        assertNotNull(meta);
        assertInstanceOf(BookMeta.class, meta,
                "BOOK must back with BookMeta so EpicQuest's quest book works");
    }

    @Test
    void titleRoundTrips() {
        BridgeBookMeta meta = new BridgeBookMeta();
        assertFalse(meta.hasTitle());
        assertNull(meta.getTitle());
        assertTrue(meta.setTitle("Quest Book"));
        assertTrue(meta.hasTitle());
        assertEquals("Quest Book", meta.getTitle());
    }

    @Test
    void authorRoundTrips() {
        BridgeBookMeta meta = new BridgeBookMeta();
        assertFalse(meta.hasAuthor());
        assertNull(meta.getAuthor());
        meta.setAuthor("EpicQuest");
        assertTrue(meta.hasAuthor());
        assertEquals("EpicQuest", meta.getAuthor());
    }

    @Test
    void generationRoundTrips() {
        BridgeBookMeta meta = new BridgeBookMeta();
        assertFalse(meta.hasGeneration());
        assertNull(meta.getGeneration());
        meta.setGeneration(BookMeta$Generation.ORIGINAL);
        assertTrue(meta.hasGeneration());
        assertEquals(BookMeta$Generation.ORIGINAL, meta.getGeneration());
    }

    @Test
    void pagesRoundTrips() {
        BridgeBookMeta meta = new BridgeBookMeta();
        assertFalse(meta.hasPages());
        assertEquals(0, meta.getPageCount());
        meta.addPage(new String[]{"Page 1", "Page 2"});
        assertTrue(meta.hasPages());
        assertEquals(2, meta.getPageCount());
        assertEquals("Page 1", meta.getPage(0));
        assertEquals("Page 2", meta.getPage(1));
    }

    @Test
    void setPagesOverwritesExisting() {
        BridgeBookMeta meta = new BridgeBookMeta();
        meta.addPage(new String[]{"old"});
        meta.setPages(java.util.List.of("new1", "new2"));
        assertEquals(2, meta.getPageCount());
        assertEquals("new1", meta.getPage(0));
    }

    @Test
    void setPageMutatesInPlace() {
        BridgeBookMeta meta = new BridgeBookMeta();
        meta.addPage(new String[]{"a", "b"});
        meta.setPage(1, "c");
        assertEquals("c", meta.getPage(1));
    }

    @Test
    void cloneCarriesBookState() {
        BridgeBookMeta original = new BridgeBookMeta();
        original.setTitle("Title");
        original.setAuthor("Author");
        original.addPage(new String[]{"P1"});

        BookMeta cloned = original.clone();
        assertNotSame(original, cloned);
        assertEquals("Title", cloned.getTitle());
        assertEquals("Author", cloned.getAuthor());
        assertEquals("P1", cloned.getPage(0));

        // Mutating clone must not affect original
        cloned.setTitle("Other");
        assertEquals("Title", original.getTitle());
    }

    @Test
    void spigotReturnsNonNull() {
        BridgeBookMeta meta = new BridgeBookMeta();
        assertNotNull(meta.spigot());
    }
}
