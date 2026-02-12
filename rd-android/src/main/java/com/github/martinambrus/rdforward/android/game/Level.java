package com.github.martinambrus.rdforward.android.game;

import java.util.ArrayList;
import java.util.List;

public class Level {
    public final int width;
    public final int height;
    public final int depth;
    private byte[] blocks;
    private int[] lightDepths;
    private final List<LevelListener> levelListeners = new ArrayList<>();

    public Level(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.blocks = new byte[w * h * d];
        this.lightDepths = new int[w * h];

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < d; y++) {
                for (int z = 0; z < h; z++) {
                    int i = (y * this.height + z) * this.width + x;
                    this.blocks[i] = (byte) (y <= d * 2 / 3 ? 1 : 0);
                }
            }
        }
        this.calcLightDepths(0, 0, w, h);
    }

    public void calcLightDepths(int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x0 + x1; x++) {
            for (int z = y0; z < y0 + y1; z++) {
                int oldDepth = this.lightDepths[x + z * this.width];
                int y = this.depth - 1;
                while (y > 0 && !this.isLightBlocker(x, y, z)) y--;
                this.lightDepths[x + z * this.width] = y;
                if (oldDepth != y) {
                    int yl0 = Math.min(oldDepth, y);
                    int yl1 = Math.max(oldDepth, y);
                    for (LevelListener l : this.levelListeners) {
                        l.lightColumnChanged(x, z, yl0, yl1);
                    }
                }
            }
        }
    }

    public void addListener(LevelListener l) { this.levelListeners.add(l); }

    public boolean isTile(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) return false;
        return this.blocks[(y * this.height + z) * this.width + x] == 1;
    }

    public boolean isSolidTile(int x, int y, int z) { return isTile(x, y, z); }
    public boolean isLightBlocker(int x, int y, int z) { return isSolidTile(x, y, z); }

    public List<AABB> getCubes(AABB aabb) {
        List<AABB> result = new ArrayList<>();
        int x0 = Math.max(0, (int) aabb.x0);
        int x1 = Math.min(width, (int) (aabb.x1 + 1.0F));
        int y0 = Math.max(0, (int) aabb.y0);
        int y1 = Math.min(depth, (int) (aabb.y1 + 1.0F));
        int z0 = Math.max(0, (int) aabb.z0);
        int z1 = Math.min(height, (int) (aabb.z1 + 1.0F));
        for (int x = x0; x < x1; x++)
            for (int y = y0; y < y1; y++)
                for (int z = z0; z < z1; z++)
                    if (isSolidTile(x, y, z))
                        result.add(new AABB(x, y, z, x + 1, y + 1, z + 1));
        return result;
    }

    public float getBrightness(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) return 1.0F;
        return y < this.lightDepths[x + z * this.width] ? 0.8F : 1.0F;
    }

    public void setTile(int x, int y, int z, int type) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) return;
        this.blocks[(y * this.height + z) * this.width + x] = (byte) type;
        this.calcLightDepths(x, z, 1, 1);
        for (LevelListener l : this.levelListeners) {
            l.tileChanged(x, y, z);
        }
    }
}
