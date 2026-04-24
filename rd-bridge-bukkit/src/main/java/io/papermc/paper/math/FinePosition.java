package io.papermc.paper.math;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FinePosition extends io.papermc.paper.math.Position {
    default int blockX() {
        return 0;
    }
    default int blockY() {
        return 0;
    }
    default int blockZ() {
        return 0;
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
    default io.papermc.paper.math.FinePosition offset(int arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.FinePosition.offset(III)Lio/papermc/paper/math/FinePosition;");
        return this;
    }
    default io.papermc.paper.math.FinePosition offset(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.FinePosition.offset(DDD)Lio/papermc/paper/math/FinePosition;");
        return this;
    }
}
