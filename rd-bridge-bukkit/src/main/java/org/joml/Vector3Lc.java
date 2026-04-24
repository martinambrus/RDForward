package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vector3Lc {
    long x();
    long y();
    long z();
    java.nio.LongBuffer get(java.nio.LongBuffer arg0);
    java.nio.LongBuffer get(int arg0, java.nio.LongBuffer arg1);
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    org.joml.Vector3Lc getToAddress(long arg0);
    org.joml.Vector3L sub(org.joml.Vector3Lc arg0, org.joml.Vector3L arg1);
    org.joml.Vector3L sub(long arg0, long arg1, long arg2, org.joml.Vector3L arg3);
    org.joml.Vector3L add(org.joml.Vector3Lc arg0, org.joml.Vector3L arg1);
    org.joml.Vector3L add(long arg0, long arg1, long arg2, org.joml.Vector3L arg3);
    org.joml.Vector3L mul(long arg0, org.joml.Vector3L arg1);
    org.joml.Vector3L mul(org.joml.Vector3Lc arg0, org.joml.Vector3L arg1);
    org.joml.Vector3L mul(long arg0, long arg1, long arg2, org.joml.Vector3L arg3);
    org.joml.Vector3L div(float arg0, org.joml.Vector3L arg1);
    org.joml.Vector3L div(long arg0, org.joml.Vector3L arg1);
    long lengthSquared();
    double length();
    double distance(org.joml.Vector3Lc arg0);
    double distance(long arg0, long arg1, long arg2);
    long gridDistance(org.joml.Vector3Lc arg0);
    long gridDistance(long arg0, long arg1, long arg2);
    long distanceSquared(org.joml.Vector3Lc arg0);
    long distanceSquared(long arg0, long arg1, long arg2);
    org.joml.Vector3L negate(org.joml.Vector3L arg0);
    org.joml.Vector3L min(org.joml.Vector3Lc arg0, org.joml.Vector3L arg1);
    org.joml.Vector3L max(org.joml.Vector3Lc arg0, org.joml.Vector3L arg1);
    long get(int arg0) throws java.lang.IllegalArgumentException;
    int maxComponent();
    int minComponent();
    org.joml.Vector3L absolute(org.joml.Vector3L arg0);
    boolean equals(long arg0, long arg1, long arg2);
}
