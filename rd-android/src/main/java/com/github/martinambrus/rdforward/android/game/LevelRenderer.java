package com.github.martinambrus.rdforward.android.game;

import com.github.martinambrus.rdforward.render.libgdx.LibGDXGraphics;

public class LevelRenderer implements LevelListener {
    private static final int CHUNK_SIZE = 16;
    private final Level level;
    private final Chunk[] chunks;
    private final int xChunks, yChunks, zChunks;
    private final Frustum frustum = new Frustum();
    private final LibGDXGraphics graphics;

    public LevelRenderer(Level level, LibGDXGraphics graphics) {
        this.level = level;
        this.graphics = graphics;
        level.addListener(this);
        xChunks = level.width / CHUNK_SIZE;
        yChunks = level.depth / CHUNK_SIZE;
        zChunks = level.height / CHUNK_SIZE;
        chunks = new Chunk[xChunks * yChunks * zChunks];

        for (int x = 0; x < xChunks; x++) {
            for (int y = 0; y < yChunks; y++) {
                for (int z = 0; z < zChunks; z++) {
                    int cx0 = x * CHUNK_SIZE;
                    int cy0 = y * CHUNK_SIZE;
                    int cz0 = z * CHUNK_SIZE;
                    int cx1 = Math.min((x + 1) * CHUNK_SIZE, level.width);
                    int cy1 = Math.min((y + 1) * CHUNK_SIZE, level.depth);
                    int cz1 = Math.min((z + 1) * CHUNK_SIZE, level.height);
                    chunks[(x + y * xChunks) * zChunks + z] =
                        new Chunk(level, graphics, cx0, cy0, cz0, cx1, cy1, cz1);
                }
            }
        }
    }

    public void render(int layer) {
        Chunk.rebuiltThisFrame = 0;
        frustum.calculate(graphics);
        for (Chunk chunk : chunks) {
            if (frustum.cubeInFrustum(chunk.aabb)) {
                chunk.render(layer);
            }
        }
    }

    public void renderHit(HitResult h) {
        graphics.enableBlend();
        graphics.blendFunc(
            com.github.martinambrus.rdforward.render.BlendFactor.SRC_ALPHA,
            com.github.martinambrus.rdforward.render.BlendFactor.ONE);
        float pulse = (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.2F + 0.4F;
        graphics.setColor(1.0F, 1.0F, 1.0F, pulse);
        graphics.disableTexture2D();
        graphics.beginQuads();
        renderFaceImmediate(h.x, h.y, h.z, h.f);
        graphics.endDraw();
        graphics.disableBlend();
    }

    private void renderFaceImmediate(int x, int y, int z, int face) {
        float x0 = x, x1 = x + 1.0f, y0 = y, y1 = y + 1.0f, z0 = z, z1 = z + 1.0f;
        switch (face) {
            case 0: graphics.vertex3f(x0,y0,z1); graphics.vertex3f(x0,y0,z0);
                    graphics.vertex3f(x1,y0,z0); graphics.vertex3f(x1,y0,z1); break;
            case 1: graphics.vertex3f(x1,y1,z1); graphics.vertex3f(x1,y1,z0);
                    graphics.vertex3f(x0,y1,z0); graphics.vertex3f(x0,y1,z1); break;
            case 2: graphics.vertex3f(x0,y1,z0); graphics.vertex3f(x1,y1,z0);
                    graphics.vertex3f(x1,y0,z0); graphics.vertex3f(x0,y0,z0); break;
            case 3: graphics.vertex3f(x0,y1,z1); graphics.vertex3f(x0,y0,z1);
                    graphics.vertex3f(x1,y0,z1); graphics.vertex3f(x1,y1,z1); break;
            case 4: graphics.vertex3f(x0,y1,z1); graphics.vertex3f(x0,y1,z0);
                    graphics.vertex3f(x0,y0,z0); graphics.vertex3f(x0,y0,z1); break;
            case 5: graphics.vertex3f(x1,y0,z1); graphics.vertex3f(x1,y0,z0);
                    graphics.vertex3f(x1,y1,z0); graphics.vertex3f(x1,y1,z1); break;
        }
    }

    private void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
        x0 /= CHUNK_SIZE; x1 /= CHUNK_SIZE;
        y0 /= CHUNK_SIZE; y1 /= CHUNK_SIZE;
        z0 /= CHUNK_SIZE; z1 /= CHUNK_SIZE;
        x0 = Math.max(0, x0); y0 = Math.max(0, y0); z0 = Math.max(0, z0);
        x1 = Math.min(xChunks - 1, x1);
        y1 = Math.min(yChunks - 1, y1);
        z1 = Math.min(zChunks - 1, z1);
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++)
                for (int z = z0; z <= z1; z++)
                    chunks[(x + y * xChunks) * zChunks + z].setDirty();
    }

    @Override
    public void tileChanged(int x, int y, int z) {
        setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void lightColumnChanged(int x, int z, int y0, int y1) {
        setDirty(x - 1, y0 - 1, z - 1, x + 1, y1 + 1, z + 1);
    }

    @Override
    public void allChanged() {
        setDirty(0, 0, 0, level.width, level.depth, level.height);
    }
}
