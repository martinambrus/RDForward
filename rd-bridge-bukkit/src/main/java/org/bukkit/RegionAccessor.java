package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RegionAccessor extends org.bukkit.Keyed, io.papermc.paper.world.flag.FeatureFlagSetHolder {
    default org.bukkit.block.Biome getBiome(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.getBiome(Lorg/bukkit/Location;)Lorg/bukkit/block/Biome;");
        return null;
    }
    org.bukkit.block.Biome getBiome(int arg0, int arg1, int arg2);
    org.bukkit.block.Biome getComputedBiome(int arg0, int arg1, int arg2);
    default void setBiome(org.bukkit.Location arg0, org.bukkit.block.Biome arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.setBiome(Lorg/bukkit/Location;Lorg/bukkit/block/Biome;)V");
    }
    void setBiome(int arg0, int arg1, int arg2, org.bukkit.block.Biome arg3);
    default org.bukkit.block.BlockState getBlockState(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.getBlockState(Lorg/bukkit/Location;)Lorg/bukkit/block/BlockState;");
        return null;
    }
    org.bukkit.block.BlockState getBlockState(int arg0, int arg1, int arg2);
    io.papermc.paper.block.fluid.FluidData getFluidData(int arg0, int arg1, int arg2);
    default io.papermc.paper.block.fluid.FluidData getFluidData(io.papermc.paper.math.Position arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.getFluidData(Lio/papermc/paper/math/Position;)Lio/papermc/paper/block/fluid/FluidData;");
        return null;
    }
    default io.papermc.paper.block.fluid.FluidData getFluidData(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.getFluidData(Lorg/bukkit/Location;)Lio/papermc/paper/block/fluid/FluidData;");
        return null;
    }
    default org.bukkit.block.data.BlockData getBlockData(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.getBlockData(Lorg/bukkit/Location;)Lorg/bukkit/block/data/BlockData;");
        return null;
    }
    org.bukkit.block.data.BlockData getBlockData(int arg0, int arg1, int arg2);
    default org.bukkit.Material getType(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.getType(Lorg/bukkit/Location;)Lorg/bukkit/Material;");
        return null;
    }
    org.bukkit.Material getType(int arg0, int arg1, int arg2);
    default void setBlockData(org.bukkit.Location arg0, org.bukkit.block.data.BlockData arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.setBlockData(Lorg/bukkit/Location;Lorg/bukkit/block/data/BlockData;)V");
    }
    void setBlockData(int arg0, int arg1, int arg2, org.bukkit.block.data.BlockData arg3);
    default void setType(org.bukkit.Location arg0, org.bukkit.Material arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.setType(Lorg/bukkit/Location;Lorg/bukkit/Material;)V");
    }
    default void setType(int arg0, int arg1, int arg2, org.bukkit.Material arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.setType(IIILorg/bukkit/Material;)V");
    }
    boolean generateTree(org.bukkit.Location arg0, java.util.Random arg1, org.bukkit.TreeType arg2);
    boolean generateTree(org.bukkit.Location arg0, java.util.Random arg1, org.bukkit.TreeType arg2, java.util.function.Consumer arg3);
    boolean generateTree(org.bukkit.Location arg0, java.util.Random arg1, org.bukkit.TreeType arg2, java.util.function.Predicate arg3);
    default org.bukkit.entity.Entity spawnEntity(org.bukkit.Location arg0, org.bukkit.entity.EntityType arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.spawnEntity(Lorg/bukkit/Location;Lorg/bukkit/entity/EntityType;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    org.bukkit.entity.Entity spawnEntity(org.bukkit.Location arg0, org.bukkit.entity.EntityType arg1, boolean arg2);
    java.util.List getEntities();
    java.util.List getLivingEntities();
    java.util.Collection getEntitiesByClass(java.lang.Class arg0);
    java.util.Collection getEntitiesByClasses(java.lang.Class[] arg0);
    org.bukkit.entity.Entity createEntity(org.bukkit.Location arg0, java.lang.Class arg1);
    default org.bukkit.entity.Entity spawn(org.bukkit.Location arg0, java.lang.Class arg1) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.spawn(Lorg/bukkit/Location;Ljava/lang/Class;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    default org.bukkit.entity.Entity spawn(org.bukkit.Location arg0, java.lang.Class arg1, java.util.function.Consumer arg2) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.spawn(Lorg/bukkit/Location;Ljava/lang/Class;Ljava/util/function/Consumer;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    default org.bukkit.entity.Entity spawn(org.bukkit.Location arg0, java.lang.Class arg1, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason arg2) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.spawn(Lorg/bukkit/Location;Ljava/lang/Class;Lorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    default org.bukkit.entity.Entity spawn(org.bukkit.Location arg0, java.lang.Class arg1, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason arg2, java.util.function.Consumer arg3) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.spawn(Lorg/bukkit/Location;Ljava/lang/Class;Lorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;Ljava/util/function/Consumer;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    default org.bukkit.entity.Entity spawnEntity(org.bukkit.Location arg0, org.bukkit.entity.EntityType arg1, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.spawnEntity(Lorg/bukkit/Location;Lorg/bukkit/entity/EntityType;Lorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    default org.bukkit.entity.Entity spawnEntity(org.bukkit.Location arg0, org.bukkit.entity.EntityType arg1, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason arg2, java.util.function.Consumer arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.RegionAccessor.spawnEntity(Lorg/bukkit/Location;Lorg/bukkit/entity/EntityType;Lorg/bukkit/event/entity/CreatureSpawnEvent$SpawnReason;Ljava/util/function/Consumer;)Lorg/bukkit/entity/Entity;");
        return null;
    }
    org.bukkit.entity.Entity spawn(org.bukkit.Location arg0, java.lang.Class arg1, java.util.function.Consumer arg2, org.bukkit.event.entity.CreatureSpawnEvent$SpawnReason arg3) throws java.lang.IllegalArgumentException;
    org.bukkit.entity.Entity spawn(org.bukkit.Location arg0, java.lang.Class arg1, boolean arg2, java.util.function.Consumer arg3) throws java.lang.IllegalArgumentException;
    int getHighestBlockYAt(int arg0, int arg1);
    int getHighestBlockYAt(org.bukkit.Location arg0);
    int getHighestBlockYAt(int arg0, int arg1, org.bukkit.HeightMap arg2);
    int getHighestBlockYAt(org.bukkit.Location arg0, org.bukkit.HeightMap arg1);
    org.bukkit.entity.Entity addEntity(org.bukkit.entity.Entity arg0);
    io.papermc.paper.world.MoonPhase getMoonPhase();
    org.bukkit.NamespacedKey getKey();
    boolean lineOfSightExists(org.bukkit.Location arg0, org.bukkit.Location arg1);
    boolean hasCollisionsIn(org.bukkit.util.BoundingBox arg0);
}
