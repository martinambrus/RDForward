package com.github.martinambrus.rdforward.render.desktop;

import com.github.martinambrus.rdforward.render.RDMeshBuilder;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

/**
 * LWJGL 3 implementation of {@link RDMeshBuilder}, matching the original
 * RubyDung {@code Tesselator} — vertex arrays drawn as {@code GL_QUADS}.
 */
public class LwjglMeshBuilder implements RDMeshBuilder {

    private static final int MAX_VERTICES = 100_000;

    private final FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 3);
    private final FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 2);
    private final FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 3);

    private int vertices;
    private float u, v;
    private float r, g, b;
    private boolean hasTexture;
    private boolean hasColor;

    @Override
    public void init() {
        vertices = 0;
        hasTexture = false;
        hasColor = false;
    }

    @Override
    public void tex(float u, float v) {
        hasTexture = true;
        this.u = u;
        this.v = v;
    }

    @Override
    public void color(float r, float g, float b) {
        hasColor = true;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public void vertex(float x, float y, float z) {
        vertexBuffer.put(vertices * 3, x)
                .put(vertices * 3 + 1, y)
                .put(vertices * 3 + 2, z);
        texCoordBuffer.put(vertices * 2, u)
                .put(vertices * 2 + 1, v);
        colorBuffer.put(vertices * 3, r)
                .put(vertices * 3 + 1, g)
                .put(vertices * 3 + 2, b);
        vertices++;
        if (vertices == MAX_VERTICES) {
            flush();
        }
    }

    @Override
    public void flush() {
        if (vertices == 0) return;

        vertexBuffer.position(0).limit(vertices * 3);
        texCoordBuffer.position(0).limit(vertices * 2);
        colorBuffer.position(0).limit(vertices * 3);

        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexBuffer);
        if (hasTexture) {
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, texCoordBuffer);
        }
        if (hasColor) {
            GL11.glColorPointer(3, GL11.GL_FLOAT, 0, colorBuffer);
        }

        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        if (hasTexture) GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        if (hasColor) GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);

        GL11.glDrawArrays(GL11.GL_QUADS, 0, vertices);

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        if (hasTexture) GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        if (hasColor) GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);

        vertices = 0;
    }

    @Override
    public boolean hasTexture() {
        return hasTexture;
    }

    @Override
    public boolean hasColor() {
        return hasColor;
    }

    @Override
    public int vertexCount() {
        return vertices;
    }
}
