package com.github.martinambrus.rdforward.e2e.agent;

import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import javax.imageio.ImageIO;

/**
 * Captures screenshots from the Minecraft client's OpenGL context.
 * Uses reflection to call LWJGL 2's GL11.glReadPixels since the agent
 * runs on the client's classpath where LWJGL 2 classes are available.
 *
 * MUST be called on the render thread (from within the tick hook).
 */
public class ScreenshotCapture {

    private Class<?> gl11Class;
    private Method glReadPixelsMethod;
    private boolean initialized;

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
        } catch (Exception e) {
            System.err.println("[McTestAgent] Screenshot capture failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void init() throws Exception {
        // LWJGL 2 GL11 class is on the client classpath
        gl11Class = Class.forName("org.lwjgl.opengl.GL11");
        // void glReadPixels(int x, int y, int width, int height, int format, int type, ByteBuffer pixels)
        glReadPixelsMethod = gl11Class.getMethod("glReadPixels",
                int.class, int.class, int.class, int.class,
                int.class, int.class, ByteBuffer.class);
        initialized = true;
    }
}
