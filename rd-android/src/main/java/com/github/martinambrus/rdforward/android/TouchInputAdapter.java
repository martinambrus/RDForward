package com.github.martinambrus.rdforward.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.github.martinambrus.rdforward.render.RDInput;

/**
 * Maps Android touch input to the keyboard/mouse interface expected by
 * the game code.
 * <p>
 * Touch mapping:
 * <ul>
 *   <li>Right-side drag → camera look (mouse look)</li>
 *   <li>Left-side drag → movement (WASD)</li>
 *   <li>Tap (short, still touch) → place block (button 0)</li>
 *   <li>Long press (long, still touch) → destroy block (button 1)</li>
 *   <li>Two-finger tap → jump (space)</li>
 * </ul>
 */
public class TouchInputAdapter extends InputAdapter implements RDInput {

    // Virtual key states (set by touch zones)
    private boolean keyW, keyA, keyS, keyD, keySpace;

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

    // Simulated mouse state
    private boolean tapDetected;
    private boolean longPressDetected;
    private long touchDownTime;
    private float lookDragDist; // accumulated drag distance in pixels
    private static final float DRAG_THRESHOLD = 15f; // px — beyond this it's a look, not a tap
    private static final long LONG_PRESS_MS = 500;

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float screenWidth = Gdx.graphics.getWidth();
        float zoneWidth = screenWidth * MOVE_ZONE_FRACTION;

        if (screenX < zoneWidth && moveTouchId == -1) {
            // Left zone: movement
            moveTouchId = pointer;
            moveStartX = screenX;
            moveStartY = screenY;
        } else if (lookTouchId == -1) {
            // Right zone: camera look / block interaction
            lookTouchId = pointer;
            lookLastX = screenX;
            lookLastY = screenY;
            touchDownTime = System.currentTimeMillis();
            lookDragDist = 0;
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer == moveTouchId) {
            moveTouchId = -1;
            keyW = keyA = keyS = keyD = false;
        }
        if (pointer == lookTouchId) {
            // Only register tap/long-press if the finger stayed mostly still.
            // If the user dragged to look around, it's not a block interaction.
            if (lookDragDist < DRAG_THRESHOLD) {
                long elapsed = System.currentTimeMillis() - touchDownTime;
                if (elapsed < LONG_PRESS_MS) {
                    tapDetected = true; // short still touch = place block
                } else {
                    longPressDetected = true; // long still touch = destroy block
                }
            }
            lookTouchId = -1;
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
            lookDX += dx * LOOK_SENSITIVITY;
            lookDY += dy * LOOK_SENSITIVITY;
            lookLastX = screenX;
            lookLastY = screenY;
        }
        return true;
    }

    /** Called once per frame to process touch state. */
    public void update() {
        // Two-finger tap → jump
        if (Gdx.input.isTouched(0) && Gdx.input.isTouched(1)) {
            keySpace = true;
        } else {
            keySpace = false;
        }
    }

    // ── RDInput implementation ─────────────────────────────────────────

    @Override
    public boolean isKeyDown(int keyCode) {
        // GLFW key codes used by the game
        return switch (keyCode) {
            case 87, 265 -> keyW;  // W or UP
            case 65, 263 -> keyA;  // A or LEFT
            case 83, 264 -> keyS;  // S or DOWN
            case 68, 262 -> keyD;  // D or RIGHT
            case 32, 343 -> keySpace; // SPACE or SUPER
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
