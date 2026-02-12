package com.github.martinambrus.rdforward.android.game;

import com.github.martinambrus.rdforward.render.RDMeshBuilder;
import com.github.martinambrus.rdforward.render.libgdx.LibGDXGraphics;

public class Chunk {
    public final AABB aabb;
    public final Level level;
    public final int x0, y0, z0, x1, y1, z1;
    private boolean dirty = true;
    private final int meshIds;
    private final LibGDXGraphics graphics;
    public static int rebuiltThisFrame = 0;

    public Chunk(Level level, LibGDXGraphics graphics, int x0, int y0, int z0, int x1, int y1, int z1) {
        this.level = level;
        this.graphics = graphics;
        this.x0 = x0; this.y0 = y0; this.z0 = z0;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        this.aabb = new AABB(x0, y0, z0, x1, y1, z1);
        this.meshIds = graphics.genCompiledMeshes(2);
    }

    private void rebuild(int layer) {
        if (rebuiltThisFrame >= 2) return;
        dirty = false;
        rebuiltThisFrame++;
        graphics.beginCompile(meshIds + layer);
        RDMeshBuilder mb = graphics.meshBuilder();
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (level.isTile(x, y, z)) {
                        int tex = y == level.depth * 2 / 3 ? 0 : 1;
                        if (tex == 0) {
                            Tile.rock.render(mb, level, layer, x, y, z);
                        } else {
                            Tile.grass.render(mb, level, layer, x, y, z);
                        }
                    }
                }
            }
        }
        graphics.endCompile();
    }

    public void render(int layer) {
        if (dirty) {
            rebuild(0);
            rebuild(1);
        }
        graphics.renderCompiled(meshIds + layer);
    }

    public void setDirty() { dirty = true; }
}
