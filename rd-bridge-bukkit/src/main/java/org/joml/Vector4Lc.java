package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vector4Lc {
    long x();
    long y();
    long z();
    long w();
    java.nio.LongBuffer get(java.nio.LongBuffer arg0);
    java.nio.LongBuffer get(int arg0, java.nio.LongBuffer arg1);
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    org.joml.Vector4Lc getToAddress(long arg0);
    org.joml.Vector4L sub(org.joml.Vector4Lc arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L sub(org.joml.Vector4ic arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L sub(long arg0, long arg1, long arg2, long arg3, org.joml.Vector4L arg4);
    org.joml.Vector4L add(org.joml.Vector4Lc arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L add(org.joml.Vector4ic arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L add(long arg0, long arg1, long arg2, long arg3, org.joml.Vector4L arg4);
    org.joml.Vector4L mul(org.joml.Vector4Lc arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L mul(org.joml.Vector4ic arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L div(org.joml.Vector4Lc arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L div(org.joml.Vector4ic arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L mul(long arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L div(float arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L div(long arg0, org.joml.Vector4L arg1);
    long lengthSquared();
    double length();
    double distance(org.joml.Vector4Lc arg0);
    double distance(org.joml.Vector4ic arg0);
    double distance(long arg0, long arg1, long arg2, long arg3);
    long gridDistance(org.joml.Vector4Lc arg0);
    long gridDistance(org.joml.Vector4ic arg0);
    long gridDistance(long arg0, long arg1, long arg2, long arg3);
    long distanceSquared(org.joml.Vector4Lc arg0);
    long distanceSquared(org.joml.Vector4ic arg0);
    long distanceSquared(long arg0, long arg1, long arg2, long arg3);
    long dot(org.joml.Vector4Lc arg0);
    long dot(org.joml.Vector4ic arg0);
    org.joml.Vector4L negate(org.joml.Vector4L arg0);
    org.joml.Vector4L min(org.joml.Vector4Lc arg0, org.joml.Vector4L arg1);
    org.joml.Vector4L max(org.joml.Vector4Lc arg0, org.joml.Vector4L arg1);
    long get(int arg0) throws java.lang.IllegalArgumentException;
    int maxComponent();
    int minComponent();
    org.joml.Vector4L absolute(org.joml.Vector4L arg0);
    boolean equals(long arg0, long arg1, long arg2, long arg3);
}
