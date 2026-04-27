package com.github.martinambrus.rdforward.api.inventory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the public {@link InventoryItem} record. Guards
 * the contract that the bridge + dispatch layers depend on (zero-count
 * is empty, two-arg ctor defaults damage to 0, EMPTY is a singleton).
 */
class InventoryItemTest {

    @Test
    void emptyConstantHasZeros() {
        assertEquals(0, InventoryItem.EMPTY.itemId());
        assertEquals(0, InventoryItem.EMPTY.count());
        assertEquals(0, InventoryItem.EMPTY.damage());
        assertTrue(InventoryItem.EMPTY.isEmpty());
    }

    @Test
    void emptyIsCachedSingleton() {
        // Same reference across multiple lookups — InventoryAdapter and
        // BukkitPlayerInventory both return EMPTY on miss, so equality
        // checks via `==` need to keep working.
        assertSame(InventoryItem.EMPTY, InventoryItem.EMPTY);
    }

    @Test
    void twoArgCtorDefaultsDamageToZero() {
        InventoryItem stone = new InventoryItem(1, 5);
        assertEquals(1, stone.itemId());
        assertEquals(5, stone.count());
        assertEquals(0, stone.damage());
    }

    @Test
    void zeroCountIsEmptyEvenWithItemId() {
        // Plugins occasionally pass count=0 stacks; the dispatch layer
        // must treat them as empty so the wire SetSlot does not
        // advertise a phantom item.
        InventoryItem ghost = new InventoryItem(4, 0, 0);
        assertTrue(ghost.isEmpty());
    }

    @Test
    void zeroItemIdIsEmptyRegardlessOfCount() {
        InventoryItem zeroId = new InventoryItem(0, 5, 0);
        assertTrue(zeroId.isEmpty());
    }

    @Test
    void negativeItemIdIsEmpty() {
        // -1 is the on-wire "empty slot" sentinel. The public API must
        // not consider it a valid stack.
        InventoryItem negative = new InventoryItem(-1, 1, 0);
        assertTrue(negative.isEmpty());
    }

    @Test
    void normalStackIsNotEmpty() {
        InventoryItem cobble = new InventoryItem(4, 64, 0);
        assertFalse(cobble.isEmpty());
    }

    @Test
    void recordEqualityAcrossInstances() {
        InventoryItem a = new InventoryItem(4, 64, 0);
        InventoryItem b = new InventoryItem(4, 64, 0);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentDamageBreaksEquality() {
        InventoryItem pristine = new InventoryItem(257, 1, 0);
        InventoryItem worn = new InventoryItem(257, 1, 100);
        assertNotEquals(pristine, worn);
    }
}
