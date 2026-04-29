---
name: Material.java is Paper-mirrored bulk catalog
description: Material.java now mirrors Paper's full Material enum (1848 entries) — don't trim or regenerate without preserving protected ids
type: project
originSessionId: 5502dcd6-95a6-49a3-a950-70fdccebac2e
---
`org.bukkit.Material` in `rd-bridge-bukkit` mirrors PaperMC/Paper's `Material.java` in full: ~1848 entries covering every modern name plus every pre-Flattening alias (`BED_BLOCK`, `BURNING_FURNACE`, `RAW_FISH`, `WOOD_AXE`, etc.) with pre-1.13 vanilla block/item ids. Modern-only constants carry id `-1`. Generation source: `https://raw.githubusercontent.com/PaperMC/Paper/main/paper-api/src/main/java/org/bukkit/Material.java` (latest fetch 2026-04-29).

**Why:** Plugins reach for legacy field names (Essentials's `Util.<clinit>` reads `Material.BED_BLOCK`, `Material.RAW_FISH`, etc.) — bulk catalog prevents NoSuchFieldError without per-plugin patches.

**How to apply:**
- 19 protected modern names hold their pre-Flat block id and drive `MaterialMapper.toApi/fromApi` + `Block.getTypeId()`: AIR, STONE, GRASS_BLOCK, DIRT, COBBLESTONE, OAK_PLANKS, OAK_SAPLING, BEDROCK, WATER, LAVA, SAND, GRAVEL, GOLD_ORE, IRON_ORE, COAL_ORE, OAK_LOG, OAK_LEAVES, GLASS, TNT. Don't change their ids.
- 3 legacy aliases neutralized to id `-1` so `getMaterial(int)` resolves to the modern owner: GRASS (would shadow GRASS_BLOCK at id 2), LOG (would shadow OAK_LOG at id 17), LEAVES (would shadow OAK_LEAVES at id 18). MaterialMapper.toApi has explicit aliases for these (GRASS→GRASS, LOG→WOOD, LEAVES→LEAVES) plus WOOD→PLANKS, SAPLING→SAPLING.
- BED is the ITEM (id 355). BED_BLOCK is the placed block (id 26). Pre-Flat semantics — don't conflate.
- File header keeps `@rdforward:preserve` because helper methods (`getMaterial`, `matchMaterial`, `getKey`, `hasGravity`, etc.) and the protected-id overrides are hand-tuned. Don't blindly regenerate.
- To add new entries from a Paper update: re-run `/tmp/gen_material.py` against fresh Paper source, diff, append new entries.
