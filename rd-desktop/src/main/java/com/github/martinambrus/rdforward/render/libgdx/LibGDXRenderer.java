package com.github.martinambrus.rdforward.render.libgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.martinambrus.rdforward.render.RDGraphics;
import com.github.martinambrus.rdforward.render.RDInput;
import com.github.martinambrus.rdforward.render.RDRenderer;

/**
 * libGDX-based renderer that uses OpenGL ES 2.0 compatible shaders.
 * Works on both desktop (via LWJGL 3) and Android.
 * <p>
 * This is a bridge: libGDX manages the window lifecycle via
 * {@link ApplicationAdapter}, while the game code drives frames via
 * the {@link RDRenderer} interface.
 */
public class LibGDXRenderer implements RDRenderer {

    private int width;
    private int height;
    private volatile boolean closeRequested;

    private LibGDXGraphics graphics;
    private LibGDXInput input;
    private Lwjgl3Application application;

    /**
     * The libGDX ApplicationAdapter that delegates to this renderer.
     * The game loop is external (driven by the game code), so
     * {@code render()} just marks a frame boundary.
     */
    private final ApplicationAdapter adapter = new ApplicationAdapter() {
        @Override
        public void create() {
            graphics = new LibGDXGraphics();
            input = new LibGDXInput();
        }

        @Override
        public void resize(int w, int h) {
            width = w;
            height = h;
        }

        @Override
        public void dispose() {
            if (graphics != null) graphics.dispose();
        }
    };

    @Override
    public void init(int width, int height, String title) {
        this.width = width;
        this.height = height;

        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle(title);
        config.setWindowedMode(width, height);
        config.useVsync(true);
        config.setResizable(false);

        // Launch on a background thread so init() returns immediately
        // (the game loop drives rendering, not libGDX's internal loop)
        Thread gdxThread = new Thread(() -> {
            application = new Lwjgl3Application(adapter, config);
        }, "libGDX-main");
        gdxThread.setDaemon(true);
        gdxThread.start();

        // Wait for the GL context to be ready
        while (graphics == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public boolean shouldClose() {
        return closeRequested;
    }

    @Override
    public void pollEvents() {
        // libGDX handles event polling internally
    }

    @Override
    public void swapBuffers() {
        // libGDX handles buffer swapping internally
    }

    @Override
    public void dispose() {
        closeRequested = true;
        if (application != null) {
            Gdx.app.exit();
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public long getWindowHandle() {
        // libGDX abstracts the window handle away
        return 0;
    }

    @Override
    public RDGraphics graphics() {
        return graphics;
    }

    @Override
    public RDInput input() {
        return input;
    }
}
