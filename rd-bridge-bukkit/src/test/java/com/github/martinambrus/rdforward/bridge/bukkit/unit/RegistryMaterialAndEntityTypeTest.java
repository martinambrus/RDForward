package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.EntityType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class RegistryMaterialAndEntityTypeTest {

    @Test
    void materialRegistryIsNotNull() {
        assertNotNull(Registry.MATERIAL, "Registry.MATERIAL must not be null for WorldEdit 7.x");
    }

    @Test
    void materialRegistryIteratesAllMaterials() {
        List<Material> collected = new ArrayList<>();
        Registry.MATERIAL.forEach(k -> collected.add((Material) k));
        assertEquals(Material.values().length, collected.size());
    }

    @Test
    void materialRegistrySizeMatchesEnum() {
        assertEquals(Material.values().length, Registry.MATERIAL.size());
    }

    @Test
    void materialRegistryGetByKey() {
        Material stone = Material.STONE;
        assertNotNull(Registry.MATERIAL.get(stone.getKey()));
        assertSame(stone, Registry.MATERIAL.get(stone.getKey()));
    }

    @Test
    void materialImplementsKeyed() {
        assertNotNull(Material.STONE.getKey(),
                "Material must implement Keyed with non-null getKey()");
    }

    @Test
    void entityTypeRegistryIsNotNull() {
        assertNotNull(Registry.ENTITY_TYPE, "Registry.ENTITY_TYPE must not be null");
    }

    @Test
    void entityTypeRegistryIteratesAll() {
        List<EntityType> collected = new ArrayList<>();
        Registry.ENTITY_TYPE.forEach(k -> collected.add((EntityType) k));
        assertEquals(EntityType.values().length, collected.size());
    }

    @Test
    void entityTypeRegistrySizeMatchesEnum() {
        assertEquals(EntityType.values().length, Registry.ENTITY_TYPE.size());
    }
}
