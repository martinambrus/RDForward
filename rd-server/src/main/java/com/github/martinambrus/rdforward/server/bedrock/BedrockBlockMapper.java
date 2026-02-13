package com.github.martinambrus.rdforward.server.bedrock;

import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleBlockDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Maps internal block IDs (0-91) to Bedrock runtime IDs and vice versa.
 *
 * At startup, loads string identifiers from block-mappings.properties and
 * resolves them against the codec's block palette to get runtime IDs.
 * Unmapped internal IDs fall back to air (runtime ID 0).
 */
public class BedrockBlockMapper {

    private final BlockDefinition[] internalToDefinition;
    private final Map<Integer, Integer> runtimeToInternal = new HashMap<>();
    private final BlockDefinition airDefinition;

    /**
     * Initialize the mapper using the vanilla block palette.
     *
     * @param blockStates the vanilla block states list from block_palette.nbt
     */
    public BedrockBlockMapper(List<NbtMap> blockStates) {
        internalToDefinition = new BlockDefinition[92];

        // Build a lookup: block string identifier -> first runtime ID (sequential index in sorted palette)
        // Only keep the first occurrence of each name (default block state)
        Map<String, BlockDefinition> paletteByName = new HashMap<>();
        for (int i = 0; i < blockStates.size(); i++) {
            NbtMap entry = blockStates.get(i);
            String name = entry.getString("name", "");
            if (!name.isEmpty() && !paletteByName.containsKey(name)) {
                NbtMap states = entry.getCompound("states");
                paletteByName.put(name, new SimpleBlockDefinition(name, i, states));
            }
        }

        airDefinition = paletteByName.getOrDefault("minecraft:air",
                new SimpleBlockDefinition("minecraft:air", 0, NbtMap.EMPTY));

        // Load mappings from properties file
        Properties props = new Properties();
        try (InputStream is = getClass().getResourceAsStream("/bedrock/block-mappings.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("Failed to load Bedrock block mappings: " + e.getMessage());
        }

        // Resolve each internal ID -> Bedrock identifier -> runtime ID
        for (String key : props.stringPropertyNames()) {
            try {
                int internalId = Integer.parseInt(key);
                String bedrockName = props.getProperty(key);
                BlockDefinition def = paletteByName.get(bedrockName);
                if (def != null && internalId >= 0 && internalId < internalToDefinition.length) {
                    internalToDefinition[internalId] = def;
                    runtimeToInternal.put(def.getRuntimeId(), internalId);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        // Fill gaps with air and log the mappings
        int mapped = 0;
        for (int i = 0; i < internalToDefinition.length; i++) {
            if (internalToDefinition[i] == null) {
                internalToDefinition[i] = airDefinition;
            } else if (internalToDefinition[i] != airDefinition || i == 0) {
                mapped++;
            }
        }
        System.out.println("[Bedrock] Block mapper: " + mapped + " blocks mapped");
        // Log first few mappings for verification
        for (int i = 0; i < Math.min(10, internalToDefinition.length); i++) {
            BlockDefinition def = internalToDefinition[i];
            System.out.println("[Bedrock]   ID " + i + " -> "
                    + ((SimpleBlockDefinition) def).getIdentifier()
                    + " (runtime=" + def.getRuntimeId() + ")");
        }

    }

    /**
     * Get the Bedrock BlockDefinition for an internal block ID.
     */
    public BlockDefinition toDefinition(int internalId) {
        if (internalId < 0 || internalId >= internalToDefinition.length) {
            return airDefinition;
        }
        return internalToDefinition[internalId];
    }

    /**
     * Get the Bedrock runtime ID for an internal block ID.
     */
    public int toRuntimeId(int internalId) {
        return toDefinition(internalId).getRuntimeId();
    }

    /**
     * Convert a Bedrock runtime ID back to an internal block ID.
     * Returns 0 (air) if the runtime ID is not mapped.
     */
    public int toInternal(int runtimeId) {
        return runtimeToInternal.getOrDefault(runtimeId, 0);
    }

    /**
     * Get the air block definition.
     */
    public BlockDefinition getAirDefinition() {
        return airDefinition;
    }
}
