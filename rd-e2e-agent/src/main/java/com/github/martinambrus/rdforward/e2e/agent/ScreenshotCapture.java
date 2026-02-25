package com.github.martinambrus.rdforward.e2e.agent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

/**
 * Captures screenshots from the Minecraft client's OpenGL context.
 * Uses reflection to call GL11.glReadPixels. For Fabric/LWJGL 3 clients,
 * GL11 must be loaded from the game's classloader (KnotClassLoader) rather
 * than the system classloader, because GL capabilities are stored per-class
 * in ThreadLocal state — a GL11 loaded by a different classloader has no
 * context.
 *
 * MUST be called on the render thread (from within the tick hook).
 */
public class ScreenshotCapture {

    private Class<?> gl11Class;
    private Method glReadPixelsMethod;
    private boolean initialized;
    private ClassLoader gameClassLoader;
    private InputController inputController;

    /**
     * Set the InputController for pre-capture cleanup (e.g., clearing RubyDung
     * hitResult highlight). Uses a direct reference instead of a Runnable/lambda
     * because this is called from ByteBuddy Advice code that gets inlined into
     * the target class — lambdas compile to private synthetic methods that cause
     * IllegalAccessError across classloaders (KnotClassLoader vs app).
     */
    public void setInputController(InputController ic) {
        this.inputController = ic;
    }

    /**
     * Capture the current OpenGL framebuffer as a PNG file.
     *
     * @param width    display width in pixels
     * @param height   display height in pixels
     * @param destFile output PNG file
     * @return true if capture succeeded
     */
    public boolean capture(int width, int height, File destFile) {
        try {
            if (inputController != null) {
                inputController.clearHitResult();
            }
            if (!initialized) {
                init();
            }

            // GL11.glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
            int GL_RGBA = 0x1908;
            int GL_UNSIGNED_BYTE = 0x1401;
            ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);

            glReadPixelsMethod.invoke(null,
                    0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            // Convert to BufferedImage (flip vertically — OpenGL origin is bottom-left)
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    // Read from flipped position (bottom row first)
                    int srcIdx = ((height - 1 - y) * width + x) * 4;
                    int r = buffer.get(srcIdx) & 0xFF;
                    int g = buffer.get(srcIdx + 1) & 0xFF;
                    int b = buffer.get(srcIdx + 2) & 0xFF;
                    image.setRGB(x, y, (r << 16) | (g << 8) | b);
                }
            }

            destFile.getParentFile().mkdirs();
            ImageIO.write(image, "PNG", destFile);
            System.out.println("[McTestAgent] Screenshot saved: " + destFile.getAbsolutePath());
            return true;
        } catch (Throwable e) {
            System.err.println("[McTestAgent] Screenshot capture failed: " + e.getClass().getName()
                    + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Set the classloader to use for loading GL classes. Must be the game's
     * classloader (e.g. Fabric KnotClassLoader) so we get the same GL11 class
     * that holds the active GL capabilities.
     */
    public void setClassLoader(ClassLoader cl) {
        this.gameClassLoader = cl;
    }

    private void init() throws Exception {
        // Use game classloader if available (needed for Fabric/LWJGL 3 where
        // GL capabilities are per-class-instance ThreadLocal state), otherwise
        // fall back to thread context classloader, then system classloader.
        ClassLoader cl = gameClassLoader;
        if (cl == null) cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = ClassLoader.getSystemClassLoader();

        gl11Class = Class.forName("org.lwjgl.opengl.GL11", true, cl);
        // void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels)
        glReadPixelsMethod = gl11Class.getMethod("glReadPixels",
                int.class, int.class, int.class, int.class,
                int.class, int.class, ByteBuffer.class);
        initialized = true;
    }
}
