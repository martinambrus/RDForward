package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vector3ic {
    int x();
    int y();
    int z();
    java.nio.IntBuffer get(java.nio.IntBuffer arg0);
    java.nio.IntBuffer get(int arg0, java.nio.IntBuffer arg1);
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    org.joml.Vector3ic getToAddress(long arg0);
    org.joml.Vector3i sub(org.joml.Vector3ic arg0, org.joml.Vector3i arg1);
    org.joml.Vector3i sub(int arg0, int arg1, int arg2, org.joml.Vector3i arg3);
    org.joml.Vector3i add(org.joml.Vector3ic arg0, org.joml.Vector3i arg1);
    org.joml.Vector3i add(int arg0, int arg1, int arg2, org.joml.Vector3i arg3);
    org.joml.Vector3i mul(int arg0, org.joml.Vector3i arg1);
    org.joml.Vector3i mul(org.joml.Vector3ic arg0, org.joml.Vector3i arg1);
    org.joml.Vector3i mul(int arg0, int arg1, int arg2, org.joml.Vector3i arg3);
    org.joml.Vector3i div(float arg0, org.joml.Vector3i arg1);
    org.joml.Vector3i div(int arg0, org.joml.Vector3i arg1);
    long lengthSquared();
    double length();
    double distance(org.joml.Vector3ic arg0);
    double distance(int arg0, int arg1, int arg2);
    long gridDistance(org.joml.Vector3ic arg0);
    long gridDistance(int arg0, int arg1, int arg2);
    long distanceSquared(org.joml.Vector3ic arg0);
    long distanceSquared(int arg0, int arg1, int arg2);
    org.joml.Vector3i negate(org.joml.Vector3i arg0);
    org.joml.Vector3i min(org.joml.Vector3ic arg0, org.joml.Vector3i arg1);
    org.joml.Vector3i max(org.joml.Vector3ic arg0, org.joml.Vector3i arg1);
    int get(int arg0) throws java.lang.IllegalArgumentException;
    int maxComponent();
    int minComponent();
    org.joml.Vector3i absolute(org.joml.Vector3i arg0);
    boolean equals(int arg0, int arg1, int arg2);
}
