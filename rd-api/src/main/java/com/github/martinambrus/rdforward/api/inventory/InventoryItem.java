package com.github.martinambrus.rdforward.api.inventory;

/**
 * Immutable inventory slot value.
 *
 * <p>Item IDs are pre-Flattening Notch numerics (the same canonical form
 * used on the wire by Alpha, Beta, and Netty pre-1.13). The dispatch
 * layer in rd-server translates to per-version registry IDs at packet
 * write time via the existing {@code BlockStateMapper.toVxxxItemId}
 * tables; consumers of this API never see the post-Flattening form.
 *
 * @param itemId  Notch numeric item ID; 0 = air / empty slot
 * @param count   stack size; 0 with itemId &gt; 0 is treated as empty
 * @param damage  damage / metadata value
 */
public record InventoryItem(int itemId, int count, int damage) {

    public static final InventoryItem EMPTY = new InventoryItem(0, 0, 0);

    public InventoryItem(int itemId, int count) {
        this(itemId, count, 0);
    }

    public boolean isEmpty() {
        return itemId <= 0 || count <= 0;
    }
}
