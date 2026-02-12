package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.github.martinambrus.rdforward.render.libgdx.LibGDXGraphics;

/**
 * libGDX ApplicationAdapter that bridges the Android lifecycle with the
 * RDForward rendering pipeline. The game loop, world state, and networking
 * are managed separately — this adapter only handles GL lifecycle events.
 */
public class RDForwardGameAdapter extends ApplicationAdapter {

    private final AndroidLauncher launcher;
    private LibGDXGraphics graphics;
    private TouchInputAdapter touchInput;
    private boolean initialized;

    public RDForwardGameAdapter(AndroidLauncher launcher) {
        this.launcher = launcher;
    }

    @Override
    public void create() {
        graphics = new LibGDXGraphics();
        touchInput = new TouchInputAdapter();
        Gdx.input.setInputProcessor(touchInput);
        initialized = true;
    }

    @Override
    public void render() {
        if (!initialized) return;

        touchInput.update();

        // Minimal test render: clear to cornflower blue to prove the GL
        // pipeline is alive.  Replace this with the real game loop once
        // RubyDung is ported to the RDGraphics abstraction.
        graphics.setClearColor(0.4f, 0.6f, 0.9f, 1.0f);
        graphics.clear();
    }

    @Override
    public void resize(int width, int height) {
        // Update viewport when screen size changes (rotation, etc.)
    }

    @Override
    public void pause() {
        // Android lifecycle: save state, pause networking
    }

    @Override
    public void resume() {
        // Android lifecycle: restore state, resume networking
    }

    @Override
    public void dispose() {
        if (graphics != null) graphics.dispose();
    }

    public LibGDXGraphics getGraphics() {
        return graphics;
    }

    public TouchInputAdapter getTouchInput() {
        return touchInput;
    }
}
