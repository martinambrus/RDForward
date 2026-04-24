package io.papermc.paper.math;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlockPosition extends io.papermc.paper.math.Position {
    default double x() {
        return 0.0;
    }
    default double y() {
        return 0.0;
    }
    default double z() {
        return 0.0;
    }
    default boolean isBlock() {
        return false;
    }
    default boolean isFine() {
        return false;
    }
    default io.papermc.paper.math.BlockPosition toBlock() {
        return null;
    }
    default io.papermc.paper.math.BlockPosition offset(int arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.BlockPosition.offset(III)Lio/papermc/paper/math/BlockPosition;");
        return this;
    }
    default io.papermc.paper.math.FinePosition offset(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.BlockPosition.offset(DDD)Lio/papermc/paper/math/FinePosition;");
        return null;
    }
    default io.papermc.paper.math.BlockPosition offset(org.bukkit.block.BlockFace arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.BlockPosition.offset(Lorg/bukkit/block/BlockFace;)Lio/papermc/paper/math/BlockPosition;");
        return this;
    }
    default io.papermc.paper.math.BlockPosition offset(org.bukkit.block.BlockFace arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.BlockPosition.offset(Lorg/bukkit/block/BlockFace;I)Lio/papermc/paper/math/BlockPosition;");
        return this;
    }
    default io.papermc.paper.math.BlockPosition offset(org.bukkit.Axis arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.BlockPosition.offset(Lorg/bukkit/Axis;I)Lio/papermc/paper/math/BlockPosition;");
        return this;
    }
}
