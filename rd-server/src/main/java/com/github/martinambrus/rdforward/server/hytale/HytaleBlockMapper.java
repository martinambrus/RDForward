package com.github.martinambrus.rdforward.server.hytale;

import io.netty.buffer.ByteBufAllocator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps internal block IDs (0-91) to Hytale block identifiers and vice versa.
 *
 * Hytale's asset system is server-driven: the server defines which blocks exist
 * via UpdateBlockTypes (ID 40) during login. The client then uses integer block IDs
 * (assigned by the server's asset registry) in chunk data and block update packets.
 *
 * For RDForward, we map each internal Classic block to the closest Hytale vanilla
 * block. The Hytale block identifiers (PascalCase like "Rock_Stone", "Soil_Dirt")
 * are from the decompiled Hytale server's block registry.
 *
 * Since we ARE the server, we assign Hytale block IDs starting at 0 (matching our
 * internal IDs). The client accepts whatever IDs we send in UpdateBlockTypes.
 */
public class HytaleBlockMapper {

    /** Map from internal block ID -> Hytale block identifier string. */
    private final String[] internalToHytaleName;

    /** Map from Hytale block ID -> internal block ID. */
    private final Map<Integer, Integer> hytaleIdToInternal;

    /** Number of registered blocks. */
    private final int blockCount;

    public HytaleBlockMapper() {
        internalToHytaleName = new String[92];
        hytaleIdToInternal = new HashMap<>();

        loadMappings();
        blockCount = countMappings();
    }

    private void loadMappings() {
        InputStream is = getClass().getResourceAsStream("/hytale/block-mappings.properties");
        if (is == null) {
            System.err.println("[Hytale] block-mappings.properties not found, using defaults");
            loadDefaults();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq < 0) continue;
                int internalId = Integer.parseInt(line.substring(0, eq).trim());
                String hytaleName = line.substring(eq + 1).trim();
                if (internalId >= 0 && internalId < 92) {
                    internalToHytaleName[internalId] = hytaleName;
                    // Hytale block ID = internal ID (we define the mapping)
                    hytaleIdToInternal.put(internalId, internalId);
                }
            }
        } catch (Exception e) {
            System.err.println("[Hytale] Error loading block-mappings.properties: " + e.getMessage());
            loadDefaults();
        }
    }

    private void loadDefaults() {
        // Core Classic blocks mapped to closest Hytale equivalents
        setMapping(0, "Empty");             // air
        setMapping(1, "Rock_Stone");        // stone
        setMapping(2, "Soil_Grass");        // grass
        setMapping(3, "Soil_Dirt");         // dirt
        setMapping(4, "Rock_Stone_Cobble"); // cobblestone
        setMapping(5, "Wood_Oak_Plank");    // planks
        setMapping(6, "Plant_Sapling");     // sapling
        setMapping(7, "Rock_Bedrock");      // bedrock
        setMapping(8, "Fluid_Water");       // water (flowing)
        setMapping(9, "Fluid_Water");       // water (still)
        setMapping(10, "Fluid_Lava");       // lava (flowing)
        setMapping(11, "Fluid_Lava");       // lava (still)
        setMapping(12, "Soil_Sand");        // sand
        setMapping(13, "Soil_Gravel");      // gravel
        setMapping(14, "Ore_Gold");         // gold ore
        setMapping(15, "Ore_Iron");         // iron ore
        setMapping(16, "Ore_Coal");         // coal ore
        setMapping(17, "Wood_Oak_Log");     // log
        setMapping(18, "Plant_Oak_Leaves"); // leaves
        setMapping(19, "Soil_Sponge");      // sponge
        setMapping(20, "Deco_Glass");       // glass
        setMapping(41, "Deco_Gold_Block");  // gold block
        setMapping(42, "Deco_Iron_Block");  // iron block
        setMapping(43, "Rock_Stone_Slab_Double"); // double slab
        setMapping(44, "Rock_Stone_Slab");  // slab
        setMapping(45, "Rock_Brick");       // bricks
        setMapping(46, "Prototype_TNT");    // TNT
        setMapping(47, "Furniture_Bookshelf"); // bookshelf
        setMapping(48, "Rock_Mossy_Cobble"); // mossy cobblestone
        setMapping(49, "Rock_Obsidian");    // obsidian
    }

    private void setMapping(int internalId, String hytaleName) {
        internalToHytaleName[internalId] = hytaleName;
        hytaleIdToInternal.put(internalId, internalId);
    }

    private int countMappings() {
        int count = 0;
        for (String name : internalToHytaleName) {
            if (name != null) count++;
        }
        return count;
    }

    /** Get the Hytale block name for an internal block ID. Returns "Empty" for unmapped. */
    public String toHytaleName(int internalId) {
        if (internalId >= 0 && internalId < 92 && internalToHytaleName[internalId] != null) {
            return internalToHytaleName[internalId];
        }
        return "Empty";
    }

    /**
     * Get the Hytale block ID for an internal block ID.
     * Currently maps all non-air blocks to ID 3 (our single defined block type)
     * because the client crashes with IndexOutOfRangeException if chunk data
     * references block IDs that exceed maxId in UpdateBlockTypes.
     * TODO: send full block type definitions for each unique block and use 1:1 IDs.
     */
    public int toHytaleId(int internalId) {
        if (internalId <= 0 || internalId >= 92) {
            return 0; // air
        }
        // All solid blocks → ID 1 (built-in Unknown block type).
        // Using built-in ID avoids sending custom block type entries that crash the renderer.
        return internalToHytaleName[internalId] != null ? 1 : 0;
    }

    /** Get the internal block ID for a Hytale block ID. Returns 0 for unmapped. */
    public int toInternal(int hytaleId) {
        Integer internal = hytaleIdToInternal.get(hytaleId);
        return internal != null ? internal : 0;
    }

    /**
     * Build the UpdateBlockTypes packet (ID 40) to send during login.
     *
     * Wire format (from decompiled UpdateBlockTypes.serialize + BlockType.serialize):
     *
     * UpdateBlockTypes fixed block (10 bytes):
     *   [0]  byte    nullBits (bit0 = blockTypes map present)
     *   [1]  byte    UpdateType enum (0=Init, 1=AddOrUpdate, 2=Remove)
     *   [2]  int32LE maxId
     *   [6]  byte    updateBlockTextures
     *   [7]  byte    updateModelTextures
     *   [8]  byte    updateModels
     *   [9]  byte    updateMapGeometry
     * Variable block: Map&lt;Integer, BlockType&gt; (VarInt count, then int32LE key + BlockType for each)
     *
     * BlockType fixed block (164 bytes):
     *   [0-3]   4x nullBits bytes
     *   [4]     byte    unknown
     *   [5]     byte    drawType (0=Empty, 1=Cube, ...)
     *   [6]     byte    material (0=Empty, ...)
     *   [7]     byte    opacity (0=Solid, ...)
     *   [8]     int32LE hitbox
     *   [12]    int32LE interactionHitbox
     *   [16]    floatLE modelScale
     *   [20]    byte    looping
     *   [21]    int32LE maxSupportDistance
     *   [25]    byte    blockSupportsRequiredFor
     *   [26]    byte    requiresAlphaBlending
     *   [27]    byte    cubeShadingMode
     *   [28]    byte    randomRotation
     *   [29]    byte    variantRotation
     *   [30]    byte    rotationYawPlacementOffset
     *   [31]    int32LE blockSoundSetIndex
     *   [35]    int32LE ambientSoundEventIndex
     *   [39-41] Color particleColor (3 bytes, zeroed if null)
     *   [42-45] ColorLight light (4 bytes, zeroed if null)
     *   [46-69] Tint tint (24 bytes, zeroed if null)
     *   [70-93] Tint biomeTint (24 bytes, zeroed if null)
     *   [94]    int32LE group
     *   [98-139]  BlockMovementSettings (42 bytes, zeroed if null)
     *   [140-141] BlockFlags (2 bytes, zeroed if null)
     *   [142-158] BlockPlacementSettings (17 bytes, zeroed if null)
     *   [159]   byte    ignoreSupportWhenPlaced
     *   [160]   int32LE transitionToTag
     *   [164-263] 25x int32LE variable field offset slots
     * Variable block start at byte 264: variable-length fields (strings, arrays, etc.)
     *
     * For our minimal blocks, we only set name (nullBits[1] bit0) and drawType.
     * All 25 variable field offsets default to -1 (null) except name.
     */
    public HytalePacketBuffer buildUpdateBlockTypesPacket(ByteBufAllocator alloc) {
        int maxId = 0;
        for (int i = 91; i >= 0; i--) {
            if (internalToHytaleName[i] != null) {
                maxId = i;
                break;
            }
        }

        HytalePacketBuffer pkt = HytalePacketBuffer.create(
                HytaleProtocolConstants.PACKET_UPDATE_BLOCK_TYPES, alloc);
        io.netty.buffer.ByteBuf buf = pkt.getBuf();

        // -- UpdateBlockTypes fixed block (10 bytes) --
        buf.writeByte(0x01);    // nullBits: bit0 = blockTypes map present
        buf.writeByte(0);       // type = Init (0)
        buf.writeIntLE(maxId);  // maxId
        buf.writeByte(1);       // updateBlockTextures = true
        buf.writeByte(1);       // updateModelTextures = true
        buf.writeByte(1);       // updateModels = true
        buf.writeByte(1);       // updateMapGeometry = true

        // -- Variable block: Map<Integer, BlockType> --
        writeVarInt(buf, blockCount);

        for (int i = 0; i <= 91; i++) {
            if (internalToHytaleName[i] == null) continue;

            // Map key: int32LE
            buf.writeIntLE(i);

            // BlockType serialization (matches decompiled BlockType.serialize exactly)
            serializeBlockType(buf, internalToHytaleName[i], i == 0);
        }

        return pkt;
    }

    /**
     * Serialize a minimal BlockType matching the exact decompiled wire format.
     * All nullable variable fields are set to null (-1 offset) except name.
     */
    private void serializeBlockType(io.netty.buffer.ByteBuf buf, String name, boolean isEmpty) {
        // 4 nullBits bytes. We only set name (nullBits[1] bit0 = 0x01)
        buf.writeByte(0x00);  // nullBits[0]: no particleColor/light/tint/biomeTint/movementSettings/flags/placementSettings/item
        buf.writeByte(0x01);  // nullBits[1]: bit0 = name present
        buf.writeByte(0x00);  // nullBits[2]: none
        buf.writeByte(0x00);  // nullBits[3]: none

        // Fixed fields
        buf.writeByte(0);     // unknown = false
        buf.writeByte(isEmpty ? 0 : 1); // drawType: 0=Empty, 1=Cube
        buf.writeByte(0);     // material = Empty
        buf.writeByte(isEmpty ? 0 : 2); // opacity: 0=Solid for most, 2 for air-like? Actually 0=Solid
        buf.writeIntLE(0);    // hitbox
        buf.writeIntLE(0);    // interactionHitbox
        buf.writeFloatLE(1.0f); // modelScale
        buf.writeByte(0);     // looping = false
        buf.writeIntLE(0);    // maxSupportDistance
        buf.writeByte(0);     // blockSupportsRequiredFor = Any
        buf.writeByte(0);     // requiresAlphaBlending = false
        buf.writeByte(0);     // cubeShadingMode = Standard
        buf.writeByte(0);     // randomRotation = None
        buf.writeByte(0);     // variantRotation = None
        buf.writeByte(0);     // rotationYawPlacementOffset = None
        buf.writeIntLE(0);    // blockSoundSetIndex
        buf.writeIntLE(0);    // ambientSoundEventIndex

        // Nullable fixed-size fields (zeroed when null)
        buf.writeZero(3);     // particleColor (null)
        buf.writeZero(4);     // light (null)
        buf.writeZero(24);    // tint (null)
        buf.writeZero(24);    // biomeTint (null)

        buf.writeIntLE(0);    // group

        buf.writeZero(42);    // movementSettings (null)
        buf.writeZero(2);     // flags (null)
        buf.writeZero(17);    // placementSettings (null)

        buf.writeByte(0);     // ignoreSupportWhenPlaced = false
        buf.writeIntLE(0);    // transitionToTag

        // 25 variable field offset slots (int32LE each)
        // Order: item, name, shaderEffect, model, modelTexture, modelAnimation,
        //        support, supporting, cubeTextures, cubeSideMaskTexture,
        //        conditionalSounds, particles, blockParticleSetId, blockBreakingDecalId,
        //        transitionTexture, transitionToGroups, interactionHint, gathering,
        //        display, rail, interactions, states, tagIndexes, bench, connectedBlockRuleSet
        int itemOffsetSlot = buf.writerIndex();
        buf.writeIntLE(-1);   // item = null
        int nameOffsetSlot = buf.writerIndex();
        buf.writeIntLE(0);    // name = placeholder (will backpatch)
        // Remaining 23 offsets: all null (-1)
        for (int j = 0; j < 23; j++) {
            buf.writeIntLE(-1);
        }
        int varBlockStart = buf.writerIndex();

        // Backpatch name offset: relative to varBlockStart
        buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
        // Write name as VarInt-length-prefixed UTF-8 string
        byte[] nameBytes = name.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        writeVarInt(buf, nameBytes.length);
        buf.writeBytes(nameBytes);
    }

    /** Write unsigned VarInt (7-bit encoding) directly to ByteBuf. */
    private static void writeVarInt(io.netty.buffer.ByteBuf buf, int value) {
        while ((value & ~0x7F) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    public int getBlockCount() { return blockCount; }
}
