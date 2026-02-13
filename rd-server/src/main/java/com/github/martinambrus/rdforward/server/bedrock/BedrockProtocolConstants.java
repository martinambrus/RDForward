package com.github.martinambrus.rdforward.server.bedrock;

import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtType;
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec;
import org.cloudburstmc.protocol.bedrock.codec.v924.Bedrock_v924;
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleBlockDefinition;
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleItemDefinition;
import org.cloudburstmc.protocol.common.DefinitionRegistry;
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Constants for the Bedrock Edition protocol integration.
 */
public final class BedrockProtocolConstants {

    /**
     * The Bedrock codec version used by this server (1.26.0).
     * Uses the standard v924 codec — BiomeDefinitionListPacket is NOT sent
     * (the client has built-in biome definitions).
     */
    public static final BedrockCodec CODEC = Bedrock_v924.CODEC;

    /** Default Bedrock Edition port (UDP/RakNet). */
    public static final int DEFAULT_PORT = 19132;

    /** Compression threshold in bytes. Packets smaller than this are not compressed. */
    public static final int COMPRESSION_THRESHOLD = 512;

    /** Vanilla block definitions loaded from block_palette.nbt. */
    private static DefinitionRegistry<BlockDefinition> blockDefinitions;

    /** Vanilla item definitions loaded from runtime_item_states.json. */
    private static DefinitionRegistry<ItemDefinition> itemDefinitions;

    /** Vanilla block states list (for block mapper lookups). */
    private static List<NbtMap> vanillaBlockStates;

    /**
     * Get the vanilla item definition registry for setting on the codec helper.
     * Built from CloudburstMC/Data runtime_item_states.json.
     */
    public static synchronized DefinitionRegistry<ItemDefinition> getItemDefinitions() {
        if (itemDefinitions == null) {
            loadItemDefinitions();
        }
        return itemDefinitions;
    }

    /**
     * Get the vanilla block definition registry for setting on the codec helper.
     * Built from CloudburstMC/Data block_palette.nbt which contains the canonical
     * vanilla palette matching what the Bedrock client expects.
     */
    public static synchronized DefinitionRegistry<BlockDefinition> getBlockDefinitions() {
        if (blockDefinitions == null) {
            loadVanillaPalette();
        }
        return blockDefinitions;
    }

    /**
     * Get the vanilla block states list for looking up block names to runtime IDs.
     */
    public static synchronized List<NbtMap> getVanillaBlockStates() {
        if (vanillaBlockStates == null) {
            loadVanillaPalette();
        }
        return vanillaBlockStates;
    }

    private static void loadVanillaPalette() {
        try (InputStream is = BedrockProtocolConstants.class
                .getResourceAsStream("/bedrock/block_palette.nbt")) {
            if (is == null) {
                throw new IOException("block_palette.nbt not found in resources");
            }

            NBTInputStream nbtStream = new NBTInputStream(
                    new DataInputStream(new GZIPInputStream(is)), true, true);
            NbtMap root = (NbtMap) nbtStream.readTag();
            List<NbtMap> unsorted = root.getList("blocks", NbtType.COMPOUND);

            // Use file order directly — matches the client's built-in palette for this codec version
            vanillaBlockStates = new java.util.ArrayList<>(unsorted);

            System.out.println("[Bedrock] Loaded vanilla block palette ("
                    + vanillaBlockStates.size() + " block states)");

            // Build the definition registry: runtime ID = sequential index in sorted palette
            SimpleDefinitionRegistry.Builder<BlockDefinition> builder =
                    SimpleDefinitionRegistry.builder();
            for (int i = 0; i < vanillaBlockStates.size(); i++) {
                NbtMap state = vanillaBlockStates.get(i);
                String name = state.getString("name");
                NbtMap states = state.getCompound("states");
                builder.add(new SimpleBlockDefinition(name, i, states));
            }
            blockDefinitions = builder.build();
        } catch (IOException e) {
            System.err.println("[Bedrock] Failed to load block_palette.nbt: " + e.getMessage());
            // Fallback: minimal registry with just air
            blockDefinitions = SimpleDefinitionRegistry.<BlockDefinition>builder()
                    .add(new SimpleBlockDefinition("minecraft:air", 0, NbtMap.EMPTY))
                    .build();
            vanillaBlockStates = List.of(NbtMap.builder()
                    .putString("name", "minecraft:air")
                    .putCompound("states", NbtMap.EMPTY)
                    .build());
        }
    }

    /**
     * Load vanilla item definitions from runtime_item_states.json.
     * Format: JSON array of {"name":"minecraft:...", "id":N, "componentBased":false}
     */
    private static void loadItemDefinitions() {
        try (InputStream is = BedrockProtocolConstants.class
                .getResourceAsStream("/bedrock/runtime_item_states.json")) {
            if (is == null) {
                throw new IOException("runtime_item_states.json not found in resources");
            }

            // Read entire JSON file
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String json = sb.toString().trim();

            // Simple JSON array parser — each entry is {"name":"...","id":N,...,"componentBased":bool}
            SimpleDefinitionRegistry.Builder<ItemDefinition> builder =
                    SimpleDefinitionRegistry.builder();
            int count = 0;

            // Strip outer brackets
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length() - 1);

            // Split by },{ pattern to get individual entries
            String[] entries = json.split("\\}\\s*,\\s*\\{");
            for (String entry : entries) {
                // Clean up braces from splitting
                entry = entry.replace("{", "").replace("}", "").trim();
                if (entry.isEmpty()) continue;

                String name = extractJsonString(entry, "name");
                int id = extractJsonInt(entry, "id");
                boolean componentBased = extractJsonBoolean(entry, "componentBased");

                if (name != null) {
                    builder.add(new SimpleItemDefinition(name, id, componentBased));
                    count++;
                }
            }

            itemDefinitions = builder.build();
            System.out.println("[Bedrock] Loaded " + count + " item definitions from runtime_item_states.json");
        } catch (IOException e) {
            System.err.println("[Bedrock] Failed to load runtime_item_states.json: " + e.getMessage());
            // Fallback: empty registry
            itemDefinitions = SimpleDefinitionRegistry.<ItemDefinition>builder()
                    .add(new SimpleItemDefinition("minecraft:air", 0, false))
                    .build();
        }
    }

    private static String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return null;
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) return null;
        int quoteStart = json.indexOf('"', colonIdx + 1);
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        if (quoteStart < 0 || quoteEnd < 0) return null;
        return json.substring(quoteStart + 1, quoteEnd);
    }

    private static int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return 0;
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) return 0;
        int start = colonIdx + 1;
        while (start < json.length() && json.charAt(start) == ' ') start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try {
            return Integer.parseInt(json.substring(start, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean extractJsonBoolean(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return false;
        int colonIdx = json.indexOf(':', idx + pattern.length());
        if (colonIdx < 0) return false;
        String rest = json.substring(colonIdx + 1).trim();
        return rest.startsWith("true");
    }

    private BedrockProtocolConstants() {}
}
