package com.github.martinambrus.rdforward.android.game;

import com.github.martinambrus.rdforward.render.RDMeshBuilder;

public class Tile {
    public static final Tile rock = new Tile(0);
    public static final Tile grass = new Tile(1);
    private final int tex;

    private Tile(int tex) { this.tex = tex; }

    public void render(RDMeshBuilder mb, Level level, int layer, int x, int y, int z) {
        float u0 = (float) tex / 16.0F;
        float u1 = u0 + 0.0624375F;
        float v0 = 0.0F;
        float v1 = v0 + 0.0624375F;
        float c1 = 1.0F, c2 = 0.8F, c3 = 0.6F;
        float x0 = x, x1 = x + 1.0F;
        float y0 = y, y1 = y + 1.0F;
        float z0 = z, z1 = z + 1.0F;

        // Bottom face (y-1)
        if (!level.isSolidTile(x, y - 1, z)) {
            float br = level.getBrightness(x, y - 1, z) * c1;
            if (br == c1 ^ layer == 1) {
                mb.color(br, br, br);
                mb.tex(u0, v1); mb.vertex(x0, y0, z1);
                mb.tex(u0, v0); mb.vertex(x0, y0, z0);
                mb.tex(u1, v0); mb.vertex(x1, y0, z0);
                mb.tex(u1, v1); mb.vertex(x1, y0, z1);
            }
        }
        // Top face (y+1)
        if (!level.isSolidTile(x, y + 1, z)) {
            float br = level.getBrightness(x, y, z) * c1;
            if (br == c1 ^ layer == 1) {
                mb.color(br, br, br);
                mb.tex(u1, v1); mb.vertex(x1, y1, z1);
                mb.tex(u1, v0); mb.vertex(x1, y1, z0);
                mb.tex(u0, v0); mb.vertex(x0, y1, z0);
                mb.tex(u0, v1); mb.vertex(x0, y1, z1);
            }
        }
        // Front face (z-1)
        if (!level.isSolidTile(x, y, z - 1)) {
            float br = level.getBrightness(x, y, z - 1) * c2;
            if (br == c2 ^ layer == 1) {
                mb.color(br, br, br);
                mb.tex(u1, v0); mb.vertex(x0, y1, z0);
                mb.tex(u0, v0); mb.vertex(x1, y1, z0);
                mb.tex(u0, v1); mb.vertex(x1, y0, z0);
                mb.tex(u1, v1); mb.vertex(x0, y0, z0);
            }
        }
        // Back face (z+1)
        if (!level.isSolidTile(x, y, z + 1)) {
            float br = level.getBrightness(x, y, z + 1) * c2;
            if (br == c2 ^ layer == 1) {
                mb.color(br, br, br);
                mb.tex(u0, v0); mb.vertex(x0, y1, z1);
                mb.tex(u0, v1); mb.vertex(x0, y0, z1);
                mb.tex(u1, v1); mb.vertex(x1, y0, z1);
                mb.tex(u1, v0); mb.vertex(x1, y1, z1);
            }
        }
        // Left face (x-1)
        if (!level.isSolidTile(x - 1, y, z)) {
            float br = level.getBrightness(x - 1, y, z) * c3;
            if (br == c3 ^ layer == 1) {
                mb.color(br, br, br);
                mb.tex(u1, v0); mb.vertex(x0, y1, z1);
                mb.tex(u0, v0); mb.vertex(x0, y1, z0);
                mb.tex(u0, v1); mb.vertex(x0, y0, z0);
                mb.tex(u1, v1); mb.vertex(x0, y0, z1);
            }
        }
        // Right face (x+1)
        if (!level.isSolidTile(x + 1, y, z)) {
            float br = level.getBrightness(x + 1, y, z) * c3;
            if (br == c3 ^ layer == 1) {
                mb.color(br, br, br);
                mb.tex(u0, v1); mb.vertex(x1, y0, z1);
                mb.tex(u1, v1); mb.vertex(x1, y0, z0);
                mb.tex(u1, v0); mb.vertex(x1, y1, z0);
                mb.tex(u0, v0); mb.vertex(x1, y1, z1);
            }
        }
    }

    public void renderFace(RDMeshBuilder mb, int x, int y, int z, int face) {
        float x0 = x, x1 = x + 1.0F;
        float y0 = y, y1 = y + 1.0F;
        float z0 = z, z1 = z + 1.0F;
        switch (face) {
            case 0: mb.vertex(x0,y0,z1); mb.vertex(x0,y0,z0); mb.vertex(x1,y0,z0); mb.vertex(x1,y0,z1); break;
            case 1: mb.vertex(x1,y1,z1); mb.vertex(x1,y1,z0); mb.vertex(x0,y1,z0); mb.vertex(x0,y1,z1); break;
            case 2: mb.vertex(x0,y1,z0); mb.vertex(x1,y1,z0); mb.vertex(x1,y0,z0); mb.vertex(x0,y0,z0); break;
            case 3: mb.vertex(x0,y1,z1); mb.vertex(x0,y0,z1); mb.vertex(x1,y0,z1); mb.vertex(x1,y1,z1); break;
            case 4: mb.vertex(x0,y1,z1); mb.vertex(x0,y1,z0); mb.vertex(x0,y0,z0); mb.vertex(x0,y0,z1); break;
            case 5: mb.vertex(x1,y0,z1); mb.vertex(x1,y0,z0); mb.vertex(x1,y1,z0); mb.vertex(x1,y1,z1); break;
        }
    }
}
