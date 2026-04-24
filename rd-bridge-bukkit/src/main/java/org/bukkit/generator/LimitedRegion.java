package org.bukkit.generator;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface LimitedRegion extends org.bukkit.RegionAccessor {
    int getBuffer();
    boolean isInRegion(org.bukkit.Location arg0);
    boolean isInRegion(int arg0, int arg1, int arg2);
    java.util.List getTileEntities();
    default void setBlockData(org.bukkit.util.Vector arg0, org.bukkit.block.data.BlockData arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.LimitedRegion.setBlockData(Lorg/bukkit/util/Vector;Lorg/bukkit/block/data/BlockData;)V");
    }
    void setBlockState(int arg0, int arg1, int arg2, org.bukkit.block.BlockState arg3);
    default void setBlockState(org.bukkit.util.Vector arg0, org.bukkit.block.BlockState arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.LimitedRegion.setBlockState(Lorg/bukkit/util/Vector;Lorg/bukkit/block/BlockState;)V");
    }
    default org.bukkit.block.BlockState getBlockState(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.LimitedRegion.getBlockState(Lorg/bukkit/util/Vector;)Lorg/bukkit/block/BlockState;");
        return null;
    }
    void scheduleBlockUpdate(int arg0, int arg1, int arg2);
    default void scheduleBlockUpdate(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.LimitedRegion.scheduleBlockUpdate(Lorg/bukkit/util/Vector;)V");
    }
    void scheduleFluidUpdate(int arg0, int arg1, int arg2);
    default void scheduleFluidUpdate(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.LimitedRegion.scheduleFluidUpdate(Lorg/bukkit/util/Vector;)V");
    }
    org.bukkit.World getWorld();
    default org.bukkit.block.data.BlockData getBlockData(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.generator.LimitedRegion.getBlockData(Lorg/bukkit/util/Vector;)Lorg/bukkit/block/data/BlockData;");
        return null;
    }
    int getCenterChunkX();
    default int getCenterBlockX() {
        return 0;
    }
    int getCenterChunkZ();
    default int getCenterBlockZ() {
        return 0;
    }
}
