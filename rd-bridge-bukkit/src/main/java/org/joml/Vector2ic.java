package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vector2ic {
    int x();
    int y();
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    java.nio.IntBuffer get(java.nio.IntBuffer arg0);
    java.nio.IntBuffer get(int arg0, java.nio.IntBuffer arg1);
    org.joml.Vector2ic getToAddress(long arg0);
    org.joml.Vector2i sub(org.joml.Vector2ic arg0, org.joml.Vector2i arg1);
    org.joml.Vector2i sub(int arg0, int arg1, org.joml.Vector2i arg2);
    long lengthSquared();
    double length();
    double distance(org.joml.Vector2ic arg0);
    double distance(int arg0, int arg1);
    long distanceSquared(org.joml.Vector2ic arg0);
    long distanceSquared(int arg0, int arg1);
    long gridDistance(org.joml.Vector2ic arg0);
    long gridDistance(int arg0, int arg1);
    org.joml.Vector2i add(org.joml.Vector2ic arg0, org.joml.Vector2i arg1);
    org.joml.Vector2i add(int arg0, int arg1, org.joml.Vector2i arg2);
    org.joml.Vector2i mul(int arg0, org.joml.Vector2i arg1);
    org.joml.Vector2i mul(org.joml.Vector2ic arg0, org.joml.Vector2i arg1);
    org.joml.Vector2i mul(int arg0, int arg1, org.joml.Vector2i arg2);
    org.joml.Vector2i div(float arg0, org.joml.Vector2i arg1);
    org.joml.Vector2i div(int arg0, org.joml.Vector2i arg1);
    org.joml.Vector2i negate(org.joml.Vector2i arg0);
    org.joml.Vector2i min(org.joml.Vector2ic arg0, org.joml.Vector2i arg1);
    org.joml.Vector2i max(org.joml.Vector2ic arg0, org.joml.Vector2i arg1);
    int maxComponent();
    int minComponent();
    org.joml.Vector2i absolute(org.joml.Vector2i arg0);
    int get(int arg0) throws java.lang.IllegalArgumentException;
    boolean equals(int arg0, int arg1);
}
