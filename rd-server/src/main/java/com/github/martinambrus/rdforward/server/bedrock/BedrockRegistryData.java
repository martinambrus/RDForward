package com.github.martinambrus.rdforward.server.bedrock;

import org.cloudburstmc.nbt.NBTInputStream;
import org.cloudburstmc.nbt.NbtMap;
import org.cloudburstmc.nbt.NbtUtils;

import java.io.InputStream;

/**
 * Loads vanilla Bedrock registry data (biome definitions, entity identifiers)
 * from bundled resource files sourced from CloudburstMC/Data and GeyserMC.
 *
 * Biome definitions use the old NbtMap format (biome_definitions.dat from GeyserMC)
 * paired with the v313 serializer override, because the v800+ structured serializer
 * produces output that crashes the Windows Bedrock client.
 */
public class BedrockRegistryData {

    private static final String BIOME_DEFS_PATH = "/bedrock/biome_definitions.dat";
    private static final String ENTITY_IDS_PATH = "/bedrock/entity_identifiers.dat";

    private final NbtMap biomeDefinitions;
    private final NbtMap entityIdentifiers;

    public BedrockRegistryData() {
        this.biomeDefinitions = loadNbtResource(BIOME_DEFS_PATH, "biome definitions");
        this.entityIdentifiers = loadNbtResource(ENTITY_IDS_PATH, "entity identifiers");
    }

    public NbtMap getBiomeDefinitions() {
        return biomeDefinitions;
    }

    public NbtMap getEntityIdentifiers() {
        return entityIdentifiers;
    }

    private NbtMap loadNbtResource(String path, String description) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("[Bedrock] Missing resource: " + path);
                return NbtMap.EMPTY;
            }

            try (NBTInputStream nbtIn = NbtUtils.createNetworkReader(is)) {
                Object tag = nbtIn.readTag();
                if (tag instanceof NbtMap) {
                    NbtMap nbtMap = (NbtMap) tag;
                    System.out.println("[Bedrock] Loaded " + description
                            + " (" + nbtMap.size() + " entries)");
                    return nbtMap;
                }
                System.err.println("[Bedrock] " + description + " tag is not NbtMap: "
                        + tag.getClass().getSimpleName());
                return NbtMap.EMPTY;
            }
        } catch (Exception e) {
            System.err.println("[Bedrock] Failed to load " + description + ": " + e.getMessage());
            e.printStackTrace();
            return NbtMap.EMPTY;
        }
    }
}
