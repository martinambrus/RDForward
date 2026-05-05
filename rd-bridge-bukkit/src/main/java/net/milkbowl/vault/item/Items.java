// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.milkbowl.vault.item;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Vault's item lookup utility. The real Vault hardcodes hundreds of items in a
 * static initializer. This stub builds the registry dynamically from our Bukkit
 * Material enum at class-load time, which keeps the implementation compact
 * while still resolving numeric IDs and name lookups the same way plugins
 * expect.
 *
 * <p>Methods match the Vault 1.6 API surface so plugins compiled against Vault
 * link cleanly.</p>
 */
public final class Items {

    private static final Logger LOG = Logger.getLogger(Items.class.getName());
    private static final CopyOnWriteArrayList<ItemInfo> items = new CopyOnWriteArrayList<>();

    static {
        initFromMaterialEnum();
    }

    private Items() {}

    private static void initFromMaterialEnum() {
        for (Material mat : Material.values()) {
            if (mat == null || mat.name().isEmpty()) continue;
            int id = mat.getId();
            if (id < 0) continue; // modern-only materials with no legacy ID
            String name = formatName(mat);
            items.add(new ItemInfo(mat, (short) 0, name, new String[][]{{mat.name().toLowerCase()}}));
        }
    }

    private static String formatName(Material mat) {
        String raw = mat.name().replace('_', ' ').toLowerCase();
        StringBuilder sb = new StringBuilder(raw.length());
        boolean capitalizeNext = true;
        for (char c : raw.toCharArray()) {
            if (c == ' ') {
                sb.append(' ');
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static List<ItemInfo> getItemList() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Parse a string into an ItemInfo. Accepts numeric IDs ("123" or "123:4"),
     * material names ("cobblestone"), or partial names.
     */
    public static ItemInfo itemByString(String string) {
        if (string == null || string.isEmpty()) return null;

        // numeric format: "123" or "123:4"
        try {
            String[] parts = string.split(":");
            int id = Integer.parseInt(parts[0]);
            short sub = parts.length > 1 ? Short.parseShort(parts[1]) : 0;
            return itemById(id, sub);
        } catch (NumberFormatException ignored) {
            // not numeric, fall through to name lookup
        }

        return itemByName(string);
    }

    public static ItemInfo itemById(int id) {
        for (ItemInfo info : items) {
            if (info.getType() != null && info.getType().getId() == id && info.getSubTypeId() == 0) {
                return info;
            }
        }
        // fallback: try Material.getMaterial
        Material mat = Material.getMaterial(id);
        if (mat != null) return fromMaterial(mat, (short) 0);
        return null;
    }

    public static ItemInfo itemById(int id, short subTypeId) {
        if (subTypeId == 0) return itemById(id);
        for (ItemInfo info : items) {
            if (info.getType() != null && info.getType().getId() == id && info.getSubTypeId() == subTypeId) {
                return info;
            }
        }
        Material mat = Material.getMaterial(id);
        if (mat != null) return fromMaterial(mat, subTypeId);
        return null;
    }

    public static ItemInfo itemByItem(ItemInfo item) {
        if (item == null) return null;
        return itemByType(item.getType(), item.getSubTypeId());
    }

    public static ItemInfo itemByType(Material mat) {
        return itemByType(mat, (short) 0);
    }

    public static ItemInfo itemByType(Material mat, short subTypeId) {
        if (mat == null) return null;
        for (ItemInfo info : items) {
            if (info.getType() == mat && info.getSubTypeId() == subTypeId) {
                return info;
            }
        }
        // if no entry with subtype, return base material entry
        if (subTypeId != 0) {
            return fromMaterial(mat, subTypeId);
        }
        return fromMaterial(mat, (short) 0);
    }

    public static ItemInfo itemByStack(ItemStack stack) {
        if (stack == null) return null;
        return itemByType(stack.getType(), stack.getDurability());
    }

    public static ItemInfo itemByName(ArrayList<String> strings) {
        return itemByName(join(strings, " "));
    }

    public static ItemInfo itemByName(String name) {
        if (name == null || name.isEmpty()) return null;

        String lower = name.toLowerCase().replace(' ', '_').replace('-', '_');

        // exact material name match
        Material exact = Material.getMaterial(lower.toUpperCase());
        if (exact != null) return itemByType(exact);

        // partial match against search terms and names
        for (ItemInfo info : items) {
            if (info.getName().equalsIgnoreCase(name)) return info;
            if (info.getName().toLowerCase().startsWith(lower)) return info;
            if (info.getType() != null && info.getType().name().toLowerCase().startsWith(lower)) return info;
            if (info.getSearch() != null) {
                for (String[] arr : info.getSearch()) {
                    for (String term : arr) {
                        if (term.equalsIgnoreCase(lower)) return info;
                    }
                }
            }
        }

        // fallback: try Bukkit's matchMaterial
        Material matched = Material.matchMaterial(name);
        if (matched != null) return fromMaterial(matched, (short) 0);

        return null;
    }

    public static ItemInfo[] itemByNames(ArrayList<String> names, boolean fuzzy) {
        List<ItemInfo> result = new ArrayList<>();
        for (String name : names) {
            if (fuzzy) {
                ItemInfo info = itemByName(name);
                if (info != null) result.add(info);
            } else {
                Material mat = Material.getMaterial(name.toUpperCase());
                if (mat != null) result.add(fromMaterial(mat, (short) 0));
            }
        }
        return result.toArray(new ItemInfo[0]);
    }

    public static ItemInfo[] itemsByName(String name, boolean fuzzy) {
        return itemByNames(new ArrayList<>(Arrays.asList(name.split(" "))), fuzzy);
    }

    private static ItemInfo fromMaterial(Material mat, short subTypeId) {
        String name = formatName(mat);
        return new ItemInfo(mat, subTypeId, name, new String[][]{{mat.name().toLowerCase()}});
    }

    public static String join(String[] arr, String sep) {
        return join(Arrays.asList(arr), sep);
    }

    public static String join(List<String> list, String sep) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(list.get(i));
        }
        return sb.toString();
    }
}
