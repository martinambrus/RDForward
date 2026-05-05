package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import net.minecraft.server.v1_6_R3.*;
import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that NMS v1_6_R3 stubs load and have correct class hierarchy.
 * These stubs exist purely so ClearLag v2.6.0 can load without
 * {@code NoClassDefFoundError} — the actual methods are no-ops.
 */
class NmsV163StubsTest {

    @Test
    void entityHierarchyIsCorrect() {
        assertEquals(Entity.class, EntityLiving.class.getSuperclass());
        assertEquals(EntityLiving.class, EntityInsentient.class.getSuperclass());
        assertEquals(EntityInsentient.class, EntitySkeleton.class.getSuperclass());
        assertEquals(EntityInsentient.class, EntityZombie.class.getSuperclass());
        assertEquals(EntityInsentient.class, EntityCreeper.class.getSuperclass());
    }

    @Test
    void worldHierarchyIsCorrect() {
        assertEquals(World.class, WorldServer.class.getSuperclass());
    }

    @Test
    void itemHierarchyIsCorrect() {
        assertEquals(Item.class, ItemBow.class.getSuperclass());
    }

    @Test
    void entityConstructorsAcceptWorld() {
        World w = new World();
        assertNotNull(new EntitySkeleton(w));
        assertNotNull(new EntityZombie(w));
        assertNotNull(new EntityCreeper(w));
    }

    @Test
    void entityMethodsDoNotThrow() {
        Entity e = new Entity();
        assertDoesNotThrow(() -> e.setPositionRotation(1, 2, 3, 90f, 45f));

        EntityInsentient ei = new EntityZombie(new World());
        assertNotNull(ei.getAttributeInstance(GenericAttributes.b));
        assertDoesNotThrow(() -> ei.setEquipment(0, new ItemStack(1, 1, 0)));
    }

    @Test
    void entityInsentientAzCanBeCalledFromSubclass() {
        // az(), bw(), bT() are protected — verify via subclass call
        class TestZombie extends EntityZombie {
            TestZombie() { super(new World()); }
            void callAz() { az(); }
        }
        assertDoesNotThrow(() -> new TestZombie().callAz());
    }

    @Test
    void skeletonProtectedMethodsCanBeCalledFromSubclass() {
        class TestSkeleton extends EntitySkeleton {
            TestSkeleton() { super(new World()); }
            void callBw() { bw(); }
            void callBT() { bT(); }
        }
        TestSkeleton s = new TestSkeleton();
        assertDoesNotThrow(s::callBw);
        assertDoesNotThrow(s::callBT);
    }

    @Test
    void genericAttributesProvidesFollowRange() {
        IAttribute attr = GenericAttributes.b;
        assertNotNull(attr);
        assertEquals("generic.followRange", attr.getName());
    }

    @Test
    void attributeInstanceSetValuedoesNotThrow() {
        AttributeInstance inst = new AttributeInstance();
        assertDoesNotThrow(() -> inst.setValue(24.0));
    }

    @Test
    void itemStackConstructors() {
        assertDoesNotThrow(() -> new ItemStack(1, 1, 0));
        assertDoesNotThrow(() -> new ItemStack(Item.BOW));
    }

    @Test
    void itemBowField() {
        assertNotNull(Item.BOW);
        assertTrue(Item.BOW instanceof ItemBow);
    }

    @Test
    void entityTypesRegistrationDoesNotThrow() {
        assertDoesNotThrow(() -> EntityTypes.a(EntityZombie.class, "Zombie", 54));
    }

    @Test
    void worldAddEntityDoesNotThrow() {
        World w = new World();
        assertDoesNotThrow(() -> w.addEntity(new Entity()));
    }

    @Test
    void craftEntityGetHandleDoesNotThrow() {
        CraftEntity ce = new CraftEntity();
        assertNotNull(ce.getHandle());
    }

    @Test
    void craftWorldGetHandleDoesNotThrow() {
        CraftWorld cw = new CraftWorld();
        assertNotNull(cw.getHandle());
        assertTrue(cw.getHandle() instanceof WorldServer);
    }
}
