package org.bukkit.util;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class BoundingBox implements java.lang.Cloneable, org.bukkit.configuration.serialization.ConfigurationSerializable {
    public BoundingBox() {}
    public BoundingBox(double arg0, double arg1, double arg2, double arg3, double arg4, double arg5) {}
    public static org.bukkit.util.BoundingBox of(org.bukkit.util.Vector arg0, org.bukkit.util.Vector arg1) {
        return null;
    }
    public static org.bukkit.util.BoundingBox of(org.bukkit.Location arg0, org.bukkit.Location arg1) {
        return null;
    }
    public static org.bukkit.util.BoundingBox of(org.bukkit.block.Block arg0, org.bukkit.block.Block arg1) {
        return null;
    }
    public static org.bukkit.util.BoundingBox of(org.bukkit.block.Block arg0) {
        return null;
    }
    public static org.bukkit.util.BoundingBox of(org.bukkit.util.Vector arg0, double arg1, double arg2, double arg3) {
        return null;
    }
    public static org.bukkit.util.BoundingBox of(org.bukkit.Location arg0, double arg1, double arg2, double arg3) {
        return null;
    }
    public org.bukkit.util.BoundingBox resize(double arg0, double arg1, double arg2, double arg3, double arg4, double arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.resize(DDDDDD)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public double getMinX() {
        return 0.0;
    }
    public double getMinY() {
        return 0.0;
    }
    public double getMinZ() {
        return 0.0;
    }
    public org.bukkit.util.Vector getMin() {
        return null;
    }
    public double getMaxX() {
        return 0.0;
    }
    public double getMaxY() {
        return 0.0;
    }
    public double getMaxZ() {
        return 0.0;
    }
    public org.bukkit.util.Vector getMax() {
        return null;
    }
    public double getWidthX() {
        return 0.0;
    }
    public double getWidthZ() {
        return 0.0;
    }
    public double getHeight() {
        return 0.0;
    }
    public double getVolume() {
        return 0.0;
    }
    public double getCenterX() {
        return 0.0;
    }
    public double getCenterY() {
        return 0.0;
    }
    public double getCenterZ() {
        return 0.0;
    }
    public org.bukkit.util.Vector getCenter() {
        return null;
    }
    public org.bukkit.util.BoundingBox copy(org.bukkit.util.BoundingBox arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.copy(Lorg/bukkit/util/BoundingBox;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expand(double arg0, double arg1, double arg2, double arg3, double arg4, double arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expand(DDDDDD)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expand(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expand(DDD)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expand(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expand(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expand(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expand(D)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expand(double arg0, double arg1, double arg2, double arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expand(DDDD)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expand(org.bukkit.util.Vector arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expand(Lorg/bukkit/util/Vector;D)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expand(org.bukkit.block.BlockFace arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expand(Lorg/bukkit/block/BlockFace;D)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expandDirectional(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expandDirectional(DDD)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox expandDirectional(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.expandDirectional(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox union(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.union(DDD)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox union(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.union(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox union(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.union(Lorg/bukkit/Location;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox union(org.bukkit.util.BoundingBox arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.union(Lorg/bukkit/util/BoundingBox;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox intersection(org.bukkit.util.BoundingBox arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.intersection(Lorg/bukkit/util/BoundingBox;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox shift(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.shift(DDD)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox shift(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.shift(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public org.bukkit.util.BoundingBox shift(org.bukkit.Location arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.shift(Lorg/bukkit/Location;)Lorg/bukkit/util/BoundingBox;");
        return this;
    }
    public boolean overlaps(org.bukkit.util.BoundingBox arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.overlaps(Lorg/bukkit/util/BoundingBox;)Z");
        return false;
    }
    public boolean overlaps(org.bukkit.util.Vector arg0, org.bukkit.util.Vector arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.overlaps(Lorg/bukkit/util/Vector;Lorg/bukkit/util/Vector;)Z");
        return false;
    }
    public boolean contains(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.contains(DDD)Z");
        return false;
    }
    public boolean contains(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.contains(Lorg/bukkit/util/Vector;)Z");
        return false;
    }
    public boolean contains(org.bukkit.util.BoundingBox arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.contains(Lorg/bukkit/util/BoundingBox;)Z");
        return false;
    }
    public boolean contains(org.bukkit.util.Vector arg0, org.bukkit.util.Vector arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.contains(Lorg/bukkit/util/Vector;Lorg/bukkit/util/Vector;)Z");
        return false;
    }
    public org.bukkit.util.RayTraceResult rayTrace(org.bukkit.util.Vector arg0, org.bukkit.util.Vector arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.rayTrace(Lorg/bukkit/util/Vector;Lorg/bukkit/util/Vector;D)Lorg/bukkit/util/RayTraceResult;");
        return null;
    }
    public int hashCode() {
        return 0;
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.BoundingBox.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public java.lang.String toString() {
        return null;
    }
    public org.bukkit.util.BoundingBox clone() {
        return null;
    }
    public java.util.Map serialize() {
        return java.util.Collections.emptyMap();
    }
    public static org.bukkit.util.BoundingBox deserialize(java.util.Map arg0) {
        return null;
    }
}
