package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Matrix2fc {
    float m00();
    float m01();
    float m10();
    float m11();
    org.joml.Matrix2f mul(org.joml.Matrix2fc arg0, org.joml.Matrix2f arg1);
    org.joml.Matrix2f mulLocal(org.joml.Matrix2fc arg0, org.joml.Matrix2f arg1);
    float determinant();
    org.joml.Matrix2f invert(org.joml.Matrix2f arg0);
    org.joml.Matrix2f transpose(org.joml.Matrix2f arg0);
    org.joml.Matrix2f get(org.joml.Matrix2f arg0);
    org.joml.Matrix3x2f get(org.joml.Matrix3x2f arg0);
    org.joml.Matrix3f get(org.joml.Matrix3f arg0);
    float getRotation();
    java.nio.FloatBuffer get(java.nio.FloatBuffer arg0);
    java.nio.FloatBuffer get(int arg0, java.nio.FloatBuffer arg1);
    java.nio.ByteBuffer get(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer get(int arg0, java.nio.ByteBuffer arg1);
    java.nio.FloatBuffer getTransposed(java.nio.FloatBuffer arg0);
    java.nio.FloatBuffer getTransposed(int arg0, java.nio.FloatBuffer arg1);
    java.nio.ByteBuffer getTransposed(java.nio.ByteBuffer arg0);
    java.nio.ByteBuffer getTransposed(int arg0, java.nio.ByteBuffer arg1);
    org.joml.Matrix2fc getToAddress(long arg0);
    org.joml.Matrix2fc getTransposedToAddress(long arg0);
    float[] get(float[] arg0, int arg1);
    float[] get(float[] arg0);
    org.joml.Matrix2f scale(org.joml.Vector2fc arg0, org.joml.Matrix2f arg1);
    org.joml.Matrix2f scale(float arg0, float arg1, org.joml.Matrix2f arg2);
    org.joml.Matrix2f scale(float arg0, org.joml.Matrix2f arg1);
    org.joml.Matrix2f scaleLocal(float arg0, float arg1, org.joml.Matrix2f arg2);
    org.joml.Vector2f transform(org.joml.Vector2f arg0);
    org.joml.Vector2f transform(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f transform(float arg0, float arg1, org.joml.Vector2f arg2);
    org.joml.Vector2f transformTranspose(org.joml.Vector2f arg0);
    org.joml.Vector2f transformTranspose(org.joml.Vector2fc arg0, org.joml.Vector2f arg1);
    org.joml.Vector2f transformTranspose(float arg0, float arg1, org.joml.Vector2f arg2);
    org.joml.Matrix2f rotate(float arg0, org.joml.Matrix2f arg1);
    org.joml.Matrix2f rotateLocal(float arg0, org.joml.Matrix2f arg1);
    org.joml.Vector2f getRow(int arg0, org.joml.Vector2f arg1) throws java.lang.IndexOutOfBoundsException;
    org.joml.Vector2f getColumn(int arg0, org.joml.Vector2f arg1) throws java.lang.IndexOutOfBoundsException;
    float get(int arg0, int arg1);
    org.joml.Matrix2f normal(org.joml.Matrix2f arg0);
    org.joml.Vector2f getScale(org.joml.Vector2f arg0);
    org.joml.Vector2f positiveX(org.joml.Vector2f arg0);
    org.joml.Vector2f normalizedPositiveX(org.joml.Vector2f arg0);
    org.joml.Vector2f positiveY(org.joml.Vector2f arg0);
    org.joml.Vector2f normalizedPositiveY(org.joml.Vector2f arg0);
    org.joml.Matrix2f add(org.joml.Matrix2fc arg0, org.joml.Matrix2f arg1);
    org.joml.Matrix2f sub(org.joml.Matrix2fc arg0, org.joml.Matrix2f arg1);
    org.joml.Matrix2f mulComponentWise(org.joml.Matrix2fc arg0, org.joml.Matrix2f arg1);
    org.joml.Matrix2f lerp(org.joml.Matrix2fc arg0, float arg1, org.joml.Matrix2f arg2);
    boolean equals(org.joml.Matrix2fc arg0, float arg1);
    boolean isFinite();
}
