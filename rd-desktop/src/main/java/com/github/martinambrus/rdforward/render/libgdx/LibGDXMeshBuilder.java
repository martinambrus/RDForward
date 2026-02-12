package com.github.martinambrus.rdforward.render.libgdx;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.github.martinambrus.rdforward.render.RDMeshBuilder;

/**
 * libGDX implementation of {@link RDMeshBuilder}. Collects quads as
 * position + colour + texcoord, then converts to triangles and renders
 * via a libGDX {@link Mesh} (VBO).
 * <p>
 * Each vertex has 9 floats: x, y, z, r, g, b, a, u, v.
 */
public class LibGDXMeshBuilder implements RDMeshBuilder {

    private static final int MAX_QUADS = 25_000; // 100K vertices / 4
    private static final int FLOATS_PER_VERTEX = 9;
    private static final int VERTS_PER_QUAD = 4;
    private static final int TRIS_PER_QUAD = 6; // 2 triangles

    // Quad buffer: stores 4 vertices per quad
    private final float[] quadBuffer = new float[MAX_QUADS * VERTS_PER_QUAD * FLOATS_PER_VERTEX];
    // Triangle buffer: stores 6 vertices per quad (for GL_TRIANGLES)
    private final float[] triBuffer = new float[MAX_QUADS * TRIS_PER_QUAD * FLOATS_PER_VERTEX];

    private int vertices;
    private float u, v;
    private float r = 1, g = 1, b = 1;
    private boolean hasTexture;
    private boolean hasColor;

    // Reusable mesh for flushing (avoids allocation per flush)
    private Mesh flushMesh;
    private ShaderProgram shader;

    /** Set the shader to use when flushing. Called by LibGDXGraphics. */
    void setShader(ShaderProgram shader) {
        this.shader = shader;
    }

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
        int base = vertices * FLOATS_PER_VERTEX;
        quadBuffer[base]     = x;
        quadBuffer[base + 1] = y;
        quadBuffer[base + 2] = z;
        quadBuffer[base + 3] = r;
        quadBuffer[base + 4] = g;
        quadBuffer[base + 5] = b;
        quadBuffer[base + 6] = 1.0f; // alpha
        quadBuffer[base + 7] = u;
        quadBuffer[base + 8] = v;
        vertices++;
        if (vertices == MAX_QUADS * VERTS_PER_QUAD) {
            flush();
        }
    }

    @Override
    public void flush() {
        if (vertices == 0) return;

        // Convert quads to triangles
        int quadCount = vertices / 4;
        int triVertCount = quadCount * 6;

        for (int q = 0; q < quadCount; q++) {
            int src = q * 4 * FLOATS_PER_VERTEX;
            int dst = q * 6 * FLOATS_PER_VERTEX;
            // Triangle 1: v0, v1, v2
            System.arraycopy(quadBuffer, src, triBuffer, dst, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + FLOATS_PER_VERTEX, triBuffer,
                    dst + FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + 2 * FLOATS_PER_VERTEX, triBuffer,
                    dst + 2 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            // Triangle 2: v0, v2, v3
            System.arraycopy(quadBuffer, src, triBuffer,
                    dst + 3 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + 2 * FLOATS_PER_VERTEX, triBuffer,
                    dst + 4 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + 3 * FLOATS_PER_VERTEX, triBuffer,
                    dst + 5 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
        }

        // Create or re-use mesh
        if (flushMesh == null || flushMesh.getMaxVertices() < triVertCount) {
            if (flushMesh != null) flushMesh.dispose();
            flushMesh = new Mesh(false, Math.max(triVertCount, 6000), 0,
                    new VertexAttribute(Usage.Position, 3, "a_position"),
                    new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
                    new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
        }

        flushMesh.setVertices(triBuffer, 0, triVertCount * FLOATS_PER_VERTEX);

        if (shader != null) {
            flushMesh.render(shader, GL20.GL_TRIANGLES, 0, triVertCount);
        }

        vertices = 0;
    }

    /**
     * Build a standalone Mesh (VBO) from the current buffer state.
     * Used by compiled meshes (display list replacement).
     */
    Mesh buildMesh() {
        int quadCount = vertices / 4;
        int triVertCount = quadCount * 6;

        for (int q = 0; q < quadCount; q++) {
            int src = q * 4 * FLOATS_PER_VERTEX;
            int dst = q * 6 * FLOATS_PER_VERTEX;
            System.arraycopy(quadBuffer, src, triBuffer, dst, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + FLOATS_PER_VERTEX, triBuffer,
                    dst + FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + 2 * FLOATS_PER_VERTEX, triBuffer,
                    dst + 2 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src, triBuffer,
                    dst + 3 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + 2 * FLOATS_PER_VERTEX, triBuffer,
                    dst + 4 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
            System.arraycopy(quadBuffer, src + 3 * FLOATS_PER_VERTEX, triBuffer,
                    dst + 5 * FLOATS_PER_VERTEX, FLOATS_PER_VERTEX);
        }

        Mesh mesh = new Mesh(true, triVertCount, 0,
                new VertexAttribute(Usage.Position, 3, "a_position"),
                new VertexAttribute(Usage.ColorUnpacked, 4, "a_color"),
                new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
        mesh.setVertices(triBuffer, 0, triVertCount * FLOATS_PER_VERTEX);
        return mesh;
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
