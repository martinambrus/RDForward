package com.github.martinambrus.rdforward.server.bedrock;

import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Maps internal pre-Flattening Notch item IDs to Bedrock {@link
 * ItemDefinition} entries from the runtime item registry. The lookup
 * reuses {@code /bedrock/block-mappings.properties} since the same
 * identifier strings (e.g. {@code minecraft:cobblestone}) name both
 * the block and the item form on Bedrock — the registry resolves the
 * name to an item runtime ID.
 *
 * <p>IDs without a mapping fall back to {@code minecraft:air}, matching
 * the convention used by {@link BedrockBlockMapper}.
 */
public class BedrockItemMapper {

    private final ItemDefinition airDefinition;
    private final Map<Integer, ItemDefinition> notchIdToDefinition = new HashMap<>();

    public BedrockItemMapper(List<ItemDefinition> itemDefinitionList) {
        // Build a name -> definition lookup once. The CloudBurst registry
        // only exposes runtime-ID lookups, but our mapping file is keyed
        // by Bedrock identifier string (e.g. "minecraft:cobblestone").
        Map<String, ItemDefinition> byName = new HashMap<>(itemDefinitionList.size());
        for (ItemDefinition def : itemDefinitionList) {
            String id = def.getIdentifier();
            if (id != null) byName.putIfAbsent(id, def);
        }

        ItemDefinition air = byName.get("minecraft:air");
        this.airDefinition = air != null ? air
                : new SimpleItemDefinition("minecraft:air", 0, false);

        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/bedrock/block-mappings.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("[BedrockItemMapper] Failed to load block-mappings.properties: " + e.getMessage());
        }

        for (String key : props.stringPropertyNames()) {
            try {
                int notchId = Integer.parseInt(key);
                String bedrockName = props.getProperty(key);
                ItemDefinition def = byName.get(bedrockName);
                if (def != null) {
                    notchIdToDefinition.put(notchId, def);
                }
            } catch (NumberFormatException ignored) {
                // skip non-numeric keys
            }
        }
    }

    /**
     * @return the Bedrock {@link ItemDefinition} for {@code notchId}, or
     *         the air definition when no mapping is registered.
     */
    public ItemDefinition toDefinition(int notchId) {
        ItemDefinition def = notchIdToDefinition.get(notchId);
        return def != null ? def : airDefinition;
    }

    public ItemDefinition getAirDefinition() {
        return airDefinition;
    }
}
