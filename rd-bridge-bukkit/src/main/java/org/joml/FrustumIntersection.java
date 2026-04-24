package org.joml;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class FrustumIntersection {
    public static final int PLANE_NX = 0;
    public static final int PLANE_PX = 1;
    public static final int PLANE_NY = 2;
    public static final int PLANE_PY = 3;
    public static final int PLANE_NZ = 4;
    public static final int PLANE_PZ = 5;
    public static final int INTERSECT = -1;
    public static final int INSIDE = -2;
    public static final int OUTSIDE = -3;
    public static final int PLANE_MASK_NX = 1;
    public static final int PLANE_MASK_PX = 2;
    public static final int PLANE_MASK_NY = 4;
    public static final int PLANE_MASK_PY = 8;
    public static final int PLANE_MASK_NZ = 16;
    public static final int PLANE_MASK_PZ = 32;
    public FrustumIntersection() {}
    public FrustumIntersection(org.joml.Matrix4fc arg0) {}
    public FrustumIntersection(org.joml.Matrix4fc arg0, boolean arg1) {}
    public org.joml.FrustumIntersection set(org.joml.Matrix4fc arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.set(Lorg/joml/Matrix4fc;)Lorg/joml/FrustumIntersection;");
        return this;
    }
    public org.joml.FrustumIntersection set(org.joml.Matrix4fc arg0, boolean arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.set(Lorg/joml/Matrix4fc;Z)Lorg/joml/FrustumIntersection;");
        return this;
    }
    public boolean testPoint(org.joml.Vector3fc arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testPoint(Lorg/joml/Vector3fc;)Z");
        return false;
    }
    public boolean testPoint(float arg0, float arg1, float arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testPoint(FFF)Z");
        return false;
    }
    public boolean testSphere(org.joml.Vector3fc arg0, float arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testSphere(Lorg/joml/Vector3fc;F)Z");
        return false;
    }
    public boolean testSphere(float arg0, float arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testSphere(FFFF)Z");
        return false;
    }
    public int intersectSphere(org.joml.Vector3fc arg0, float arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectSphere(Lorg/joml/Vector3fc;F)I");
        return 0;
    }
    public int intersectSphere(float arg0, float arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectSphere(FFFF)I");
        return 0;
    }
    public boolean testAab(org.joml.Vector3fc arg0, org.joml.Vector3fc arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testAab(Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;)Z");
        return false;
    }
    public boolean testAab(float arg0, float arg1, float arg2, float arg3, float arg4, float arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testAab(FFFFFF)Z");
        return false;
    }
    public boolean testPlaneXY(org.joml.Vector2fc arg0, org.joml.Vector2fc arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testPlaneXY(Lorg/joml/Vector2fc;Lorg/joml/Vector2fc;)Z");
        return false;
    }
    public boolean testPlaneXY(float arg0, float arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testPlaneXY(FFFF)Z");
        return false;
    }
    public boolean testPlaneXZ(float arg0, float arg1, float arg2, float arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testPlaneXZ(FFFF)Z");
        return false;
    }
    public int intersectAab(org.joml.Vector3fc arg0, org.joml.Vector3fc arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectAab(Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;)I");
        return 0;
    }
    public int intersectAab(float arg0, float arg1, float arg2, float arg3, float arg4, float arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectAab(FFFFFF)I");
        return 0;
    }
    public float distanceToPlane(float arg0, float arg1, float arg2, float arg3, float arg4, float arg5, int arg6) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.distanceToPlane(FFFFFFI)F");
        return 0.0f;
    }
    public int intersectAab(org.joml.Vector3fc arg0, org.joml.Vector3fc arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectAab(Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;I)I");
        return 0;
    }
    public int intersectAab(float arg0, float arg1, float arg2, float arg3, float arg4, float arg5, int arg6) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectAab(FFFFFFI)I");
        return 0;
    }
    public int intersectAab(org.joml.Vector3fc arg0, org.joml.Vector3fc arg1, int arg2, int arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectAab(Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;II)I");
        return 0;
    }
    public int intersectAab(float arg0, float arg1, float arg2, float arg3, float arg4, float arg5, int arg6, int arg7) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.intersectAab(FFFFFFII)I");
        return 0;
    }
    public boolean testLineSegment(org.joml.Vector3fc arg0, org.joml.Vector3fc arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testLineSegment(Lorg/joml/Vector3fc;Lorg/joml/Vector3fc;)Z");
        return false;
    }
    public boolean testLineSegment(float arg0, float arg1, float arg2, float arg3, float arg4, float arg5) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.joml.FrustumIntersection.testLineSegment(FFFFFF)Z");
        return false;
    }
}
