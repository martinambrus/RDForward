package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.github.martinambrus.rdforward.render.RDInput;

/**
 * Maps Android touch input to the keyboard/mouse interface expected by
 * the game code.
 * <p>
 * Touch mapping:
 * <ul>
 *   <li>Right-side drag → camera look (only starts after finger moves past drag threshold)</li>
 *   <li>Left-side drag → movement (WASD)</li>
 *   <li>Tap (short, still touch on right side) → place block (button 0)</li>
 *   <li>Hold (still touch held ≥ 500 ms) → continuously destroy blocks (button 1);
 *       once active, continues even while dragging to look</li>
 * </ul>
 * Movement and block interaction work simultaneously via multi-touch.
 */
public class TouchInputAdapter extends InputAdapter implements RDInput {

    private static final String TAG = "TouchInput";

    // Virtual key states (set by touch zones)
    private boolean keyW, keyA, keyS, keyD;

    // Physical keyboard edge detection (event-based for one-shot keys)
    private boolean ctrlDown = false;
    private boolean f6JustPressed = false;

    // Robust physical keyboard state tracking.
    // On Android, keyDown + keyUp can arrive in the same frame batch, causing
    // Gdx.input.isKeyPressed() to return false by the time Player.tick() polls.
    // We detect this "suspiciously fast release" and bridge the gap until
    // Android's key-repeat events start (~500 ms).  Genuine hold-then-release
    // (where the key was held for a real duration) is honoured immediately.
    private final boolean[] keyHeld = new boolean[256]; // indexed by libGDX keycode
    private final long[] keyDownTime = new long[256];
    private final long[] keyUpTime = new long[256];
    private static final long BATCH_THRESHOLD_MS = 80;  // keyUp within this of keyDown = same-frame batch
    private static final long KEY_REPEAT_BRIDGE_MS = 600; // keep "pressed" until key-repeat kicks in

    // Camera look accumulation
    private float lookDX, lookDY;

    // Touch state tracking
    private int moveTouchId = -1;
    private float moveStartX, moveStartY;
    private int lookTouchId = -1;
    private float lookLastX, lookLastY;

    // Screen dimensions for zone calculation
    private static final float MOVE_ZONE_FRACTION = 0.35f; // left 35% of screen
    private static final float MOVE_DEADZONE = 20f; // pixels
    private static final float LOOK_SENSITIVITY = 0.3f;

    // Block interaction state
    private boolean tapDetected;
    private boolean longPressDetected;
    private long touchDownTime;
    private float lookDragDist; // accumulated drag distance in pixels
    private boolean isLookDrag; // true once drag exceeds threshold
    private boolean holdDestroyActive; // latched true once hold-to-destroy fires, until touchUp
    private long lastDestroyTime;

    // Thresholds
    private static final float DRAG_THRESHOLD = 40f; // px — beyond this it's a look, not a tap
    private static final long LONG_PRESS_MS = 500;
    private static final long DESTROY_INTERVAL_MS = 250; // continuous destroy rate

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float screenWidth = Gdx.graphics.getWidth();
        float zoneWidth = screenWidth * MOVE_ZONE_FRACTION;

        Gdx.app.log(TAG, "touchDown ptr=" + pointer + " btn=" + button
                + " x=" + screenX + " y=" + screenY
                + " zoneW=" + (int) zoneWidth + " screenW=" + (int) screenWidth
                + " moveTouchId=" + moveTouchId + " lookTouchId=" + lookTouchId);

        if (screenX < zoneWidth && moveTouchId == -1) {
            // Left zone: movement
            moveTouchId = pointer;
            moveStartX = screenX;
            moveStartY = screenY;
            Gdx.app.log(TAG, "  → assigned to MOVE zone");
        } else if (lookTouchId == -1) {
            // Right zone: camera look / block interaction
            lookTouchId = pointer;
            lookLastX = screenX;
            lookLastY = screenY;
            touchDownTime = System.currentTimeMillis();
            lookDragDist = 0;
            isLookDrag = false;
            holdDestroyActive = false;
            Gdx.app.log(TAG, "  → assigned to LOOK zone");
        } else {
            Gdx.app.log(TAG, "  → IGNORED (both zones occupied)");
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        Gdx.app.log(TAG, "touchUp ptr=" + pointer + " btn=" + button
                + " moveTouchId=" + moveTouchId + " lookTouchId=" + lookTouchId
                + " isLookDrag=" + isLookDrag + " holdDestroy=" + holdDestroyActive
                + " dragDist=" + (int) lookDragDist);

        if (pointer == moveTouchId) {
            moveTouchId = -1;
            keyW = keyA = keyS = keyD = false;
            Gdx.app.log(TAG, "  → released MOVE zone");
        }
        if (pointer == lookTouchId) {
            long now = System.currentTimeMillis();
            long elapsed = now - touchDownTime;
            // Only fire a tap if the finger stayed still (not a look drag,
            // not already in hold-destroy mode) and was quick enough.
            if (!isLookDrag && !holdDestroyActive && elapsed < LONG_PRESS_MS) {
                tapDetected = true;
                Gdx.app.log(TAG, "  → TAP detected (elapsed=" + elapsed + "ms)");
            } else {
                Gdx.app.log(TAG, "  → no tap: isLookDrag=" + isLookDrag
                        + " holdDestroy=" + holdDestroyActive + " elapsed=" + elapsed);
            }
            lookTouchId = -1;
            isLookDrag = false;
            holdDestroyActive = false;
        }
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == moveTouchId) {
            float dx = screenX - moveStartX;
            float dy = screenY - moveStartY;
            keyW = dy < -MOVE_DEADZONE;
            keyS = dy > MOVE_DEADZONE;
            keyA = dx < -MOVE_DEADZONE;
            keyD = dx > MOVE_DEADZONE;
        }
        if (pointer == lookTouchId) {
            float dx = screenX - lookLastX;
            float dy = screenY - lookLastY;
            lookDragDist += (float) Math.sqrt(dx * dx + dy * dy);

            if (!isLookDrag && lookDragDist >= DRAG_THRESHOLD) {
                isLookDrag = true;
                Gdx.app.log(TAG, "touchDragged → isLookDrag activated (dist=" + (int) lookDragDist + ")");
            }

            // Rotate camera when:
            // - this is a confirmed look drag, OR
            // - hold-to-destroy is active (user wants to look while destroying)
            if (isLookDrag || holdDestroyActive) {
                lookDX += dx * LOOK_SENSITIVITY;
                lookDY += dy * LOOK_SENSITIVITY;
            }

            lookLastX = screenX;
            lookLastY = screenY;
        }
        return true;
    }

    // ── Physical keyboard support ────────────────────────────────────

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) ctrlDown = true;
        if (keycode == Input.Keys.Q && ctrlDown) { Gdx.app.exit(); return true; }
        if (keycode == Input.Keys.F6) { f6JustPressed = true; return true; }
        // Track for robust held-key detection
        if (keycode >= 0 && keycode < keyHeld.length) {
            keyHeld[keycode] = true;
            keyDownTime[keycode] = System.currentTimeMillis();
            Gdx.app.log(TAG, "keyDown code=" + keycode + " t=" + keyDownTime[keycode]);
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.CONTROL_LEFT || keycode == Input.Keys.CONTROL_RIGHT) ctrlDown = false;
        if (keycode >= 0 && keycode < keyHeld.length) {
            keyHeld[keycode] = false;
            keyUpTime[keycode] = System.currentTimeMillis();
            long held = keyUpTime[keycode] - keyDownTime[keycode];
            Gdx.app.log(TAG, "keyUp   code=" + keycode + " t=" + keyUpTime[keycode]
                    + " heldFor=" + held + "ms");
        }
        return true;
    }

    /** Consume F6 press event (returns true once, then resets). */
    public boolean consumeF6() {
        boolean was = f6JustPressed;
        f6JustPressed = false;
        return was;
    }

    /** Translate GLFW key codes (used by game code) to libGDX key codes (for polling). */
    private static int toGdxKey(int glfwKey) {
        return switch (glfwKey) {
            case 87 -> Input.Keys.W;
            case 65 -> Input.Keys.A;
            case 83 -> Input.Keys.S;
            case 68 -> Input.Keys.D;
            case 82 -> Input.Keys.R;
            case 32 -> Input.Keys.SPACE;
            case 265 -> Input.Keys.UP;
            case 264 -> Input.Keys.DOWN;
            case 263 -> Input.Keys.LEFT;
            case 262 -> Input.Keys.RIGHT;
            case 257 -> Input.Keys.ENTER;
            case 256 -> Input.Keys.ESCAPE;
            default -> -1;
        };
    }

    /** Refresh physical keyboard state by polling every frame.
     *  Keeps keyDownTime current while a key is physically held, even if
     *  the event callbacks are unreliable on this device. */
    private static final int[] POLL_GDX_KEYS = {
            Input.Keys.W, Input.Keys.A, Input.Keys.S, Input.Keys.D,
            Input.Keys.R, Input.Keys.SPACE
    };
    private void pollPhysicalKeys() {
        long now = System.currentTimeMillis();
        for (int gk : POLL_GDX_KEYS) {
            boolean pressed = Gdx.input.isKeyPressed(gk);
            if (pressed) {
                if (!keyHeld[gk]) {
                    Gdx.app.log(TAG, "poll: code=" + gk + " isKeyPressed=TRUE but keyHeld was FALSE → fixing");
                }
                keyHeld[gk] = true;
                keyDownTime[gk] = now;
            }
        }
    }

    /** Called once per frame to process touch state. */
    public void update() {
        // Keep physical key timestamps fresh via polling
        pollPhysicalKeys();

        // Continuous hold-to-destroy: fires periodically while the finger
        // is held on the right side for >= LONG_PRESS_MS.
        // Once activated (holdDestroyActive), keeps firing even if the finger
        // moves (so you can look around while destroying).
        if (lookTouchId != -1 && (!isLookDrag || holdDestroyActive)) {
            long now = System.currentTimeMillis();
            long elapsed = now - touchDownTime;
            if (elapsed >= LONG_PRESS_MS && now - lastDestroyTime >= DESTROY_INTERVAL_MS) {
                longPressDetected = true;
                lastDestroyTime = now;
                if (!holdDestroyActive) {
                    holdDestroyActive = true;
                    Gdx.app.log(TAG, "update → hold-destroy ACTIVATED (elapsed=" + elapsed + "ms)");
                }
            }
        }
    }

    // ── RDInput implementation ─────────────────────────────────────────

    // Throttle isKeyDown logging: log at most once per key per 500ms
    private final long[] lastLogTime = new long[256];

    @Override
    public boolean isKeyDown(int keyCode) {
        int gdxKey = toGdxKey(keyCode);
        if (gdxKey != -1 && gdxKey < keyHeld.length) {
            long now = System.currentTimeMillis();
            boolean held = keyHeld[gdxKey];
            boolean polled = Gdx.input.isKeyPressed(gdxKey);
            long holdDuration = keyUpTime[gdxKey] - keyDownTime[gdxKey];
            long sincePress = now - keyDownTime[gdxKey];
            boolean bridged = holdDuration >= 0 && holdDuration < BATCH_THRESHOLD_MS
                    && sincePress < KEY_REPEAT_BRIDGE_MS;

            boolean result = held || polled || bridged;

            // Log at most every 500ms per key, when there's something interesting
            if (result && now - lastLogTime[gdxKey] > 500) {
                lastLogTime[gdxKey] = now;
                Gdx.app.log(TAG, "isKeyDown(glfw=" + keyCode + " gdx=" + gdxKey + ")=TRUE"
                        + " held=" + held + " polled=" + polled + " bridged=" + bridged
                        + " holdDur=" + holdDuration + " sincePress=" + sincePress);
            }

            if (result) return true;
        }
        // Fall back to touch zone virtual keys
        return switch (keyCode) {
            case 87, 265 -> keyW;  // W or UP
            case 65, 263 -> keyA;  // A or LEFT
            case 83, 264 -> keyS;  // S or DOWN
            case 68, 262 -> keyD;  // D or RIGHT
            default -> false;
        };
    }

    @Override
    public void setCharCallback(CharCallback callback) {
        // Android text input handled via native dialog
    }

    @Override
    public void setKeyCallback(KeyCallback callback) {
        // Key events routed through libGDX InputProcessor
    }

    @Override
    public float consumeMouseDX() {
        float dx = lookDX;
        lookDX = 0;
        return dx;
    }

    @Override
    public float consumeMouseDY() {
        float dy = lookDY;
        lookDY = 0;
        return dy;
    }

    @Override
    public boolean isMouseButtonDown(int button) {
        if (button == 0) {
            boolean t = tapDetected;
            tapDetected = false;
            if (t) {
                Gdx.app.log(TAG, "isMouseButtonDown(0) → TRUE (tap consumed)");
            }
            return t;
        }
        if (button == 1) {
            boolean lp = longPressDetected;
            longPressDetected = false;
            return lp;
        }
        return false;
    }

    @Override
    public void grabMouse() {
        // No cursor on Android — always in "grabbed" mode
    }

    @Override
    public void releaseMouse() {
        // No-op on Android
    }

    @Override
    public boolean isMouseGrabbed() {
        return true; // Always grabbed on Android
    }

    @Override
    public int getScreenWidth() {
        return Gdx.graphics.getWidth();
    }

    @Override
    public int getScreenHeight() {
        return Gdx.graphics.getHeight();
    }
}
