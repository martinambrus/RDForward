package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vector2Lc {
    long x();
    long y();
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    java.nio.LongBuffer get(java.nio.LongBuffer arg0);
    java.nio.LongBuffer get(int arg0, java.nio.LongBuffer arg1);
    org.joml.Vector2Lc getToAddress(long arg0);
    org.joml.Vector2L sub(org.joml.Vector2Lc arg0, org.joml.Vector2L arg1);
    org.joml.Vector2L sub(long arg0, long arg1, org.joml.Vector2L arg2);
    long lengthSquared();
    double length();
    double distance(org.joml.Vector2Lc arg0);
    double distance(long arg0, long arg1);
    long distanceSquared(org.joml.Vector2Lc arg0);
    long distanceSquared(long arg0, long arg1);
    long gridDistance(org.joml.Vector2Lc arg0);
    long gridDistance(long arg0, long arg1);
    org.joml.Vector2L add(org.joml.Vector2Lc arg0, org.joml.Vector2L arg1);
    org.joml.Vector2L add(long arg0, long arg1, org.joml.Vector2L arg2);
    org.joml.Vector2L mul(long arg0, org.joml.Vector2L arg1);
    org.joml.Vector2L mul(org.joml.Vector2Lc arg0, org.joml.Vector2L arg1);
    org.joml.Vector2L mul(long arg0, long arg1, org.joml.Vector2L arg2);
    org.joml.Vector2L div(float arg0, org.joml.Vector2L arg1);
    org.joml.Vector2L div(long arg0, org.joml.Vector2L arg1);
    org.joml.Vector2L negate(org.joml.Vector2L arg0);
    org.joml.Vector2L min(org.joml.Vector2Lc arg0, org.joml.Vector2L arg1);
    org.joml.Vector2L max(org.joml.Vector2Lc arg0, org.joml.Vector2L arg1);
    long maxComponent();
    long minComponent();
    org.joml.Vector2L absolute(org.joml.Vector2L arg0);
    long get(int arg0) throws java.lang.IllegalArgumentException;
    boolean equals(long arg0, long arg1);
}
