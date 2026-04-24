package io.papermc.paper.math;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Rotations {
    public static final io.papermc.paper.math.Rotations ZERO = null;
    static io.papermc.paper.math.Rotations ofDegrees(double arg0, double arg1, double arg2) {
        return null;
    }
    double x();
    double y();
    double z();
    io.papermc.paper.math.Rotations withX(double arg0);
    io.papermc.paper.math.Rotations withY(double arg0);
    io.papermc.paper.math.Rotations withZ(double arg0);
    io.papermc.paper.math.Rotations add(double arg0, double arg1, double arg2);
    default io.papermc.paper.math.Rotations subtract(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.Rotations.subtract(DDD)Lio/papermc/paper/math/Rotations;");
        return this;
    }
}
