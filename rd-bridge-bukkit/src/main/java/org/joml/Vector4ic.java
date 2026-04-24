package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vector4ic {
    int x();
    int y();
    int z();
    int w();
    java.nio.IntBuffer get(java.nio.IntBuffer arg0);
    java.nio.IntBuffer get(int arg0, java.nio.IntBuffer arg1);
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    org.joml.Vector4ic getToAddress(long arg0);
    org.joml.Vector4i sub(org.joml.Vector4ic arg0, org.joml.Vector4i arg1);
    org.joml.Vector4i sub(int arg0, int arg1, int arg2, int arg3, org.joml.Vector4i arg4);
    org.joml.Vector4i add(org.joml.Vector4ic arg0, org.joml.Vector4i arg1);
    org.joml.Vector4i add(int arg0, int arg1, int arg2, int arg3, org.joml.Vector4i arg4);
    org.joml.Vector4i mul(org.joml.Vector4ic arg0, org.joml.Vector4i arg1);
    org.joml.Vector4i div(org.joml.Vector4ic arg0, org.joml.Vector4i arg1);
    org.joml.Vector4i mul(int arg0, org.joml.Vector4i arg1);
    org.joml.Vector4i div(float arg0, org.joml.Vector4i arg1);
    org.joml.Vector4i div(int arg0, org.joml.Vector4i arg1);
    long lengthSquared();
    double length();
    double distance(org.joml.Vector4ic arg0);
    double distance(int arg0, int arg1, int arg2, int arg3);
    long gridDistance(org.joml.Vector4ic arg0);
    long gridDistance(int arg0, int arg1, int arg2, int arg3);
    long distanceSquared(org.joml.Vector4ic arg0);
    long distanceSquared(int arg0, int arg1, int arg2, int arg3);
    long dot(org.joml.Vector4ic arg0);
    org.joml.Vector4i negate(org.joml.Vector4i arg0);
    org.joml.Vector4i min(org.joml.Vector4ic arg0, org.joml.Vector4i arg1);
    org.joml.Vector4i max(org.joml.Vector4ic arg0, org.joml.Vector4i arg1);
    int get(int arg0) throws java.lang.IllegalArgumentException;
    int maxComponent();
    int minComponent();
    org.joml.Vector4i absolute(org.joml.Vector4i arg0);
    boolean equals(int arg0, int arg1, int arg2, int arg3);
}
