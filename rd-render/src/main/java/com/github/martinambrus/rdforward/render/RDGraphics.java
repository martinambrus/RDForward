package com.github.martinambrus.rdforward.render;

import java.nio.ByteBuffer;

/**
 * Low-level graphics operations that abstract away the differences between
 * OpenGL 1.x (fixed-function) and OpenGL ES 2.0+ (shader-based).
 * <p>
 * The API surface intentionally mirrors the fixed-function pipeline because
 * the original RubyDung code uses it. Shader-based backends emulate these
 * operations internally.
 */
public interface RDGraphics {

    // ── Frame operations ───────────────────────────────────────────────

    void setClearColor(float r, float g, float b, float a);

    void setClearDepth(double depth);

    /** Clear the colour and depth buffers. */
    void clear();

    // ── Matrix stack ───────────────────────────────────────────────────

    /** Switch subsequent matrix operations to the projection matrix. */
    void matrixModeProjection();

    /** Switch subsequent matrix operations to the model-view matrix. */
    void matrixModeModelView();

    void pushMatrix();

    void popMatrix();

    void loadIdentity();

    void translate(float x, float y, float z);

    void rotate(float angle, float x, float y, float z);

    void scale(float x, float y, float z);

    /** Load a symmetric perspective projection. */
    void perspective(float fovYDeg, float aspect, float zNear, float zFar);

    /** Load an orthographic projection. */
    void ortho(double left, double right, double bottom, double top,
               double near, double far);

    /** Post-multiply the current matrix by a 16-element column-major matrix. */
    void multMatrix(float[] m16);

    /** @return a copy of the current projection matrix (16 floats, column-major). */
    float[] getProjectionMatrix();

    /** @return a copy of the current model-view matrix (16 floats, column-major). */
    float[] getModelViewMatrix();

    /** @return the current viewport as {x, y, width, height}. */
    int[] getViewport();

    // ── State save / restore ───────────────────────────────────────────

    /** Push all render state (colour, blend, depth, texture, matrix). */
    void pushAllState();

    /** Pop all render state saved by {@link #pushAllState()}. */
    void popAllState();

    // ── Render state ───────────────────────────────────────────────────

    void enableDepthTest();

    void disableDepthTest();

    void depthFunc(DepthFunc func);

    void enableCullFace();

    void disableCullFace();

    void enableBlend();

    void disableBlend();

    void blendFunc(BlendFactor src, BlendFactor dst);

    void enableFog(float density, float r, float g, float b, float a);

    void disableFog();

    void enableTexture2D();

    void disableTexture2D();

    /** No-op on shader-based backends. */
    void shadeSmooth();

    // ── Drawing state ──────────────────────────────────────────────────

    void setColor(float r, float g, float b, float a);

    void setLineWidth(float width);

    // ── Textures ───────────────────────────────────────────────────────

    /**
     * Load a texture from a classpath resource.
     *
     * @param resource   classpath path (e.g. "/terrain.png")
     * @param filter     texture filter mode
     * @return opaque texture id
     */
    int loadTexture(String resource, TextureFilter filter);

    /**
     * Create a texture from raw RGBA pixel data.
     *
     * @param w           width in pixels
     * @param h           height in pixels
     * @param rgbaPixels  RGBA bytes (4 bytes per pixel)
     * @param filter      texture filter mode
     * @param clampEdge   true for CLAMP_TO_EDGE, false for REPEAT
     * @return opaque texture id
     */
    int createTexture(int w, int h, ByteBuffer rgbaPixels,
                      TextureFilter filter, boolean clampEdge);

    void bindTexture(int textureId);

    void deleteTexture(int textureId);

    // ── Mesh builder ───────────────────────────────────────────────────

    /**
     * @return the shared mesh builder instance (replacement for Tesselator).
     *         Not thread-safe — call only from the render thread.
     */
    RDMeshBuilder meshBuilder();

    // ── Compiled meshes (display list replacement) ─────────────────────

    /**
     * Allocate opaque handles for compiled meshes.
     *
     * @param count number of handles to allocate
     * @return the first handle id (sequential: id, id+1, ..., id+count-1)
     */
    int genCompiledMeshes(int count);

    /** Begin recording draw calls into the given compiled mesh. */
    void beginCompile(int meshId);

    /** End recording and upload the compiled mesh. */
    void endCompile();

    /** Render a previously compiled mesh. */
    void renderCompiled(int meshId);

    // ── Picking (GL_SELECT replacement) ────────────────────────────────

    /** Begin pick mode. Subsequent draw calls populate the selection buffer. */
    void beginPick(java.nio.IntBuffer selectBuffer);

    /**
     * End pick mode.
     *
     * @return the number of hits recorded
     */
    int endPick();

    void initNames();

    void pushName(int name);

    void popName();

    // ── Immediate-mode drawing ─────────────────────────────────────────

    /** Begin submitting quads (4 vertices each). */
    void beginQuads();

    /** Begin submitting lines (2 vertices each). */
    void beginLines();

    void texCoord2f(float u, float v);

    void vertex2f(float x, float y);

    void vertex3f(float x, float y, float z);

    /** Finish the current immediate-mode batch and draw it. */
    void endDraw();
}
