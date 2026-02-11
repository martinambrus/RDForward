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

        // The game tick and render loop runs here. On Android, libGDX
        // drives the loop at ~60 FPS. The game code is called per frame.
        touchInput.update();
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
