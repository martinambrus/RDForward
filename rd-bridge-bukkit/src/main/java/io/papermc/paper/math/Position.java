package io.papermc.paper.math;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Position {
    public static final io.papermc.paper.math.FinePosition FINE_ZERO = null;
    public static final io.papermc.paper.math.BlockPosition BLOCK_ZERO = null;
    int blockX();
    int blockY();
    int blockZ();
    double x();
    double y();
    double z();
    boolean isBlock();
    boolean isFine();
    default boolean isFinite() {
        return false;
    }
    io.papermc.paper.math.Position offset(int arg0, int arg1, int arg2);
    io.papermc.paper.math.FinePosition offset(double arg0, double arg1, double arg2);
    default io.papermc.paper.math.FinePosition toCenter() {
        return null;
    }
    io.papermc.paper.math.BlockPosition toBlock();
    default org.bukkit.util.Vector toVector() {
        return null;
    }
    default org.bukkit.Location toLocation(org.bukkit.World arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.math.Position.toLocation(Lorg/bukkit/World;)Lorg/bukkit/Location;");
        return null;
    }
    static io.papermc.paper.math.BlockPosition block(int arg0, int arg1, int arg2) {
        return null;
    }
    static io.papermc.paper.math.BlockPosition block(org.bukkit.Location arg0) {
        return null;
    }
    static io.papermc.paper.math.FinePosition fine(double arg0, double arg1, double arg2) {
        return null;
    }
    static io.papermc.paper.math.FinePosition fine(org.bukkit.Location arg0) {
        return null;
    }
}
