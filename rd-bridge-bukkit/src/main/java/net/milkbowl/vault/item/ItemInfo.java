// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.milkbowl.vault.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Vault's item descriptor. Mirrors the public Vault 1.6 contract so plugins
 * compiled against Vault link cleanly. Carries a Bukkit Material + optional
 * sub-type (durability/data value).
 */
public class ItemInfo {

    private final Material material;
    private final short subTypeId;
    private final String name;
    private final String[][] search;

    public ItemInfo(Material material, short subTypeId, String name, String[][] search) {
        this.material = material;
        this.subTypeId = subTypeId;
        this.name = name != null ? name : material != null ? material.name() : "UNKNOWN";
        this.search = search;
    }

    public ItemInfo(Material material, String name, String[][] search) {
        this(material, (short) 0, name, search);
    }

    public Material getType() {
        return material;
    }

    public short getSubTypeId() {
        return subTypeId;
    }

    public int getId() {
        return material != null ? material.getId() : -1;
    }

    public String getName() {
        return name;
    }

    public String[][] getSearch() {
        return search;
    }

    public ItemStack toStack() {
        if (material == null) return null;
        return new ItemStack(material, 1, subTypeId);
    }

    public boolean isDurable() {
        return material != null && material.getMaxDurability() > 0;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemInfo)) return false;
        ItemInfo that = (ItemInfo) o;
        return subTypeId == that.subTypeId && material == that.material;
    }

    @Override
    public int hashCode() {
        return 31 * (material != null ? material.hashCode() : 0) + subTypeId;
    }
}
