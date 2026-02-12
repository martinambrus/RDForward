package com.github.martinambrus.rdforward.render.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.github.martinambrus.rdforward.render.RDInput;

/**
 * libGDX input implementation. Translates libGDX key codes to GLFW-compatible
 * codes for compatibility with the existing game code.
 */
public class LibGDXInput implements RDInput {

    private volatile float mouseDX;
    private volatile float mouseDY;
    private boolean mouseGrabbed;

    private CharCallback charCallback;
    private KeyCallback keyCallback;

    public LibGDXInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                KeyCallback cb = keyCallback;
                if (cb != null) cb.onKey(gdxToGlfw(keycode), 1, 0);
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                KeyCallback cb = keyCallback;
                if (cb != null) cb.onKey(gdxToGlfw(keycode), 0, 0);
                return true;
            }

            @Override
            public boolean keyTyped(char character) {
                CharCallback cb = charCallback;
                if (cb != null) cb.onChar(character);
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseDX += Gdx.input.getDeltaX();
                mouseDY += Gdx.input.getDeltaY();
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                mouseDX += Gdx.input.getDeltaX();
                mouseDY += Gdx.input.getDeltaY();
                return true;
            }
        });
    }

    @Override
    public boolean isKeyDown(int keyCode) {
        // keyCode is GLFW — translate to libGDX
        int gdxKey = glfwToGdx(keyCode);
        return gdxKey != -1 && Gdx.input.isKeyPressed(gdxKey);
    }

    @Override
    public void setCharCallback(CharCallback callback) {
        this.charCallback = callback;
    }

    @Override
    public void setKeyCallback(KeyCallback callback) {
        this.keyCallback = callback;
    }

    @Override
    public float consumeMouseDX() {
        float dx = mouseDX;
        mouseDX = 0;
        return dx;
    }

    @Override
    public float consumeMouseDY() {
        float dy = mouseDY;
        mouseDY = 0;
        return dy;
    }

    @Override
    public boolean isMouseButtonDown(int button) {
        return Gdx.input.isButtonPressed(button);
    }

    @Override
    public void grabMouse() {
        Gdx.input.setCursorCatched(true);
        mouseGrabbed = true;
    }

    @Override
    public void releaseMouse() {
        Gdx.input.setCursorCatched(false);
        mouseGrabbed = false;
    }

    @Override
    public boolean isMouseGrabbed() {
        return mouseGrabbed;
    }

    @Override
    public int getScreenWidth() {
        return Gdx.graphics.getWidth();
    }

    @Override
    public int getScreenHeight() {
        return Gdx.graphics.getHeight();
    }

    // ── Key code translation ───────────────────────────────────────────
    // Maps between GLFW key codes (used by game code) and libGDX key codes.
    // Only the keys actually used by RubyDung are mapped.

    private static int glfwToGdx(int glfw) {
        return switch (glfw) {
            case 87  -> Keys.W;           // GLFW_KEY_W
            case 65  -> Keys.A;           // GLFW_KEY_A
            case 83  -> Keys.S;           // GLFW_KEY_S
            case 68  -> Keys.D;           // GLFW_KEY_D
            case 32  -> Keys.SPACE;       // GLFW_KEY_SPACE
            case 82  -> Keys.R;           // GLFW_KEY_R
            case 84  -> Keys.T;           // GLFW_KEY_T
            case 256 -> Keys.ESCAPE;      // GLFW_KEY_ESCAPE
            case 257 -> Keys.ENTER;       // GLFW_KEY_ENTER
            case 259 -> Keys.BACKSPACE;   // GLFW_KEY_BACKSPACE
            case 265 -> Keys.UP;          // GLFW_KEY_UP
            case 264 -> Keys.DOWN;        // GLFW_KEY_DOWN
            case 263 -> Keys.LEFT;        // GLFW_KEY_LEFT
            case 262 -> Keys.RIGHT;       // GLFW_KEY_RIGHT
            case 295 -> Keys.F6;          // GLFW_KEY_F6
            case 341 -> Keys.CONTROL_LEFT;  // GLFW_KEY_LEFT_CONTROL
            case 77  -> Keys.M;           // GLFW_KEY_M
            case 343 -> Keys.SYM;         // GLFW_KEY_LEFT_SUPER (approx)
            default -> -1;
        };
    }

    private static int gdxToGlfw(int gdx) {
        return switch (gdx) {
            case Keys.W -> 87;
            case Keys.A -> 65;
            case Keys.S -> 83;
            case Keys.D -> 68;
            case Keys.SPACE -> 32;
            case Keys.R -> 82;
            case Keys.T -> 84;
            case Keys.ESCAPE -> 256;
            case Keys.ENTER -> 257;
            case Keys.BACKSPACE -> 259;
            case Keys.UP -> 265;
            case Keys.DOWN -> 264;
            case Keys.LEFT -> 263;
            case Keys.RIGHT -> 262;
            case Keys.F6 -> 295;
            case Keys.CONTROL_LEFT -> 341;
            case Keys.M -> 77;
            default -> gdx; // pass through unmapped
        };
    }
}
