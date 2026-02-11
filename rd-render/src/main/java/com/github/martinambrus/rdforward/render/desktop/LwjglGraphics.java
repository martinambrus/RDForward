package com.github.martinambrus.rdforward.render.desktop;

import com.github.martinambrus.rdforward.render.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * OpenGL 1.x fixed-function implementation of {@link RDGraphics}.
 * Wraps the same GL calls the original RubyDung code made.
 */
public class LwjglGraphics implements RDGraphics {

    private final LwjglMeshBuilder meshBuilder = new LwjglMeshBuilder();
    private final Map<String, Integer> textureCache = new HashMap<>();

    // Reusable buffers
    private final FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
    private final IntBuffer vpBuf = BufferUtils.createIntBuffer(4);

    // Immediate-mode state
    private float imTexU, imTexV;
    private boolean imHasTex;

    // ── Frame operations ───────────────────────────────────────────────

    @Override
    public void setClearColor(float r, float g, float b, float a) {
        GL11.glClearColor(r, g, b, a);
    }

    @Override
    public void setClearDepth(double depth) {
        GL11.glClearDepth(depth);
    }

    @Override
    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    // ── Matrix stack ───────────────────────────────────────────────────

    @Override
    public void matrixModeProjection() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
    }

    @Override
    public void matrixModeModelView() {
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void pushMatrix() {
        GL11.glPushMatrix();
    }

    @Override
    public void popMatrix() {
        GL11.glPopMatrix();
    }

    @Override
    public void loadIdentity() {
        GL11.glLoadIdentity();
    }

    @Override
    public void translate(float x, float y, float z) {
        GL11.glTranslatef(x, y, z);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        GL11.glRotatef(angle, x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        GL11.glScalef(x, y, z);
    }

    @Override
    public void perspective(float fovYDeg, float aspect, float zNear, float zFar) {
        float f = (float) (1.0 / Math.tan(Math.toRadians(fovYDeg) / 2.0));
        float rangeReciprocal = 1.0f / (zNear - zFar);
        FloatBuffer m = BufferUtils.createFloatBuffer(16);
        m.put(new float[]{
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (zFar + zNear) * rangeReciprocal, -1,
                0, 0, 2 * zFar * zNear * rangeReciprocal, 0
        });
        m.flip();
        GL11.glMultMatrixf(m);
    }

    @Override
    public void ortho(double left, double right, double bottom, double top,
                      double near, double far) {
        GL11.glOrtho(left, right, bottom, top, near, far);
    }

    @Override
    public void multMatrix(float[] m16) {
        matBuf.clear();
        matBuf.put(m16);
        matBuf.flip();
        GL11.glMultMatrixf(matBuf);
    }

    @Override
    public float[] getProjectionMatrix() {
        matBuf.clear();
        GL11.glGetFloatv(GL11.GL_PROJECTION_MATRIX, matBuf);
        float[] result = new float[16];
        matBuf.get(result);
        return result;
    }

    @Override
    public float[] getModelViewMatrix() {
        matBuf.clear();
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, matBuf);
        float[] result = new float[16];
        matBuf.get(result);
        return result;
    }

    @Override
    public int[] getViewport() {
        vpBuf.clear();
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, vpBuf);
        int[] result = new int[4];
        vpBuf.get(result);
        return result;
    }

    // ── State save / restore ───────────────────────────────────────────

    @Override
    public void pushAllState() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
    }

    @Override
    public void popAllState() {
        GL11.glPopAttrib();
    }

    // ── Render state ───────────────────────────────────────────────────

    @Override
    public void enableDepthTest() {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void disableDepthTest() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void depthFunc(DepthFunc func) {
        GL11.glDepthFunc(mapDepthFunc(func));
    }

    @Override
    public void enableCullFace() {
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    @Override
    public void disableCullFace() {
        GL11.glDisable(GL11.GL_CULL_FACE);
    }

    @Override
    public void enableBlend() {
        GL11.glEnable(GL11.GL_BLEND);
    }

    @Override
    public void disableBlend() {
        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void blendFunc(BlendFactor src, BlendFactor dst) {
        GL11.glBlendFunc(mapBlend(src), mapBlend(dst));
    }

    @Override
    public void enableFog(float density, float r, float g, float b, float a) {
        GL11.glEnable(GL11.GL_FOG);
        GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP2);
        GL11.glFogf(GL11.GL_FOG_DENSITY, density);
        FloatBuffer col = BufferUtils.createFloatBuffer(4);
        col.put(new float[]{r, g, b, a}).flip();
        GL11.glFogfv(GL11.GL_FOG_COLOR, col);
    }

    @Override
    public void disableFog() {
        GL11.glDisable(GL11.GL_FOG);
    }

    @Override
    public void enableTexture2D() {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void disableTexture2D() {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    @Override
    public void shadeSmooth() {
        GL11.glShadeModel(GL11.GL_SMOOTH);
    }

    // ── Drawing state ──────────────────────────────────────────────────

    @Override
    public void setColor(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }

    @Override
    public void setLineWidth(float width) {
        GL11.glLineWidth(width);
    }

    // ── Textures ───────────────────────────────────────────────────────

    @Override
    public int loadTexture(String resource, TextureFilter filter) {
        Integer cached = textureCache.get(resource);
        if (cached != null) return cached;

        try {
            InputStream is = LwjglGraphics.class.getResourceAsStream(resource);
            if (is == null) throw new IOException("Resource not found: " + resource);
            BufferedImage img = ImageIO.read(is);
            int w = img.getWidth();
            int h = img.getHeight();

            ByteBuffer pixels = BufferUtils.createByteBuffer(w * h * 4);
            int[] raw = new int[w * h];
            img.getRGB(0, 0, w, h, raw, 0, w);
            // ARGB → RGBA
            for (int i = 0; i < raw.length; i++) {
                int a = (raw[i] >> 24) & 0xFF;
                int r = (raw[i] >> 16) & 0xFF;
                int g = (raw[i] >> 8) & 0xFF;
                int b = raw[i] & 0xFF;
                pixels.put((byte) r).put((byte) g).put((byte) b).put((byte) a);
            }
            pixels.flip();

            int id = createTexture(w, h, pixels, filter, false);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
            textureCache.put(resource, id);
            return id;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + resource, e);
        }
    }

    @Override
    public int createTexture(int w, int h, ByteBuffer rgbaPixels,
                             TextureFilter filter, boolean clampEdge) {
        int id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        int glFilter = mapFilter(filter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, glFilter);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, glFilter);
        if (clampEdge) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        }
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0,
                GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, rgbaPixels);
        return id;
    }

    @Override
    public void bindTexture(int textureId) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
    }

    @Override
    public void deleteTexture(int textureId) {
        GL11.glDeleteTextures(textureId);
    }

    // ── Mesh builder ───────────────────────────────────────────────────

    @Override
    public RDMeshBuilder meshBuilder() {
        return meshBuilder;
    }

    // ── Compiled meshes ────────────────────────────────────────────────

    @Override
    public int genCompiledMeshes(int count) {
        return GL11.glGenLists(count);
    }

    @Override
    public void beginCompile(int meshId) {
        GL11.glNewList(meshId, GL11.GL_COMPILE);
    }

    @Override
    public void endCompile() {
        GL11.glEndList();
    }

    @Override
    public void renderCompiled(int meshId) {
        GL11.glCallList(meshId);
    }

    // ── Picking ────────────────────────────────────────────────────────

    @Override
    public void beginPick(IntBuffer selectBuffer) {
        selectBuffer.clear();
        GL11.glSelectBuffer(selectBuffer);
        GL11.glRenderMode(GL11.GL_SELECT);
    }

    @Override
    public int endPick() {
        return GL11.glRenderMode(GL11.GL_RENDER);
    }

    @Override
    public void initNames() {
        GL11.glInitNames();
    }

    @Override
    public void pushName(int name) {
        GL11.glPushName(name);
    }

    @Override
    public void popName() {
        GL11.glPopName();
    }

    // ── Immediate mode ─────────────────────────────────────────────────

    @Override
    public void beginQuads() {
        GL11.glBegin(GL11.GL_QUADS);
        imHasTex = false;
    }

    @Override
    public void beginLines() {
        GL11.glBegin(GL11.GL_LINES);
        imHasTex = false;
    }

    @Override
    public void texCoord2f(float u, float v) {
        imTexU = u;
        imTexV = v;
        imHasTex = true;
    }

    @Override
    public void vertex2f(float x, float y) {
        if (imHasTex) GL11.glTexCoord2f(imTexU, imTexV);
        GL11.glVertex2f(x, y);
    }

    @Override
    public void vertex3f(float x, float y, float z) {
        if (imHasTex) GL11.glTexCoord2f(imTexU, imTexV);
        GL11.glVertex3f(x, y, z);
    }

    @Override
    public void endDraw() {
        GL11.glEnd();
    }

    // ── Enum → GL constant mapping ─────────────────────────────────────

    private static int mapDepthFunc(DepthFunc func) {
        return switch (func) {
            case NEVER -> GL11.GL_NEVER;
            case LESS -> GL11.GL_LESS;
            case EQUAL -> GL11.GL_EQUAL;
            case LEQUAL -> GL11.GL_LEQUAL;
            case GREATER -> GL11.GL_GREATER;
            case NOTEQUAL -> GL11.GL_NOTEQUAL;
            case GEQUAL -> GL11.GL_GEQUAL;
            case ALWAYS -> GL11.GL_ALWAYS;
        };
    }

    private static int mapBlend(BlendFactor factor) {
        return switch (factor) {
            case ZERO -> GL11.GL_ZERO;
            case ONE -> GL11.GL_ONE;
            case SRC_ALPHA -> GL11.GL_SRC_ALPHA;
            case ONE_MINUS_SRC_ALPHA -> GL11.GL_ONE_MINUS_SRC_ALPHA;
            case DST_ALPHA -> GL11.GL_DST_ALPHA;
            case ONE_MINUS_DST_ALPHA -> GL11.GL_ONE_MINUS_DST_ALPHA;
        };
    }

    private static int mapFilter(TextureFilter filter) {
        return switch (filter) {
            case NEAREST -> GL11.GL_NEAREST;
            case LINEAR -> GL11.GL_LINEAR;
            case NEAREST_MIPMAP_NEAREST -> GL11.GL_NEAREST_MIPMAP_NEAREST;
            case LINEAR_MIPMAP_LINEAR -> GL11.GL_LINEAR_MIPMAP_LINEAR;
        };
    }
}
