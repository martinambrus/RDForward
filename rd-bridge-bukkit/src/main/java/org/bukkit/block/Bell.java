package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Bell extends org.bukkit.block.TileState {
    boolean ring(org.bukkit.entity.Entity arg0, org.bukkit.block.BlockFace arg1);
    default boolean ring(org.bukkit.entity.Entity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.Bell.ring(Lorg/bukkit/entity/Entity;)Z");
        return false;
    }
    default boolean ring(org.bukkit.block.BlockFace arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.Bell.ring(Lorg/bukkit/block/BlockFace;)Z");
        return false;
    }
    default boolean ring() {
        return false;
    }
    boolean isShaking();
    int getShakingTicks();
    boolean isResonating();
    int getResonatingTicks();
}
