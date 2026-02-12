package com.github.martinambrus.rdforward.android.game;

import com.github.martinambrus.rdforward.render.libgdx.LibGDXGraphics;

public class Frustum {
    private final float[][] planes = new float[6][4];

    public void calculate(LibGDXGraphics graphics) {
        float[] proj = graphics.getProjectionMatrix();
        float[] modl = graphics.getModelViewMatrix();
        float[] clip = new float[16];

        // Multiply modelview × projection (column-major)
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                clip[i * 4 + j] =
                    modl[i * 4] * proj[j] +
                    modl[i * 4 + 1] * proj[4 + j] +
                    modl[i * 4 + 2] * proj[8 + j] +
                    modl[i * 4 + 3] * proj[12 + j];
            }
        }

        // Extract frustum planes
        // Right
        planes[0][0] = clip[3] - clip[0]; planes[0][1] = clip[7] - clip[4];
        planes[0][2] = clip[11] - clip[8]; planes[0][3] = clip[15] - clip[12];
        normalize(0);
        // Left
        planes[1][0] = clip[3] + clip[0]; planes[1][1] = clip[7] + clip[4];
        planes[1][2] = clip[11] + clip[8]; planes[1][3] = clip[15] + clip[12];
        normalize(1);
        // Bottom
        planes[2][0] = clip[3] + clip[1]; planes[2][1] = clip[7] + clip[5];
        planes[2][2] = clip[11] + clip[9]; planes[2][3] = clip[15] + clip[13];
        normalize(2);
        // Top
        planes[3][0] = clip[3] - clip[1]; planes[3][1] = clip[7] - clip[5];
        planes[3][2] = clip[11] - clip[9]; planes[3][3] = clip[15] - clip[13];
        normalize(3);
        // Back
        planes[4][0] = clip[3] - clip[2]; planes[4][1] = clip[7] - clip[6];
        planes[4][2] = clip[11] - clip[10]; planes[4][3] = clip[15] - clip[14];
        normalize(4);
        // Front
        planes[5][0] = clip[3] + clip[2]; planes[5][1] = clip[7] + clip[6];
        planes[5][2] = clip[11] + clip[10]; planes[5][3] = clip[15] + clip[14];
        normalize(5);
    }

    private void normalize(int side) {
        float mag = (float) Math.sqrt(
            planes[side][0] * planes[side][0] +
            planes[side][1] * planes[side][1] +
            planes[side][2] * planes[side][2]);
        planes[side][0] /= mag; planes[side][1] /= mag;
        planes[side][2] /= mag; planes[side][3] /= mag;
    }

    public boolean cubeInFrustum(AABB aabb) {
        return cubeInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1);
    }

    public boolean cubeInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        for (int i = 0; i < 6; i++) {
            if (planes[i][0] * x1 + planes[i][1] * y1 + planes[i][2] * z1 + planes[i][3] > 0) continue;
            if (planes[i][0] * x2 + planes[i][1] * y1 + planes[i][2] * z1 + planes[i][3] > 0) continue;
            if (planes[i][0] * x1 + planes[i][1] * y2 + planes[i][2] * z1 + planes[i][3] > 0) continue;
            if (planes[i][0] * x2 + planes[i][1] * y2 + planes[i][2] * z1 + planes[i][3] > 0) continue;
            if (planes[i][0] * x1 + planes[i][1] * y1 + planes[i][2] * z2 + planes[i][3] > 0) continue;
            if (planes[i][0] * x2 + planes[i][1] * y1 + planes[i][2] * z2 + planes[i][3] > 0) continue;
            if (planes[i][0] * x1 + planes[i][1] * y2 + planes[i][2] * z2 + planes[i][3] > 0) continue;
            if (planes[i][0] * x2 + planes[i][1] * y2 + planes[i][2] * z2 + planes[i][3] > 0) continue;
            return false;
        }
        return true;
    }
}
