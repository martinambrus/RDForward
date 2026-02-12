package com.github.martinambrus.rdforward.render;

/**
 * Vertex batcher that replaces the original {@code Tesselator}. Collects
 * position, texture-coordinate and colour data, then flushes to the GPU.
 * <p>
 * On the LWJGL 3 desktop backend this uses vertex arrays and
 * {@code GL_QUADS}. On libGDX / Android it converts quads to triangles
 * and renders via VBOs.
 */
public interface RDMeshBuilder {

    /** Reset the builder for a new batch of vertices. */
    void init();

    /** Set the texture coordinate for subsequent vertices. */
    void tex(float u, float v);

    /** Set the colour for subsequent vertices. */
    void color(float r, float g, float b);

    /**
     * Emit a 3-D vertex with the most-recently-set tex coord and colour.
     * When the internal buffer is full the builder auto-flushes.
     */
    void vertex(float x, float y, float z);

    /** Send all buffered vertices to the GPU and reset the count. */
    void flush();

    /** @return true if {@link #tex} has been called since the last init. */
    boolean hasTexture();

    /** @return true if {@link #color} has been called since the last init. */
    boolean hasColor();

    /** @return the number of vertices currently buffered. */
    int vertexCount();
}
