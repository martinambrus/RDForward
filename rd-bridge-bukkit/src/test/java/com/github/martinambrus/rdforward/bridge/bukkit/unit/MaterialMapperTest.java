package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import com.github.martinambrus.rdforward.api.world.BlockType;
import com.github.martinambrus.rdforward.api.world.BlockTypes;
import com.github.martinambrus.rdforward.bridge.bukkit.MaterialMapper;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MaterialMapperTest {

    @Test
    void nullMaterialMapsToAir() {
        assertSame(BlockTypes.AIR, MaterialMapper.toApi(null));
    }

    @Test
    void nullBlockTypeMapsToAirMaterial() {
        assertEquals(Material.AIR, MaterialMapper.fromApi(null));
    }

    @Test
    void toApiCoversEveryEnumConstant() {
        for (Material m : Material.values()) {
            BlockType b = MaterialMapper.toApi(m);
            org.junit.jupiter.api.Assertions.assertNotNull(b,
                    "toApi must return non-null for every Material stub entry: " + m);
        }
    }

    @Test
    void roundTripsStone() {
        assertSame(BlockTypes.STONE, MaterialMapper.toApi(Material.STONE));
        assertEquals(Material.STONE, MaterialMapper.fromApi(BlockTypes.STONE));
    }

    @Test
    void roundTripsCobble() {
        assertSame(BlockTypes.COBBLE, MaterialMapper.toApi(Material.COBBLESTONE));
        assertEquals(Material.COBBLESTONE, MaterialMapper.fromApi(BlockTypes.COBBLE));
    }

    @Test
    void roundTripsWater() {
        assertSame(BlockTypes.WATER, MaterialMapper.toApi(Material.WATER));
        assertEquals(Material.WATER, MaterialMapper.fromApi(BlockTypes.WATER));
    }

    @Test
    void roundTripsTnt() {
        assertSame(BlockTypes.TNT, MaterialMapper.toApi(Material.TNT));
        assertEquals(Material.TNT, MaterialMapper.fromApi(BlockTypes.TNT));
    }

    @Test
    void unmappedBlockTypeIdFallsBackToAir() {
        BlockType obscure = new BlockType() {
            @Override public int getId() { return 9999; }
            @Override public String getName() { return "unknown"; }
        };
        assertEquals(Material.AIR, MaterialMapper.fromApi(obscure));
    }
}
