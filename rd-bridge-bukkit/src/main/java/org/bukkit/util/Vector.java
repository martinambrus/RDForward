// @rdforward:preserve - hand-tuned facade, do not regenerate
package org.bukkit.util;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class Vector implements java.lang.Cloneable, org.bukkit.configuration.serialization.ConfigurationSerializable {
    protected double x;
    protected double y;
    protected double z;
    public Vector() {}
    public Vector(int x, int y, int z) { this.x = x; this.y = y; this.z = z; }
    public Vector(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    public Vector(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
    public org.bukkit.util.Vector add(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.add(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector subtract(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.subtract(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector multiply(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.multiply(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector divide(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.divide(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector copy(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.copy(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public double length() {
        return 0.0;
    }
    public double lengthSquared() {
        return 0.0;
    }
    public double distance(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.distance(Lorg/bukkit/util/Vector;)D");
        return 0.0;
    }
    public double distanceSquared(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.distanceSquared(Lorg/bukkit/util/Vector;)D");
        return 0.0;
    }
    public float angle(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.angle(Lorg/bukkit/util/Vector;)F");
        return 0.0f;
    }
    public org.bukkit.util.Vector midpoint(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.midpoint(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector getMidpoint(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.getMidpoint(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector multiply(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.multiply(I)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector multiply(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.multiply(D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector multiply(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.multiply(F)Lorg/bukkit/util/Vector;");
        return this;
    }
    public double dot(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.dot(Lorg/bukkit/util/Vector;)D");
        return 0.0;
    }
    public org.bukkit.util.Vector crossProduct(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.crossProduct(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector getCrossProduct(org.bukkit.util.Vector arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.getCrossProduct(Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector normalize() {
        return null;
    }
    public org.bukkit.util.Vector zero() {
        return null;
    }
    public boolean isZero() {
        return false;
    }
    public boolean isInAABB(org.bukkit.util.Vector arg0, org.bukkit.util.Vector arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.isInAABB(Lorg/bukkit/util/Vector;Lorg/bukkit/util/Vector;)Z");
        return false;
    }
    public boolean isInSphere(org.bukkit.util.Vector arg0, double arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.isInSphere(Lorg/bukkit/util/Vector;D)Z");
        return false;
    }
    public boolean isNormalized() {
        return false;
    }
    public org.bukkit.util.Vector rotateAroundX(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.rotateAroundX(D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector rotateAroundY(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.rotateAroundY(D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector rotateAroundZ(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.rotateAroundZ(D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector rotateAroundAxis(org.bukkit.util.Vector arg0, double arg1) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.rotateAroundAxis(Lorg/bukkit/util/Vector;D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector rotateAroundNonUnitAxis(org.bukkit.util.Vector arg0, double arg1) throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.rotateAroundNonUnitAxis(Lorg/bukkit/util/Vector;D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public double getX() {
        return x;
    }
    public int getBlockX() {
        return (int) Math.floor(x);
    }
    public double getY() {
        return y;
    }
    public int getBlockY() {
        return (int) Math.floor(y);
    }
    public double getZ() {
        return z;
    }
    public int getBlockZ() {
        return (int) Math.floor(z);
    }
    public org.bukkit.util.Vector setX(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setX(I)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setX(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setX(D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setX(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setX(F)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setY(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setY(I)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setY(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setY(D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setY(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setY(F)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setZ(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setZ(I)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setZ(double arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setZ(D)Lorg/bukkit/util/Vector;");
        return this;
    }
    public org.bukkit.util.Vector setZ(float arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.setZ(F)Lorg/bukkit/util/Vector;");
        return this;
    }
    public boolean equals(java.lang.Object other) {
        if (!(other instanceof org.bukkit.util.Vector)) return false;
        org.bukkit.util.Vector v = (org.bukkit.util.Vector) other;
        return Math.abs(x - v.x) < 1.0E-6
                && Math.abs(y - v.y) < 1.0E-6
                && Math.abs(z - v.z) < 1.0E-6;
    }
    public int hashCode() {
        long lx = Double.doubleToLongBits(x);
        long ly = Double.doubleToLongBits(y);
        long lz = Double.doubleToLongBits(z);
        int h = (int) (lx ^ (lx >>> 32));
        h = 31 * h + (int) (ly ^ (ly >>> 32));
        h = 31 * h + (int) (lz ^ (lz >>> 32));
        return h;
    }
    public org.bukkit.util.Vector clone() {
        return new org.bukkit.util.Vector(x, y, z);
    }
    public java.lang.String toString() {
        return x + "," + y + "," + z;
    }
    public org.bukkit.Location toLocation(org.bukkit.World arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.toLocation(Lorg/bukkit/World;)Lorg/bukkit/Location;");
        return null;
    }
    public org.bukkit.Location toLocation(org.bukkit.World arg0, float arg1, float arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.toLocation(Lorg/bukkit/World;FF)Lorg/bukkit/Location;");
        return null;
    }
    public org.bukkit.util.BlockVector toBlockVector() {
        return null;
    }
    public org.joml.Vector3f toVector3f() {
        return null;
    }
    public org.joml.Vector3d toVector3d() {
        return null;
    }
    public org.joml.Vector3i toVector3i(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.toVector3i(I)Lorg/joml/Vector3i;");
        return null;
    }
    public org.joml.Vector3i toVector3i() {
        return null;
    }
    public void checkFinite() throws java.lang.IllegalArgumentException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.util.Vector.checkFinite()V");
    }
    public static double getEpsilon() {
        return 0.0;
    }
    public static org.bukkit.util.Vector getMinimum(org.bukkit.util.Vector arg0, org.bukkit.util.Vector arg1) {
        return null;
    }
    public static org.bukkit.util.Vector getMaximum(org.bukkit.util.Vector arg0, org.bukkit.util.Vector arg1) {
        return null;
    }
    public static org.bukkit.util.Vector getRandom() {
        return null;
    }
    public static org.bukkit.util.Vector fromJOML(org.joml.Vector3f arg0) {
        return null;
    }
    public static org.bukkit.util.Vector fromJOML(org.joml.Vector3d arg0) {
        return null;
    }
    public static org.bukkit.util.Vector fromJOML(org.joml.Vector3i arg0) {
        return null;
    }
    public static org.bukkit.util.Vector fromJOML(org.joml.Vector3fc arg0) {
        return null;
    }
    public static org.bukkit.util.Vector fromJOML(org.joml.Vector3dc arg0) {
        return null;
    }
    public static org.bukkit.util.Vector fromJOML(org.joml.Vector3ic arg0) {
        return null;
    }
    public java.util.Map serialize() {
        return java.util.Collections.emptyMap();
    }
    public static org.bukkit.util.Vector deserialize(java.util.Map arg0) {
        return null;
    }
}
