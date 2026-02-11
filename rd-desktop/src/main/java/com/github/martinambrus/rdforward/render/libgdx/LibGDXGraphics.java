package com.github.martinambrus.rdforward.render.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import com.github.martinambrus.rdforward.render.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Shader-based implementation of {@link RDGraphics} using libGDX.
 * Emulates the OpenGL 1.x fixed-function pipeline using a custom shader pair.
 * <p>
 * Key differences from the LWJGL desktop backend:
 * <ul>
 *   <li>GL_QUADS are converted to GL_TRIANGLES (2 triangles per quad)</li>
 *   <li>Display lists are replaced by cached {@link Mesh} objects (VBOs)</li>
 *   <li>GL_SELECT picking is replaced by ray-AABB intersection</li>
 *   <li>Matrix stack is software-emulated</li>
 *   <li>Fog and lighting are implemented in shaders</li>
 * </ul>
 */
public class LibGDXGraphics implements RDGraphics {

    private ShaderProgram shader;
    private final LibGDXMeshBuilder meshBuilder = new LibGDXMeshBuilder();

    // Software matrix stack (OpenGL ES 2.0 has no matrix stack)
    private final Matrix4 projectionMatrix = new Matrix4();
    private final Matrix4 modelViewMatrix = new Matrix4();
    private boolean projectionMode; // true = projection, false = modelview
    private final Deque<Matrix4> projStack = new ArrayDeque<>();
    private final Deque<Matrix4> mvStack = new ArrayDeque<>();

    // State tracking
    private float clearR, clearG, clearB, clearA;
    private boolean fogEnabled;
    private float fogDensity;
    private final float[] fogColor = {0.5f, 0.8f, 1.0f, 1.0f};
    private float colorR = 1, colorG = 1, colorB = 1, colorA = 1;
    private boolean texturesEnabled;
    private boolean depthTestEnabled;
    private boolean blendEnabled;
    private int blendSrc, blendDst;

    // Compiled mesh storage (display list replacement)
    private int nextMeshId = 1;
    private final Map<Integer, Mesh> compiledMeshes = new HashMap<>();
    private int compilingId = -1;

    // Texture management
    private int nextTexId = 1;
    private final Map<Integer, Texture> textures = new HashMap<>();
    private final Map<String, Integer> textureCache = new HashMap<>();
    private int boundTexture;

    // Immediate mode state
    private boolean imHasTex;
    private float imTexU, imTexV;
    private boolean imIsLines;
    private final float[] imVertices = new float[9 * 4 * 10_000]; // pos+color+tex * 4 verts * 10K quads
    private int imVertexCount;

    // State save/restore stack
    private static class SavedState {
        boolean depthTest, blend, textures, fog;
        float colorR, colorG, colorB, colorA;
        float fogDensity;
        float[] fogColor;
        int blendSrc, blendDst;
        Matrix4 projection, modelView;
    }
    private final Deque<SavedState> stateStack = new ArrayDeque<>();

    public LibGDXGraphics() {
        ShaderProgram.pedantic = false;
        FileHandle vertFile = Gdx.files.classpath("shaders/block.vert");
        FileHandle fragFile = Gdx.files.classpath("shaders/block.frag");
        shader = new ShaderProgram(vertFile, fragFile);
        if (!shader.isCompiled()) {
            throw new RuntimeException("Shader compilation failed:\n" + shader.getLog());
        }
    }

    public void dispose() {
        if (shader != null) shader.dispose();
        compiledMeshes.values().forEach(Mesh::dispose);
        textures.values().forEach(Texture::dispose);
    }

    // ── Frame operations ───────────────────────────────────────────────

    @Override
    public void setClearColor(float r, float g, float b, float a) {
        clearR = r; clearG = g; clearB = b; clearA = a;
    }

    @Override
    public void setClearDepth(double depth) {
        // libGDX always clears depth to 1.0
    }

    @Override
    public void clear() {
        Gdx.gl.glClearColor(clearR, clearG, clearB, clearA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    // ── Matrix stack ───────────────────────────────────────────────────

    private Matrix4 currentMatrix() {
        return projectionMode ? projectionMatrix : modelViewMatrix;
    }

    private Deque<Matrix4> currentStack() {
        return projectionMode ? projStack : mvStack;
    }

    @Override
    public void matrixModeProjection() {
        projectionMode = true;
    }

    @Override
    public void matrixModeModelView() {
        projectionMode = false;
    }

    @Override
    public void pushMatrix() {
        currentStack().push(new Matrix4(currentMatrix()));
    }

    @Override
    public void popMatrix() {
        Matrix4 saved = currentStack().poll();
        if (saved != null) currentMatrix().set(saved);
    }

    @Override
    public void loadIdentity() {
        currentMatrix().idt();
    }

    @Override
    public void translate(float x, float y, float z) {
        currentMatrix().translate(x, y, z);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        currentMatrix().rotate(x, y, z, angle);
    }

    @Override
    public void scale(float x, float y, float z) {
        currentMatrix().scale(x, y, z);
    }

    @Override
    public void perspective(float fovYDeg, float aspect, float zNear, float zFar) {
        Matrix4 persp = new Matrix4();
        float f = (float) (1.0 / Math.tan(Math.toRadians(fovYDeg) / 2.0));
        float rangeRecip = 1.0f / (zNear - zFar);
        float[] v = persp.val;
        v[Matrix4.M00] = f / aspect;
        v[Matrix4.M11] = f;
        v[Matrix4.M22] = (zFar + zNear) * rangeRecip;
        v[Matrix4.M23] = 2 * zFar * zNear * rangeRecip;
        v[Matrix4.M32] = -1;
        v[Matrix4.M33] = 0;
        currentMatrix().mul(persp);
    }

    @Override
    public void ortho(double left, double right, double bottom, double top,
                      double near, double far) {
        Matrix4 o = new Matrix4();
        float[] v = o.val;
        float dx = (float) (right - left);
        float dy = (float) (top - bottom);
        float dz = (float) (far - near);
        v[Matrix4.M00] = 2.0f / dx;
        v[Matrix4.M11] = 2.0f / dy;
        v[Matrix4.M22] = -2.0f / dz;
        v[Matrix4.M03] = -(float) (right + left) / dx;
        v[Matrix4.M13] = -(float) (top + bottom) / dy;
        v[Matrix4.M23] = -(float) (far + near) / dz;
        v[Matrix4.M33] = 1;
        currentMatrix().mul(o);
    }

    @Override
    public void multMatrix(float[] m16) {
        Matrix4 m = new Matrix4(m16);
        currentMatrix().mul(m);
    }

    @Override
    public float[] getProjectionMatrix() {
        float[] result = new float[16];
        System.arraycopy(projectionMatrix.val, 0, result, 0, 16);
        return result;
    }

    @Override
    public float[] getModelViewMatrix() {
        float[] result = new float[16];
        System.arraycopy(modelViewMatrix.val, 0, result, 0, 16);
        return result;
    }

    @Override
    public int[] getViewport() {
        return new int[]{0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()};
    }

    // ── State save / restore ───────────────────────────────────────────

    @Override
    public void pushAllState() {
        SavedState s = new SavedState();
        s.depthTest = depthTestEnabled;
        s.blend = blendEnabled;
        s.textures = texturesEnabled;
        s.fog = fogEnabled;
        s.colorR = colorR; s.colorG = colorG; s.colorB = colorB; s.colorA = colorA;
        s.fogDensity = fogDensity;
        s.fogColor = fogColor.clone();
        s.blendSrc = blendSrc; s.blendDst = blendDst;
        s.projection = new Matrix4(projectionMatrix);
        s.modelView = new Matrix4(modelViewMatrix);
        stateStack.push(s);
    }

    @Override
    public void popAllState() {
        SavedState s = stateStack.poll();
        if (s == null) return;
        depthTestEnabled = s.depthTest;
        blendEnabled = s.blend;
        texturesEnabled = s.textures;
        fogEnabled = s.fog;
        colorR = s.colorR; colorG = s.colorG; colorB = s.colorB; colorA = s.colorA;
        fogDensity = s.fogDensity;
        System.arraycopy(s.fogColor, 0, fogColor, 0, 4);
        blendSrc = s.blendSrc; blendDst = s.blendDst;
        projectionMatrix.set(s.projection);
        modelViewMatrix.set(s.modelView);
        applyGlState();
    }

    private void applyGlState() {
        if (depthTestEnabled) {
            Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        } else {
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        }
        if (blendEnabled) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(blendSrc, blendDst);
        } else {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    // ── Render state ───────────────────────────────────────────────────

    @Override
    public void enableDepthTest() {
        depthTestEnabled = true;
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void disableDepthTest() {
        depthTestEnabled = false;
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    @Override
    public void depthFunc(DepthFunc func) {
        Gdx.gl.glDepthFunc(mapDepthFunc(func));
    }

    @Override
    public void enableCullFace() {
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
    }

    @Override
    public void disableCullFace() {
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
    }

    @Override
    public void enableBlend() {
        blendEnabled = true;
        Gdx.gl.glEnable(GL20.GL_BLEND);
    }

    @Override
    public void disableBlend() {
        blendEnabled = false;
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void blendFunc(BlendFactor src, BlendFactor dst) {
        blendSrc = mapBlend(src);
        blendDst = mapBlend(dst);
        Gdx.gl.glBlendFunc(blendSrc, blendDst);
    }

    @Override
    public void enableFog(float density, float r, float g, float b, float a) {
        fogEnabled = true;
        fogDensity = density;
        fogColor[0] = r; fogColor[1] = g; fogColor[2] = b; fogColor[3] = a;
    }

    @Override
    public void disableFog() {
        fogEnabled = false;
    }

    @Override
    public void enableTexture2D() {
        texturesEnabled = true;
    }

    @Override
    public void disableTexture2D() {
        texturesEnabled = false;
    }

    @Override
    public void shadeSmooth() {
        // Shader-based: smooth shading is default (interpolated varyings)
    }

    // ── Drawing state ──────────────────────────────────────────────────

    @Override
    public void setColor(float r, float g, float b, float a) {
        colorR = r; colorG = g; colorB = b; colorA = a;
    }

    @Override
    public void setLineWidth(float width) {
        Gdx.gl.glLineWidth(width);
    }

    // ── Textures ───────────────────────────────────────────────────────

    @Override
    public int loadTexture(String resource, com.github.martinambrus.rdforward.render.TextureFilter filter) {
        Integer cached = textureCache.get(resource);
        if (cached != null) return cached;

        Texture tex = new Texture(Gdx.files.classpath(resource.startsWith("/") ? resource.substring(1) : resource));
        Texture.TextureFilter gdxFilter = mapFilter(filter);
        tex.setFilter(gdxFilter, gdxFilter);
        int id = nextTexId++;
        textures.put(id, tex);
        textureCache.put(resource, id);
        return id;
    }

    @Override
    public int createTexture(int w, int h, ByteBuffer rgbaPixels,
                             com.github.martinambrus.rdforward.render.TextureFilter filter,
                             boolean clampEdge) {
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        // Copy pixel data into pixmap's buffer
        ByteBuffer pmBuf = pixmap.getPixels();
        rgbaPixels.rewind();
        pmBuf.put(rgbaPixels);
        pmBuf.flip();

        Texture tex = new Texture(pixmap);
        Texture.TextureFilter gdxFilter = mapFilter(filter);
        tex.setFilter(gdxFilter, gdxFilter);
        if (clampEdge) {
            tex.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        }
        pixmap.dispose();

        int id = nextTexId++;
        textures.put(id, tex);
        return id;
    }

    @Override
    public void bindTexture(int textureId) {
        boundTexture = textureId;
        Texture tex = textures.get(textureId);
        if (tex != null) tex.bind(0);
    }

    @Override
    public void deleteTexture(int textureId) {
        Texture tex = textures.remove(textureId);
        if (tex != null) tex.dispose();
    }

    // ── Mesh builder ───────────────────────────────────────────────────

    @Override
    public RDMeshBuilder meshBuilder() {
        return meshBuilder;
    }

    // ── Compiled meshes ────────────────────────────────────────────────

    @Override
    public int genCompiledMeshes(int count) {
        int first = nextMeshId;
        nextMeshId += count;
        return first;
    }

    @Override
    public void beginCompile(int meshId) {
        compilingId = meshId;
        meshBuilder.init();
    }

    @Override
    public void endCompile() {
        // Capture the mesh builder state into a libGDX Mesh (VBO)
        if (compilingId >= 0 && meshBuilder.vertexCount() > 0) {
            Mesh mesh = meshBuilder.buildMesh();
            Mesh old = compiledMeshes.put(compilingId, mesh);
            if (old != null) old.dispose();
        }
        compilingId = -1;
    }

    @Override
    public void renderCompiled(int meshId) {
        Mesh mesh = compiledMeshes.get(meshId);
        if (mesh == null) return;

        shader.bind();
        uploadUniforms();
        mesh.render(shader, GL20.GL_TRIANGLES);
    }

    // ── Picking ────────────────────────────────────────────────────────
    // On ES 2.0, GL_SELECT is not available. Picking is implemented via
    // ray casting at a higher level (in the game code / mixin).
    // These methods are stubs that do nothing — the actual picking
    // happens in software using the camera matrices.

    @Override
    public void beginPick(IntBuffer selectBuffer) {
        // No-op: ray casting used instead
    }

    @Override
    public int endPick() {
        return 0; // No hits via GL_SELECT
    }

    @Override
    public void initNames() { /* no-op */ }

    @Override
    public void pushName(int name) { /* no-op */ }

    @Override
    public void popName() { /* no-op */ }

    // ── Immediate mode ─────────────────────────────────────────────────
    // Emulates glBegin/glVertex/glEnd by batching into a temporary mesh
    // and rendering as GL_TRIANGLES (quads are split into 2 triangles).

    @Override
    public void beginQuads() {
        imVertexCount = 0;
        imHasTex = false;
        imIsLines = false;
    }

    @Override
    public void beginLines() {
        imVertexCount = 0;
        imHasTex = false;
        imIsLines = true;
    }

    @Override
    public void texCoord2f(float u, float v) {
        imTexU = u;
        imTexV = v;
        imHasTex = true;
    }

    @Override
    public void vertex2f(float x, float y) {
        vertex3f(x, y, 0);
    }

    @Override
    public void vertex3f(float x, float y, float z) {
        int base = imVertexCount * 9;
        if (base + 9 > imVertices.length) return; // safety
        imVertices[base]     = x;
        imVertices[base + 1] = y;
        imVertices[base + 2] = z;
        imVertices[base + 3] = colorR;
        imVertices[base + 4] = colorG;
        imVertices[base + 5] = colorB;
        imVertices[base + 6] = colorA;
        imVertices[base + 7] = imHasTex ? imTexU : 0;
        imVertices[base + 8] = imHasTex ? imTexV : 0;
        imVertexCount++;
    }

    @Override
    public void endDraw() {
        if (imVertexCount == 0) return;

        shader.bind();
        uploadUniforms();

        if (imIsLines) {
            renderImmediateLines();
        } else {
            renderImmediateQuads();
        }
    }

    private void renderImmediateQuads() {
        // Convert quads (4 verts each) to triangles (6 verts each = 2 triangles)
        int quadCount = imVertexCount / 4;
        int triVertCount = quadCount * 6;
        float[] triVerts = new float[triVertCount * 9];

        for (int q = 0; q < quadCount; q++) {
            int src = q * 4 * 9;
            int dst = q * 6 * 9;
            // Triangle 1: v0, v1, v2
            System.arraycopy(imVertices, src, triVerts, dst, 9);
            System.arraycopy(imVertices, src + 9, triVerts, dst + 9, 9);
            System.arraycopy(imVertices, src + 18, triVerts, dst + 18, 9);
            // Triangle 2: v0, v2, v3
            System.arraycopy(imVertices, src, triVerts, dst + 27, 9);
            System.arraycopy(imVertices, src + 18, triVerts, dst + 36, 9);
            System.arraycopy(imVertices, src + 27, triVerts, dst + 45, 9);
        }

        Mesh mesh = new Mesh(false, triVertCount, 0,
                new VertexAttribute(Usage.Position, 3, "a_position"),
                new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
                new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
        mesh.setVertices(triVerts, 0, triVertCount * 9);
        mesh.render(shader, GL20.GL_TRIANGLES);
        mesh.dispose();
    }

    private void renderImmediateLines() {
        Mesh mesh = new Mesh(false, imVertexCount, 0,
                new VertexAttribute(Usage.Position, 3, "a_position"),
                new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
                new VertexAttribute(Usage.TextureCoordinates, 2, "a_texCoord0"));
        mesh.setVertices(imVertices, 0, imVertexCount * 9);
        mesh.render(shader, GL20.GL_LINES);
        mesh.dispose();
    }

    // ── Shader uniform upload ──────────────────────────────────────────

    private void uploadUniforms() {
        Matrix4 combined = new Matrix4(projectionMatrix).mul(modelViewMatrix);
        shader.setUniformMatrix("u_projViewMatrix", combined);
        shader.setUniformi("u_hasTexture", texturesEnabled ? 1 : 0);
        shader.setUniformi("u_hasColor", 1);
        shader.setUniform4fv("u_color", new float[]{colorR, colorG, colorB, colorA}, 0, 4);
        shader.setUniformi("u_fogEnabled", fogEnabled ? 1 : 0);
        if (fogEnabled) {
            shader.setUniformf("u_fogDensity", fogDensity);
            shader.setUniform4fv("u_fogColor", fogColor, 0, 4);
        }
    }

    // ── Enum → GL constant mapping ─────────────────────────────────────

    private static int mapDepthFunc(DepthFunc func) {
        return switch (func) {
            case NEVER -> GL20.GL_NEVER;
            case LESS -> GL20.GL_LESS;
            case EQUAL -> GL20.GL_EQUAL;
            case LEQUAL -> GL20.GL_LEQUAL;
            case GREATER -> GL20.GL_GREATER;
            case NOTEQUAL -> GL20.GL_NOTEQUAL;
            case GEQUAL -> GL20.GL_GEQUAL;
            case ALWAYS -> GL20.GL_ALWAYS;
        };
    }

    private static int mapBlend(BlendFactor factor) {
        return switch (factor) {
            case ZERO -> GL20.GL_ZERO;
            case ONE -> GL20.GL_ONE;
            case SRC_ALPHA -> GL20.GL_SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA -> GL20.GL_ONE_MINUS_SRC_ALPHA;
            case DST_ALPHA -> GL20.GL_DST_ALPHA;
            case ONE_MINUS_DST_ALPHA -> GL20.GL_ONE_MINUS_DST_ALPHA;
        };
    }

    private static Texture.TextureFilter mapFilter(
            com.github.martinambrus.rdforward.render.TextureFilter filter) {
        return switch (filter) {
            case NEAREST -> Texture.TextureFilter.Nearest;
            case LINEAR -> Texture.TextureFilter.Linear;
            case NEAREST_MIPMAP_NEAREST -> Texture.TextureFilter.MipMapNearestNearest;
            case LINEAR_MIPMAP_LINEAR -> Texture.TextureFilter.MipMapLinearLinear;
        };
    }
}
