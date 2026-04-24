package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vector2fc {
    float x();
    float y();
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    java.nio.FloatBuffer get(java.nio.FloatBuffer arg0);
    java.nio.FloatBuffer get(int arg0, java.nio.FloatBuffer arg1);
    org.joml.Vector2fc getToAddress(long arg0);
    org.joml.Vector2f sub(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f sub(float arg0, float arg1, org.joml.Vector2f arg2);
    float dot(org.joml.Vector2fc arg0);
    float angle(org.joml.Vector2fc arg0);
    float lengthSquared();
    float length();
    float distance(org.joml.Vector2fc arg0);
    float distanceSquared(org.joml.Vector2fc arg0);
    float distance(float arg0, float arg1);
    float distanceSquared(float arg0, float arg1);
    org.joml.Vector2f normalize(org.joml.Vector2f arg0);
    org.joml.Vector2f normalize(float arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f add(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f add(float arg0, float arg1, org.joml.Vector2f arg2);
    org.joml.Vector2f negate(org.joml.Vector2f arg0);
    org.joml.Vector2f mul(float arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f mul(float arg0, float arg1, org.joml.Vector2f arg2);
    org.joml.Vector2f mul(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f div(float arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f div(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f div(float arg0, float arg1, org.joml.Vector2f arg2);
    org.joml.Vector2f mul(org.joml.Matrix2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f mul(org.joml.Matrix2dc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f mulTranspose(org.joml.Matrix2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f mulPosition(org.joml.Matrix3x2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f mulDirection(org.joml.Matrix3x2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f lerp(org.joml.Vector2fc arg0, float arg1, org.joml.Vector2f arg2);
    org.joml.Vector2f fma(org.joml.Vector2fc arg0, org.joml.Vector2fc arg1, org.joml.Vector2f arg2);
    org.joml.Vector2f fma(float arg0, org.joml.Vector2fc arg1, org.joml.Vector2f arg2);
    org.joml.Vector2f min(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f max(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    int maxComponent();
    int minComponent();
    float get(int arg0) throws java.lang.IllegalArgumentException;
    org.joml.Vector2i get(int arg0, org.joml.Vector2i arg1);
    org.joml.Vector2f get(org.joml.Vector2f arg0);
    org.joml.Vector2d get(org.joml.Vector2d arg0);
    org.joml.Vector2f floor(org.joml.Vector2f arg0);
    org.joml.Vector2f ceil(org.joml.Vector2f arg0);
    org.joml.Vector2f round(org.joml.Vector2f arg0);
    boolean isFinite();
    org.joml.Vector2f absolute(org.joml.Vector2f arg0);
    boolean equals(org.joml.Vector2fc arg0, float arg1);
    boolean equals(float arg0, float arg1);
}
