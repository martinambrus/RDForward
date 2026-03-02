package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.mappings.FieldMappings;
import com.github.martinambrus.rdforward.e2e.agent.mappings.RubyDungMappings;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reflection-based input injection for Minecraft Alpha 1.2.6.
 *
 * Movement: Alpha has no KeyBinding.pressed field. Movement is driven by the
 * boolean[] array (he.f) on MovementInputFromOptions. We write directly to it.
 *
 * Mouse clicks: Call Minecraft.a(int) via reflection (0=left, 1=right).
 *
 * Look direction: Set Entity.yaw/pitch fields directly.
 */
public class InputController {

    // Movement key indices in the he.f[] boolean array
    public static final int FORWARD = 0;
    public static final int BACK = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;
    public static final int JUMP = 4;
    public static final int SNEAK = 5;

    private final GameState gameState;
    private final FieldMappings mappings;

    // Cached reflection handles
    private Field movementInputField;   // EntityPlayerSP -> MovementInput
    private Field pressedKeysField;     // boolean[] (Alpha only)
    private Field yawField;
    private Field pitchField;
    private Field prevYawField;
    private Field prevPitchField;
    private Method clickMethod;
    private Method rightClickMethod;
    // 0=int param (pre-Netty), 1=no-arg, 2=boolean param
    private int clickMethodType;
    private int rightClickMethodType;
    private Field clickCooldownField;
    private Field playerControllerField;
    private Method digMethod;
    private boolean usesBlockPos;
    private Constructor<?> digBlockPosConstructor;
    private Object enumFacingUp;

    // KeyBinding-based movement (Beta 1.8+)
    private boolean useKeyBindingMovement;
    private boolean movementModeResolved;
    private Field gameSettingsField;
    private Field keyBindingPressedField;
    private Field[] keyBindingFields;   // [forward, back, left, right, jump, sneak]

    // Last known player object (to detect respawns)
    private Object lastPlayer;

    // Debug tracking for right-click invocations
    private String lastRightClickResult = "none";
    // Debug tracking for lookAtBlock
    private String lastLookResult = "none";

    /**
     * Returns debug info about right-click method resolution.
     */
    public String getRightClickDebug() {
        if (rightClickMethod == null && clickMethod == null) {
            return "not_resolved_yet";
        }
        return "click=" + (clickMethod != null ? clickMethod.getName() + "(type=" + clickMethodType + ")" : "null")
                + " rightClick=" + (rightClickMethod != null ? rightClickMethod.getName() + "(type=" + rightClickMethodType + ")" : "NULL")
                + " pending=" + pendingClicks + " lastRC=" + lastRightClickResult;
    }

    // Tracks whether the agent intentionally opened a screen (inventory GUI).
    // When false and currentScreen is non-null, it's an unwanted pause menu
    // from Xvfb focus loss, and should be dismissed.
    private boolean agentOpenedScreen;

    // Pending state
    private final boolean[] pendingMovement = new boolean[6];
    private final List<Integer> pendingClicks = new ArrayList<Integer>();

    public InputController(GameState gameState, FieldMappings mappings) {
        this.gameState = gameState;
        this.mappings = mappings;
    }

    /** Append a debug line to statusDir/debug_inventory.log (best-effort). */
    private void debugLog(String msg) {
        System.out.println("[McTestAgent] " + msg);
        try {
            File dir = McTestAgent.statusDir;
            if (dir != null) {
                try (PrintWriter pw = new PrintWriter(
                        new FileWriter(new File(dir, "debug_inventory.log"), true))) {
                    pw.println(msg);
                }
            }
        } catch (Exception ignored) {}
    }

    public void pressKey(int index) {
        if (index >= 0 && index < pendingMovement.length) {
            pendingMovement[index] = true;
        }
    }

    public void releaseKey(int index) {
        if (index >= 0 && index < pendingMovement.length) {
            pendingMovement[index] = false;
        }
    }

    public void releaseAllKeys() {
        for (int i = 0; i < pendingMovement.length; i++) {
            pendingMovement[i] = false;
        }
    }

    public void click(int button) {
        pendingClicks.add(button);
    }

    /**
     * Set the player's look direction directly via Entity fields.
     */
    public void setLookDirection(float yaw, float pitch) {
        Object player = gameState.getPlayer();
        if (player == null) { lastLookResult = "no_player"; return; }
        try {
            ensureYawPitchFields(player);
            float oldYaw = yawField.getFloat(player);
            yawField.setFloat(player, yaw);
            pitchField.setFloat(player, pitch);
            float readBack = yawField.getFloat(player);
            lastLookResult = "set(y=" + yaw + ",p=" + pitch + ") old=" + oldYaw
                    + " rb=" + readBack
                    + " fld=" + yawField.getName() + "@" + yawField.getDeclaringClass().getSimpleName()
                    + " obj=" + player.getClass().getSimpleName();
            // Also set prevRotation fields to eliminate render interpolation artifacts.
            // The renderer computes: prevPitch + (pitch - prevPitch) * partialTick,
            // so without setting prev fields, the camera may not match the intended angle.
            if (prevYawField != null) prevYawField.setFloat(player, yaw);
            if (prevPitchField != null) prevPitchField.setFloat(player, pitch);
        } catch (Exception e) {
            lastLookResult = "error:" + e.getMessage();
        }
    }

    public String getLastLookResult() { return lastLookResult; }

    /**
     * Compute yaw/pitch to look at a specific block position from the player's current position.
     * Player position is eye-level.
     */
    public void lookAtBlock(double bx, double by, double bz) {
        double[] pos = gameState.getPlayerPosition();
        if (pos == null) return;
        // Target center of block face (add 0.5 to block coords)
        double dx = (bx + 0.5) - pos[0];
        double dy = (by + 0.5) - pos[1]; // pos[1] is already eye-level
        double dz = (bz + 0.5) - pos[2];

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw;
        if (isRubyDung()) {
            // RubyDung yaw: 0 = north (-Z), 90 = east (+X). OpenGL convention.
            yaw = (float) Math.toDegrees(Math.atan2(dx, -dz));
        } else {
            // Alpha yaw: 0 = south (+Z), 90 = west (-X), 180 = north (-Z), 270 = east (+X)
            yaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
        }
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));

        setLookDirection(yaw, pitch);
    }

    /**
     * Apply all pending inputs. Called once per tick from TickHook BEFORE scenario step.
     */
    public void applyInputs() {
        Object player = gameState.getPlayer();
        if (player == null) return;

        try {
            // Invalidate cached fields if player object changed (respawn)
            if (player != lastPlayer) {
                movementInputField = null;
                yawField = null;
                pitchField = null;
                prevYawField = null;
                prevPitchField = null;
                lastPlayer = player;
            }

            // Ensure mouse is grabbed (field L on Minecraft)
            if (mappings.mouseGrabbedFieldName() != null) {
                if (!gameState.isMouseGrabbed()) {
                    gameState.setMouseGrabbed(true);
                }
            }

            // Apply movement keys to he.f[] boolean array
            applyMovement(player);

            // Execute pending clicks
            if (!pendingClicks.isEmpty()) {
                ensureClickMethod();

                // Reset the click cooldown (field S on Minecraft) before left-clicks.
                // Alpha's a(0) silently returns when S > 0; S is set to 10
                // when objectMouseOver is null during a left-click, creating a
                // deadlock where the cooldown blocks all subsequent clicks.
                boolean hasLeftClick = false;
                for (int b : pendingClicks) {
                    if (b == 0) { hasLeftClick = true; break; }
                }
                if (hasLeftClick) {
                    ensureClickCooldownField();
                    clickCooldownField.setInt(gameState.getMinecraftInstance(), 0);
                }

                Object mc = gameState.getMinecraftInstance();
                for (int button : pendingClicks) {
                    if (clickMethodType == 0) {
                        // Pre-Netty: a(int)
                        clickMethod.invoke(mc, button);
                    } else if (button == 0) {
                        // Left click
                        if (clickMethodType == 2) clickMethod.invoke(mc, true);
                        else clickMethod.invoke(mc);
                    } else if (button == 1 && rightClickMethod != null) {
                        // Right click
                        try {
                            if (rightClickMethodType == 2) rightClickMethod.invoke(mc, true);
                            else rightClickMethod.invoke(mc);
                            lastRightClickResult = "ok";
                        } catch (Exception rcEx) {
                            lastRightClickResult = "error:" + rcEx.getMessage();
                            System.err.println("[McTestAgent] Right-click invoke error: " + rcEx);
                            rcEx.printStackTrace();
                        }
                    } else if (button == 1) {
                        lastRightClickResult = "null_method";
                    }
                }
                pendingClicks.clear();
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] InputController.applyInputs error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Dismiss the pause menu if it appeared due to Xvfb focus loss.
     * Only closes the screen if the agent did NOT intentionally open it
     * (e.g. inventory GUI). Safe to call every tick.
     */
    public void dismissPauseScreen() {
        if (mappings.displayGuiScreenMethodName() == null) return;
        try {
            Object screen = gameState.getCurrentScreen();
            if (!agentOpenedScreen && screen != null) {
                ensureDisplayGuiScreenMethod();
                displayGuiScreenMethod.invoke(gameState.getMinecraftInstance(), (Object) null);
                gameState.setMouseGrabbed(true);
            }
        } catch (Exception e) {
            // Silently ignore — this is a best-effort safety net
        }
    }

    /**
     * Directly send a dig-start packet for the given block position,
     * bypassing Minecraft.a(0) and its cooldown/objectMouseOver checks.
     * Uses the PlayerController's a(int,int,int,int) method.
     * Face 1 = top face (looking down).
     */
    public void breakBlock(int x, int y, int z) {
        try {
            Object mc = gameState.getMinecraftInstance();
            ensurePlayerControllerField(mc);
            Object playerController = playerControllerField.get(mc);
            if (playerController == null) {
                System.err.println("[McTestAgent] PlayerController is null");
                return;
            }
            ensureDigMethod(playerController);

            // For 1.8+ (posYIsFeetLevel), snapshot non-static boolean fields
            // before dig — startDestroyBlock() sets an internal isDestroying
            // flag that blocks subsequent right-clicks. We find which field
            // changed and reset it. Skip for pre-1.8 where dig doesn't set
            // this flag (and the reset can cause side effects).
            boolean resetIsDestroying = gameState.getMappings().posYIsFeetLevel();
            java.lang.reflect.Field[] boolFields = null;
            boolean[] before = null;
            if (resetIsDestroying) {
                boolFields = getBooleanFields(playerController);
                before = new boolean[boolFields.length];
                for (int i = 0; i < boolFields.length; i++) {
                    before[i] = boolFields[i].getBoolean(playerController);
                }
            }

            if (usesBlockPos) {
                Object pos = digBlockPosConstructor.newInstance(x, y, z);
                digMethod.invoke(playerController, pos, enumFacingUp);
            } else {
                digMethod.invoke(playerController, x, y, z, 1); // face=1 (top)
            }

            if (resetIsDestroying) {
                // Reset isDestroying: find the boolean that flipped false→true
                for (int i = 0; i < boolFields.length; i++) {
                    boolean after = boolFields[i].getBoolean(playerController);
                    if (!before[i] && after) {
                        boolFields[i].setBoolean(playerController, false);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] breakBlock error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private java.lang.reflect.Field[] getBooleanFields(Object obj) {
        java.util.List<java.lang.reflect.Field> result = new java.util.ArrayList<java.lang.reflect.Field>();
        Class<?> c = obj.getClass();
        while (c != null && c != Object.class) {
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                if (f.getType() == boolean.class
                        && !java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    result.add(f);
                }
            }
            c = c.getSuperclass();
        }
        return result.toArray(new java.lang.reflect.Field[0]);
    }

    // --- RubyDung-specific methods ---

    /**
     * Returns true if the current client is RubyDung (uses RubyDungMappings).
     */
    public boolean isRubyDung() {
        return mappings instanceof RubyDungMappings;
    }

    // Cached reflection handles for RubyDung direct interaction
    private Method setTileMethod;
    private Field blockChangeQueueField;
    private Field rdPlayerXField, rdPlayerYField, rdPlayerZField;
    private Field rdBbField; // player bounding box
    private Field rdBbX0, rdBbY0, rdBbZ0, rdBbX1, rdBbY1, rdBbZ1;

    /**
     * Move the player position by delta values. Used for RubyDung where
     * GLFW key polling happens before our TickAdvice, making key injection impossible.
     */
    public void movePlayerPosition(float dx, float dy, float dz) {
        Object player = gameState.getPlayer();
        if (player == null) return;
        try {
            if (rdPlayerXField == null) {
                Class<?> c = player.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Field fx = c.getDeclaredField(mappings.posXFieldName());
                        fx.setAccessible(true);
                        rdPlayerXField = fx;
                        Field fy = c.getDeclaredField(mappings.posYFieldName());
                        fy.setAccessible(true);
                        rdPlayerYField = fy;
                        Field fz = c.getDeclaredField(mappings.posZFieldName());
                        fz.setAccessible(true);
                        rdPlayerZField = fz;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                    c = c.getSuperclass();
                }
            }
            if (rdPlayerXField == null) return;

            // RubyDung uses float position fields
            if (rdPlayerXField.getType() == float.class) {
                rdPlayerXField.setFloat(player, rdPlayerXField.getFloat(player) + dx);
                rdPlayerYField.setFloat(player, rdPlayerYField.getFloat(player) + dy);
                rdPlayerZField.setFloat(player, rdPlayerZField.getFloat(player) + dz);
            } else {
                rdPlayerXField.setDouble(player, rdPlayerXField.getDouble(player) + dx);
                rdPlayerYField.setDouble(player, rdPlayerYField.getDouble(player) + dy);
                rdPlayerZField.setDouble(player, rdPlayerZField.getDouble(player) + dz);
            }

            // Also shift the bounding box. Player.move() recalculates x/y/z
            // from the bounding box, so without this, position changes are reverted.
            if (rdBbField == null) {
                Class<?> c = player.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Field f = c.getDeclaredField("bb");
                        f.setAccessible(true);
                        rdBbField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                    c = c.getSuperclass();
                }
            }
            if (rdBbField != null) {
                Object bb = rdBbField.get(player);
                if (bb != null) {
                    if (rdBbX0 == null) {
                        Class<?> bbClass = bb.getClass();
                        rdBbX0 = bbClass.getDeclaredField("x0"); rdBbX0.setAccessible(true);
                        rdBbY0 = bbClass.getDeclaredField("y0"); rdBbY0.setAccessible(true);
                        rdBbZ0 = bbClass.getDeclaredField("z0"); rdBbZ0.setAccessible(true);
                        rdBbX1 = bbClass.getDeclaredField("x1"); rdBbX1.setAccessible(true);
                        rdBbY1 = bbClass.getDeclaredField("y1"); rdBbY1.setAccessible(true);
                        rdBbZ1 = bbClass.getDeclaredField("z1"); rdBbZ1.setAccessible(true);
                    }
                    rdBbX0.setFloat(bb, rdBbX0.getFloat(bb) + dx);
                    rdBbY0.setFloat(bb, rdBbY0.getFloat(bb) + dy);
                    rdBbZ0.setFloat(bb, rdBbZ0.getFloat(bb) + dz);
                    rdBbX1.setFloat(bb, rdBbX1.getFloat(bb) + dx);
                    rdBbY1.setFloat(bb, rdBbY1.getFloat(bb) + dy);
                    rdBbZ1.setFloat(bb, rdBbZ1.getFloat(bb) + dz);
                }
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] movePlayerPosition error: " + e.getMessage());
        }
    }

    /**
     * Place a block directly via Level.setTile() + blockChangeQueue.
     * RubyDung only — bypasses client click/raycast mechanics.
     */
    public void placeBlockDirect(int x, int y, int z, int blockType) {
        try {
            Object world = gameState.getWorld();
            if (world == null) {
                System.err.println("[McTestAgent] Cannot placeBlockDirect: no world");
                return;
            }

            // Call Level.setTile(x, y, z, blockType)
            if (setTileMethod == null) {
                Class<?> c = world.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Method m = c.getDeclaredMethod(mappings.setTileMethodName(),
                                int.class, int.class, int.class, int.class);
                        m.setAccessible(true);
                        setTileMethod = m;
                        break;
                    } catch (NoSuchMethodException ignored) {}
                    c = c.getSuperclass();
                }
                if (setTileMethod == null) {
                    throw new RuntimeException("setTile method not found on "
                            + world.getClass().getName());
                }
            }
            setTileMethod.invoke(world, x, y, z, blockType);

            // Add to blockChangeQueue so the Mixin sends it to the server
            if (blockChangeQueueField == null) {
                // blockChangeQueue is a static field on the RubyDung class
                Class<?> rdClass = gameState.getMinecraftInstance().getClass();
                while (rdClass != null && rdClass != Object.class) {
                    try {
                        Field f = rdClass.getDeclaredField(mappings.blockChangeQueueFieldName());
                        f.setAccessible(true);
                        blockChangeQueueField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                    rdClass = rdClass.getSuperclass();
                }
            }
            if (blockChangeQueueField != null) {
                @SuppressWarnings("unchecked")
                List<int[]> queue = (List<int[]>) blockChangeQueueField.get(
                        gameState.getMinecraftInstance());
                if (queue != null) {
                    // Queue format: {x, y, z, mode, blockType}
                    // mode=0 for destroy, mode=1 for place
                    int mode = (blockType != 0) ? 1 : 0;
                    queue.add(new int[]{x, y, z, mode, blockType});
                }
            }

            System.out.println("[McTestAgent] placeBlockDirect at ("
                    + x + "," + y + "," + z + ") type=" + blockType);
        } catch (Exception e) {
            System.err.println("[McTestAgent] placeBlockDirect error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Break a block directly via placeBlockDirect(x, y, z, 0).
     * RubyDung only.
     */
    public void breakBlockDirect(int x, int y, int z) {
        placeBlockDirect(x, y, z, 0);
    }

    /**
     * Clear hitResult on the RubyDung instance to remove the pulsing block
     * highlight overlay. Call before capturing screenshots for consistency.
     */
    private Field hitResultField;
    public void clearHitResult() {
        if (!isRubyDung()) return;
        try {
            Object mc = gameState.getMinecraftInstance();
            if (mc == null) return;
            if (hitResultField == null) {
                Class<?> c = mc.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Field f = c.getDeclaredField("hitResult");
                        f.setAccessible(true);
                        hitResultField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                    c = c.getSuperclass();
                }
            }
            if (hitResultField != null) {
                hitResultField.set(mc, null);
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] clearHitResult error: " + e.getMessage());
        }
    }

    private void applyMovement(Object player) throws Exception {
        // Determine movement mode once
        if (!movementModeResolved) {
            useKeyBindingMovement = mappings.pressedKeysFieldName() == null
                    && mappings.keyBindingPressedFieldName() != null;
            movementModeResolved = true;
        }

        // RubyDung: neither pressedKeys nor keyBinding — skip movement entirely.
        // RD polls GLFW keys in Player.tick() before our TickAdvice fires,
        // making key injection impossible. Use movePlayerPosition() instead.
        if (mappings.pressedKeysFieldName() == null
                && mappings.keyBindingPressedFieldName() == null) {
            return;
        }

        if (useKeyBindingMovement) {
            applyKeyBindingMovement();
        } else {
            applyBooleanArrayMovement(player);
        }
    }

    /**
     * Alpha-style: write directly to the boolean[] on MovementInputFromOptions.
     */
    private void applyBooleanArrayMovement(Object player) throws Exception {
        if (movementInputField == null) {
            Class<?> c = player.getClass();
            while (c != null && c != Object.class) {
                try {
                    Field f = c.getDeclaredField(mappings.movementInputFieldName());
                    f.setAccessible(true);
                    movementInputField = f;
                    break;
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
            if (movementInputField == null) {
                System.err.println("[McTestAgent] movementInput field not found");
                return;
            }
        }

        Object movementInput = movementInputField.get(player);
        if (movementInput == null) return;

        if (pressedKeysField == null) {
            Class<?> c = movementInput.getClass();
            while (c != null && c != Object.class) {
                try {
                    Field f = c.getDeclaredField(mappings.pressedKeysFieldName());
                    f.setAccessible(true);
                    pressedKeysField = f;
                    break;
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
            if (pressedKeysField == null) {
                System.err.println("[McTestAgent] pressedKeys field not found");
                return;
            }
        }

        boolean[] keys = (boolean[]) pressedKeysField.get(movementInput);
        if (keys == null) return;

        int len = Math.min(pendingMovement.length, keys.length);
        System.arraycopy(pendingMovement, 0, keys, 0, len);
    }

    /**
     * Beta-style: set KeyBinding.pressed on individual GameSettings key fields.
     */
    private void applyKeyBindingMovement() throws Exception {
        if (gameSettingsField == null) {
            Object mc = gameState.getMinecraftInstance();
            Class<?> c = mc.getClass();
            while (c != null && c != Object.class) {
                try {
                    Field f = c.getDeclaredField(mappings.gameSettingsFieldName());
                    f.setAccessible(true);
                    gameSettingsField = f;
                    break;
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
            if (gameSettingsField == null) {
                System.err.println("[McTestAgent] gameSettings field not found");
                return;
            }
        }

        Object gs = gameSettingsField.get(gameState.getMinecraftInstance());
        if (gs == null) return;

        if (keyBindingFields == null) {
            keyBindingFields = new Field[6];
            String[] names = {
                mappings.forwardKeyFieldName(), mappings.backKeyFieldName(),
                mappings.leftKeyFieldName(), mappings.rightKeyFieldName(),
                mappings.jumpKeyFieldName(), mappings.sneakKeyFieldName()
            };
            Class<?> gsClass = gs.getClass();
            for (int i = 0; i < 6; i++) {
                if (names[i] == null) continue;
                Class<?> c = gsClass;
                while (c != null && c != Object.class) {
                    try {
                        Field f = c.getDeclaredField(names[i]);
                        f.setAccessible(true);
                        keyBindingFields[i] = f;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                    c = c.getSuperclass();
                }
            }
        }

        String pressedFieldName = mappings.keyBindingPressedFieldName();
        for (int i = 0; i < 6; i++) {
            if (keyBindingFields[i] == null) continue;
            Object keyBinding = keyBindingFields[i].get(gs);
            if (keyBinding == null) continue;

            if (keyBindingPressedField == null) {
                Class<?> c = keyBinding.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Field f = c.getDeclaredField(pressedFieldName);
                        f.setAccessible(true);
                        keyBindingPressedField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                    c = c.getSuperclass();
                }
            }
            if (keyBindingPressedField != null) {
                keyBindingPressedField.setBoolean(keyBinding, pendingMovement[i]);
            }
        }
    }

    private void ensureYawPitchFields(Object player) throws Exception {
        if (yawField == null) {
            Class<?> c = player.getClass();
            while (c != null && c != Object.class) {
                try {
                    Field f = c.getDeclaredField(mappings.yawFieldName());
                    // Must be a non-static float — avoid shadowing by
                    // static final int fields on intermediate classes (e.g.
                    // LivingEntity.aA shadows Entity.aA in 1.18.2)
                    if (f.getType() == float.class
                            && !java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                        f.setAccessible(true);
                        yawField = f;
                        pitchField = c.getDeclaredField(mappings.pitchFieldName());
                        pitchField.setAccessible(true);
                        // Resolve prevRotation fields (declared +2 positions after rotation
                        // in obfuscated Entity class: yaw,pitch,prevYaw,prevPitch)
                        resolvePrevRotationFields(c);
                        break;
                    }
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
        }
    }

    /**
     * Try to find prevRotationYaw and prevRotationPitch fields in the given class.
     * In obfuscated Minecraft, these are the next two float fields after yaw/pitch
     * (e.g. aS,aT -> aU,aV). For non-obfuscated names (RubyDung), this is skipped.
     */
    private void resolvePrevRotationFields(Class<?> declaringClass) {
        String yawName = mappings.yawFieldName();
        String pitchName = mappings.pitchFieldName();
        if (yawName == null || pitchName == null) return;

        // Compute prev field names by incrementing last character by 2
        // This works for obfuscated single/two-char names (aS->aU, u->w)
        String prevYawName = incrementFieldName(pitchName, 1); // prevYaw is pitch+1
        String prevPitchName = incrementFieldName(pitchName, 2); // prevPitch is pitch+2

        try {
            Field f = declaringClass.getDeclaredField(prevYawName);
            if (f.getType() == float.class) {
                f.setAccessible(true);
                prevYawField = f;
            }
        } catch (NoSuchFieldException ignored) {}

        try {
            Field f = declaringClass.getDeclaredField(prevPitchName);
            if (f.getType() == float.class) {
                f.setAccessible(true);
                prevPitchField = f;
            }
        } catch (NoSuchFieldException ignored) {}

        if (prevYawField != null && prevPitchField != null) {
            System.out.println("[McTestAgent] Resolved prevRotation fields: "
                    + prevYawName + ", " + prevPitchName);
        }
    }

    private static String incrementFieldName(String name, int offset) {
        if (name == null || name.isEmpty()) return null;
        char last = name.charAt(name.length() - 1);
        char incremented = (char) (last + offset);
        // ProGuard field naming goes a-z then A-Z. Handle the wrap.
        if (last >= 'a' && last <= 'z' && incremented > 'z') {
            incremented = (char) ('A' + (incremented - 'z' - 1));
        }
        return name.substring(0, name.length() - 1) + incremented;
    }

    private void ensureClickMethod() throws Exception {
        if (clickMethod != null) return;
        Class<?> mcClass = gameState.getMinecraftInstance().getClass();
        String methodName = mappings.clickMethodName();

        if (!mappings.isNettyClient()) {
            // Pre-Netty: int param (the only variant)
            Class<?> c = mcClass;
            while (c != null && c != Object.class) {
                try {
                    Method m = c.getDeclaredMethod(methodName, int.class);
                    m.setAccessible(true);
                    clickMethod = m;
                    clickMethodType = 0;
                    return;
                } catch (NoSuchMethodException ignored) {}
                c = c.getSuperclass();
            }
        } else {
            // Netty: try boolean first (1.9.1+), then no-arg (1.7-1.9.0).
            // Boolean first prevents generic names like "b" matching wrong no-arg methods.
            Class<?> c = mcClass;
            while (c != null && c != Object.class) {
                try {
                    Method m = c.getDeclaredMethod(methodName, boolean.class);
                    if (m.getReturnType() == void.class) {
                        m.setAccessible(true);
                        clickMethod = m;
                        clickMethodType = 2;
                        resolveRightClickMethod(mcClass, 2);
                        System.out.println("[McTestAgent] Click method: "
                                + methodName + "(boolean) on " + c.getName());
                        return;
                    }
                } catch (NoSuchMethodException ignored) {}
                c = c.getSuperclass();
            }
            c = mcClass;
            while (c != null && c != Object.class) {
                try {
                    Method m = c.getDeclaredMethod(methodName);
                    if (m.getReturnType() == void.class) {
                        m.setAccessible(true);
                        clickMethod = m;
                        clickMethodType = 1;
                        resolveRightClickMethod(mcClass, 1);
                        System.out.println("[McTestAgent] Click method: "
                                + methodName + "() on " + c.getName());
                        return;
                    }
                } catch (NoSuchMethodException ignored) {}
                c = c.getSuperclass();
            }
        }

        throw new RuntimeException("Click method '" + methodName + "' not found on "
                + mcClass.getName());
    }

    /**
     * Resolves the right-click method on the Minecraft class.
     * Uses explicit mapping if available, otherwise discovers by incrementing
     * the left-click method name.
     */
    private void resolveRightClickMethod(Class<?> mcClass, int expectedType) {
        String rightName = mappings.rightClickMethodName();
        if (rightName == null) {
            // Discovery: try incrementing last char of click method name
            rightName = incrementFieldName(mappings.clickMethodName(), 1);
        }
        if (rightName == null) return;

        // Try boolean first (1.9.1+), then no-arg (1.7-1.9.0).
        // Boolean first prevents generic names matching wrong no-arg methods.
        Class<?> c = mcClass;
        while (c != null && c != Object.class) {
            try {
                Method m = c.getDeclaredMethod(rightName, boolean.class);
                if (m.getReturnType() == void.class) {
                    m.setAccessible(true);
                    rightClickMethod = m;
                    rightClickMethodType = 2;
                    System.out.println("[McTestAgent] Right-click method: "
                            + rightName + "(boolean)");
                    return;
                }
            } catch (NoSuchMethodException ignored) {}
            try {
                Method m = c.getDeclaredMethod(rightName);
                if (m.getReturnType() == void.class) {
                    m.setAccessible(true);
                    rightClickMethod = m;
                    rightClickMethodType = 1;
                    System.out.println("[McTestAgent] Right-click method: " + rightName + "()");
                    return;
                }
            } catch (NoSuchMethodException ignored) {}
            c = c.getSuperclass();
        }
        System.err.println("[McTestAgent] Right-click method '" + rightName
                + "' not found — right-clicks will be ignored");
    }

    private void ensureClickCooldownField() throws Exception {
        if (clickCooldownField != null) return;
        String fieldName = mappings.clickCooldownFieldName();
        Class<?> c = gameState.getMinecraftInstance().getClass();
        while (c != null && c != Object.class) {
            try {
                Field f = c.getDeclaredField(fieldName);
                if (f.getType() == int.class) {
                    f.setAccessible(true);
                    clickCooldownField = f;
                    return;
                }
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        // Non-fatal: cooldown reset is best-effort
        System.err.println("[McTestAgent] Click cooldown field '" + fieldName + "' not found");
    }

    private void ensurePlayerControllerField(Object mc) throws Exception {
        if (playerControllerField != null) return;
        String fieldName = mappings.playerControllerFieldName();
        Class<?> c = mc.getClass();
        while (c != null && c != Object.class) {
            try {
                Field f = c.getDeclaredField(fieldName);
                f.setAccessible(true);
                playerControllerField = f;
                return;
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        throw new RuntimeException("PlayerController field '" + fieldName + "' not found on " + mc.getClass().getName());
    }

    private void ensureDigMethod(Object playerController) throws Exception {
        if (digMethod != null) return;
        String methodName = mappings.digMethodName();

        // Try 1: (int,int,int,int) — pre-Netty + V4/V5 (1.7.x)
        Class<?> c = playerController.getClass();
        while (c != null && c != Object.class) {
            try {
                Method m = c.getDeclaredMethod(methodName,
                        int.class, int.class, int.class, int.class);
                m.setAccessible(true);
                digMethod = m;
                usesBlockPos = false;
                return;
            } catch (NoSuchMethodException ignored) {}
            c = c.getSuperclass();
        }

        // Try 2: 2-param method (V47+ BlockPos + EnumFacing)
        c = playerController.getClass();
        while (c != null && c != Object.class) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(methodName) && m.getParameterCount() == 2
                        && !m.getParameterTypes()[0].isPrimitive()
                        && !m.getParameterTypes()[1].isPrimitive()) {
                    m.setAccessible(true);
                    digMethod = m;
                    usesBlockPos = true;
                    Class<?>[] paramTypes = m.getParameterTypes();
                    digBlockPosConstructor = paramTypes[0].getConstructor(
                            int.class, int.class, int.class);
                    digBlockPosConstructor.setAccessible(true);
                    // Get EnumFacing.UP: ordinal 1 in all Netty versions
                    enumFacingUp = paramTypes[1].getEnumConstants()[1];
                    System.out.println("[McTestAgent] Dig method uses BlockPos+"
                            + paramTypes[1].getSimpleName()
                            + "; UP=" + enumFacingUp);
                    return;
                }
            }
            c = c.getSuperclass();
        }

        throw new RuntimeException("Dig method '" + methodName + "' not found on "
                + playerController.getClass().getName());
    }

    // --- Phase 3: Chat, Q-drop, inventory GUI methods ---

    // Cached reflection handles for Phase 3
    private Method sendChatMessageMethod;
    private boolean sendChatIs2Param; // 1.19.1+: chatSigned(String, Component)
    private Field sendChatNetworkHandler; // 1.19.3+: handler field on player for chat
    private Method dropPlayerItemMethod;
    private Method displayGuiScreenMethod;
    private Field inventoryField;
    private Field currentItemField;
    private Field craftingInventoryField;
    private Constructor<?> guiInventoryConstructor;
    private Method guiContainerMouseClickedMethod;
    private Field guiScreenWidthField;
    private Field guiScreenHeightField;

    // Slot pixel positions for GuiInventory (ne) - relative to container origin
    // Format: [windowSlot] = {x, y}
    private static final int[][] SLOT_POSITIONS = new int[45][2];
    static {
        // Slot 0: craft output at (144, 36)
        SLOT_POSITIONS[0] = new int[]{144, 36};
        // Slots 1-4: 2x2 craft grid
        for (int i = 0; i < 4; i++) {
            SLOT_POSITIONS[1 + i] = new int[]{88 + (i % 2) * 18, 26 + (i / 2) * 18};
        }
        // Slots 5-8: armor
        for (int i = 0; i < 4; i++) {
            SLOT_POSITIONS[5 + i] = new int[]{8, 8 + i * 18};
        }
        // Slots 9-35: main inventory (3 rows of 9)
        for (int i = 0; i < 27; i++) {
            SLOT_POSITIONS[9 + i] = new int[]{8 + (i % 9) * 18, 84 + (i / 9) * 18};
        }
        // Slots 36-44: hotbar
        for (int i = 0; i < 9; i++) {
            SLOT_POSITIONS[36 + i] = new int[]{8 + i * 18, 142};
        }
    }

    // Cached RubyDung chat reflection handles
    private Method rdSendChatMethod;
    private Object rdClientInstance;

    /**
     * Send a chat message via EntityPlayerSP.sendChatMessage(String).
     * For 1.19.3+, the method moved to ClientPlayNetworkHandler; accessed via
     * the networkHandler field on the player.
     * For RubyDung, uses RDClient.getInstance().sendChat(String) via Fabric classloader.
     */
    public void sendChatMessage(String message) {
        if (isRubyDung()) {
            sendChatMessageRubyDung(message);
            return;
        }

        Object player = gameState.getPlayer();
        if (player == null) {
            System.err.println("[McTestAgent] Cannot send chat: no player");
            return;
        }
        try {
            if (sendChatMessageMethod == null) {
                String methodName = mappings.sendChatMessageMethodName();
                String handlerField = mappings.networkHandlerFieldName();

                System.out.println("[McTestAgent] sendChat: handlerField="
                        + handlerField + " methodName=" + methodName);
                if (handlerField != null && methodName != null) {
                    // 1.19.3+ path: sendChatMessage is on the network handler, not the player.
                    // Use getDeclaredFields() instead of getDeclaredField() because ProGuard
                    // can map multiple fields to the same name with different types (e.g.,
                    // "cz" = ClientPacketListener AND "cz" = int on LocalPlayer in 1.20.6).
                    // We need the non-primitive Object field.
                    Class<?> pc = player.getClass();
                    while (pc != null && pc != Object.class) {
                        for (Field f : pc.getDeclaredFields()) {
                            if (f.getName().equals(handlerField)
                                    && !f.getType().isPrimitive()
                                    && !Modifier.isStatic(f.getModifiers())) {
                                f.setAccessible(true);
                                sendChatNetworkHandler = f;
                                System.out.println("[McTestAgent] sendChat: found handler field "
                                        + pc.getName() + "." + f.getName()
                                        + " (type=" + f.getType().getName() + ")");
                                break;
                            }
                        }
                        if (sendChatNetworkHandler != null) break;
                        pc = pc.getSuperclass();
                    }
                    if (sendChatNetworkHandler != null) {
                        Object handler = sendChatNetworkHandler.get(player);
                        System.out.println("[McTestAgent] sendChat: handler object="
                                + (handler != null ? handler.getClass().getName() : "null"));
                        if (handler != null) {
                            Class<?> c = handler.getClass();
                            while (c != null && c != Object.class) {
                                try {
                                    Method m = c.getDeclaredMethod(methodName, String.class);
                                    if (m.getReturnType() == void.class) {
                                        m.setAccessible(true);
                                        sendChatMessageMethod = m;
                                        break;
                                    }
                                } catch (NoSuchMethodException ignored) {}
                                c = c.getSuperclass();
                            }
                        }
                    } else {
                        System.out.println("[McTestAgent] sendChat: handler field not found on hierarchy");
                    }
                }

                if (sendChatMessageMethod == null && methodName != null) {
                    // Pre-1.19.3 path: method is on the player hierarchy
                    // First try single-param chat(String) (pre-1.19.1)
                    Class<?> c = player.getClass();
                    while (c != null && c != Object.class) {
                        try {
                            Method m = c.getDeclaredMethod(methodName, String.class);
                            if (m.getReturnType() == void.class) {
                                m.setAccessible(true);
                                sendChatMessageMethod = m;
                                break;
                            }
                        } catch (NoSuchMethodException ignored) {}
                        c = c.getSuperclass();
                    }
                    // If single-param not found, try chatSigned(String, Component) (1.19.1+)
                    if (sendChatMessageMethod == null) {
                        c = player.getClass();
                        while (c != null && c != Object.class) {
                            for (Method m : c.getDeclaredMethods()) {
                                if (m.getName().equals(methodName)
                                        && m.getReturnType() == void.class
                                        && m.getParameterCount() == 2
                                        && m.getParameterTypes()[0] == String.class
                                        && !m.getParameterTypes()[1].isPrimitive()) {
                                    m.setAccessible(true);
                                    sendChatMessageMethod = m;
                                    sendChatIs2Param = true;
                                    break;
                                }
                            }
                            if (sendChatMessageMethod != null) break;
                            c = c.getSuperclass();
                        }
                    }
                }

                if (sendChatMessageMethod == null) {
                    throw new RuntimeException("sendChatMessage method not found on "
                            + player.getClass().getName()
                            + (handlerField != null ? " or its networkHandler" : ""));
                }
            }

            if (sendChatNetworkHandler != null) {
                // 1.19.3+ path: invoke on the handler object
                Object handler = sendChatNetworkHandler.get(player);
                sendChatMessageMethod.invoke(handler, message);
            } else if (sendChatIs2Param) {
                // 1.19.1+ path: chatSigned(String, Component) — pass null for Component
                sendChatMessageMethod.invoke(player, message, null);
            } else {
                // Pre-1.19.1 path: chat(String) on the player
                sendChatMessageMethod.invoke(player, message);
            }
            System.out.println("[McTestAgent] Sent chat: " + message);
        } catch (Exception e) {
            System.err.println("[McTestAgent] sendChatMessage error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendChatMessageRubyDung(String message) {
        try {
            if (rdSendChatMethod == null) {
                ClassLoader cl = gameState.getMinecraftInstance().getClass().getClassLoader();
                Class<?> rdClientClass = cl.loadClass(
                        "com.github.martinambrus.rdforward.multiplayer.RDClient");
                Method getInstance = rdClientClass.getMethod("getInstance");
                rdClientInstance = getInstance.invoke(null);
                rdSendChatMethod = rdClientClass.getMethod("sendChat", String.class);
            }
            rdSendChatMethod.invoke(rdClientInstance, message);
            System.out.println("[McTestAgent] RD Sent chat: " + message);
        } catch (Exception e) {
            System.err.println("[McTestAgent] RD sendChat error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Drop the current held item (Q-drop).
     *
     * For Beta 1.8.1+ multiplayer clients: calls EntityPlayer.dropOneItem() which
     * is overridden in EntityClientPlayerMP to send Packet14BlockDig(status=4) to
     * the server. The server handles the actual inventory change and replenishment.
     *
     * For Alpha clients: manually calls decrStackSize + dropPlayerItem, which
     * triggers a PickupSpawnPacket to the server.
     */
    public void dropCurrentItem() {
        Object player = gameState.getPlayer();
        if (player == null) {
            System.err.println("[McTestAgent] Cannot drop: no player");
            return;
        }
        try {
            // Beta path: use dropOneItem() which sends the proper network packet
            if (mappings.dropOneItemMethodName() != null) {
                Method dropOneItem = player.getClass().getMethod(
                        mappings.dropOneItemMethodName());
                dropOneItem.invoke(player);
                System.out.println("[McTestAgent] Dropped item via dropOneItem()");
                return;
            }

            // Alpha path: manual decrStackSize + dropPlayerItem
            ensureInventoryFields(player);
            Object inventory = inventoryField.get(player);
            if (inventory == null) {
                System.err.println("[McTestAgent] Cannot drop: no inventory");
                return;
            }
            int currentSlot = currentItemField.getInt(inventory);

            // Call fo.a(int, int) = decrStackSize -> returns ItemStack
            Method decrMethod = findMethod(inventory.getClass(),
                    mappings.sendChatMessageMethodName(), // "a" — same obfuscated name
                    new Class<?>[]{int.class, int.class}, true);
            Object droppedStack = decrMethod.invoke(inventory, currentSlot, 1);

            if (droppedStack == null) {
                System.out.println("[McTestAgent] Nothing to drop in slot " + currentSlot);
                return;
            }

            // Call eb.a(fp, boolean) = dropPlayerItem
            if (dropPlayerItemMethod == null) {
                // Find method with signature (ItemStack class, boolean)
                Class<?> stackClass = droppedStack.getClass();
                Class<?> c = player.getClass();
                while (c != null && c != Object.class) {
                    for (Method m : c.getDeclaredMethods()) {
                        if (m.getName().equals(mappings.dropPlayerItemMethodName())) {
                            Class<?>[] params = m.getParameterTypes();
                            if (params.length == 2 && params[0] == stackClass
                                    && params[1] == boolean.class) {
                                m.setAccessible(true);
                                dropPlayerItemMethod = m;
                                break;
                            }
                        }
                    }
                    if (dropPlayerItemMethod != null) break;
                    c = c.getSuperclass();
                }
                if (dropPlayerItemMethod == null) {
                    throw new RuntimeException("dropPlayerItem method not found");
                }
            }
            dropPlayerItemMethod.invoke(player, droppedStack, false);
            System.out.println("[McTestAgent] Dropped item from slot " + currentSlot);
        } catch (Exception e) {
            System.err.println("[McTestAgent] dropCurrentItem error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Open the player inventory GUI.
     * Alpha: constructs GuiInventory(IInventory, ItemStack[]) via reflection.
     * Beta: constructs GuiContainerCreative(EntityPlayer) for creative mode,
     *       or GuiInventory(EntityPlayer) for survival.
     */
    public void openInventory() {
        Object player = gameState.getPlayer();
        if (player == null) {
            System.err.println("[McTestAgent] Cannot open inventory: no player");
            return;
        }
        try {
            agentOpenedScreen = true;
            ensureDisplayGuiScreenMethod();

            // Try Beta-style first (1-arg EntityPlayer constructor),
            // fall back to Alpha-style (IInventory + ItemStack[] constructor)
            if (mappings.creativeInventoryClassName() != null
                    || mappings.guiInventoryClassName() != null) {
                if (!tryOpenBetaInventory(player)) {
                    openAlphaInventory(player);
                }
                return;
            }

            // Alpha-style: GuiInventory(IInventory, ItemStack[])
            openAlphaInventory(player);
        } catch (Exception e) {
            System.err.println("[McTestAgent] openInventory error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean tryOpenBetaInventory(Object player) throws Exception {
        String className;
        if (McTestAgent.isCreativeMode && mappings.creativeInventoryClassName() != null) {
            className = mappings.creativeInventoryClassName();
        } else {
            className = mappings.guiInventoryClassName();
        }

        debugLog("tryOpenBetaInventory: className=" + className
                + " player=" + player.getClass().getName());
        Class<?> guiClass = loadGuiClassWithFallback(className);
        debugLog("tryOpenBetaInventory: guiClass=" + guiClass.getName());
        // Find constructor that takes the player's class or a superclass.
        // Use getDeclaredConstructors to include non-public constructors (Netty versions).
        // First try 1-param (pre-1.19.3), then multi-param with defaults.
        Constructor<?> ctor = null;
        Object[] ctorArgs = null;
        for (Constructor<?> c : guiClass.getDeclaredConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            debugLog("  ctor: " + Arrays.toString(params));
            if (params.length == 1 && params[0].isAssignableFrom(player.getClass())) {
                ctor = c;
                ctorArgs = new Object[]{player};
                debugLog("  -> matched 1-param constructor");
                break;
            }
        }
        // If no 1-param constructor, try multi-param where first param is player.
        // For non-primitive params, try to resolve from world/player no-arg methods.
        if (ctor == null) {
            Object world = gameState.getWorld();
            for (Constructor<?> c : guiClass.getDeclaredConstructors()) {
                Class<?>[] params = c.getParameterTypes();
                if (params.length >= 2 && params[0].isAssignableFrom(player.getClass())) {
                    Object[] args = new Object[params.length];
                    args[0] = player;
                    boolean canFill = true;
                    for (int i = 1; i < params.length; i++) {
                        if (params[i] == boolean.class) args[i] = false;
                        else if (params[i] == int.class) args[i] = 0;
                        else if (params[i] == float.class) args[i] = 0f;
                        else if (params[i] == double.class) args[i] = 0d;
                        else if (params[i] == long.class) args[i] = 0L;
                        else if (!params[i].isPrimitive()) {
                            // Try to find a no-arg method on world or player returning this type
                            Object resolved = resolveParamFromSources(params[i], world, player);
                            debugLog("  resolved param[" + i + "] " + params[i].getName()
                                    + " = " + (resolved == null ? "NULL" : resolved.getClass().getName()));
                            args[i] = resolved; // may still be null if unresolvable
                        }
                        else { canFill = false; break; }
                    }
                    if (canFill) {
                        ctor = c;
                        ctorArgs = args;
                        debugLog("  -> matched " + params.length + "-param constructor");
                        System.out.println("[McTestAgent] Using " + params.length
                                + "-param constructor for " + className);
                        break;
                    }
                }
            }
        }
        if (ctor == null) {
            debugLog("tryOpenBetaInventory: No suitable constructor for " + className);
            System.out.println("[McTestAgent] No suitable constructor for " + className);
            return false; // No Beta-style constructor; caller should try Alpha-style
        }
        ctor.setAccessible(true);
        try {
            Object guiInv = ctor.newInstance(ctorArgs);
            debugLog("tryOpenBetaInventory: constructor succeeded, invoking displayGuiScreen");
            displayGuiScreenMethod.invoke(gameState.getMinecraftInstance(), guiInv);
            System.out.println("[McTestAgent] Opened " + className + " inventory GUI");
            return true;
        } catch (Exception e) {
            debugLog("tryOpenBetaInventory: constructor/display threw: " + e
                    + (e.getCause() != null ? " cause=" + e.getCause() : ""));
            // Fall back to other approaches
            return false;
        }
    }

    /**
     * Try to resolve a constructor parameter type from game objects (world, player).
     * Searches for no-arg methods on the given sources that return the target type.
     */
    private Object resolveParamFromSources(Class<?> targetType, Object... sources) {
        for (Object source : sources) {
            if (source == null) continue;
            Class<?> c = source.getClass();
            while (c != null && c != Object.class) {
                for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                    if (m.getParameterCount() == 0
                            && targetType.isAssignableFrom(m.getReturnType())) {
                        try {
                            m.setAccessible(true);
                            Object val = m.invoke(source);
                            if (val != null) {
                                System.out.println("[McTestAgent] Resolved " + targetType.getName()
                                        + " from " + c.getName() + "." + m.getName() + "()");
                                return val;
                            }
                        } catch (Exception ignored) {}
                    }
                }
                c = c.getSuperclass();
            }
        }
        // Try static no-arg factory methods on the target type itself.
        // E.g. CreativeModeTabs.a() returns default tab set.
        for (java.lang.reflect.Method m : targetType.getDeclaredMethods()) {
            if (m.getParameterCount() == 0
                    && java.lang.reflect.Modifier.isStatic(m.getModifiers())
                    && targetType.isAssignableFrom(m.getReturnType())) {
                try {
                    m.setAccessible(true);
                    Object val = m.invoke(null);
                    if (val != null) {
                        System.out.println("[McTestAgent] Resolved " + targetType.getName()
                                + " via static " + targetType.getName() + "." + m.getName() + "()");
                        return val;
                    }
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    /**
     * Load a GUI class by name, falling back to nearby obfuscated names if the
     * exact name doesn't have a suitable Player constructor (obfuscation shift).
     */
    private Class<?> loadGuiClassWithFallback(String className) throws ClassNotFoundException {
        debugLog("loadGuiClassWithFallback: trying " + className);
        // Try exact name first — verify it has a Player-compatible constructor
        try {
            Class<?> cls = Class.forName(className);
            if (hasPlayerConstructor(cls)) {
                debugLog("loadGuiClassWithFallback: exact match " + className);
                return cls;
            }
            debugLog(className + " loaded but has no Player constructor, searching nearby...");
            System.out.println("[McTestAgent] " + className
                    + " loaded but has no Player constructor, searching nearby...");
        } catch (ClassNotFoundException ignored) {
            debugLog(className + " not found, searching nearby...");
            System.out.println("[McTestAgent] " + className
                    + " not found, searching nearby...");
        }

        // Get the Screen base class from displayGuiScreenMethod for validation
        Class<?> screenClass = null;
        if (displayGuiScreenMethod != null) {
            screenClass = displayGuiScreenMethod.getParameterTypes()[0];
        }
        debugLog("loadGuiClassWithFallback: screenClass="
                + (screenClass != null ? screenClass.getName() : "null"));

        // Search nearby obfuscated class names
        Class<?> bestCandidate = null;
        // Try shifting the last character ±1 through ±10
        char[] chars = className.toCharArray();
        char origLast = chars[chars.length - 1];
        for (int delta = 1; delta <= 10; delta++) {
            for (int sign : new int[]{1, -1}) {
                char c = (char) (origLast + sign * delta);
                if (!Character.isLetterOrDigit(c)) continue;
                chars[chars.length - 1] = c;
                String candidate = new String(chars);
                try {
                    Class<?> cls = Class.forName(candidate);
                    if (!hasPlayerConstructor(cls)) continue;
                    if (screenClass == null || screenClass.isAssignableFrom(cls)) {
                        debugLog("loadGuiClassWithFallback: " + className + " -> " + candidate);
                        return cls;
                    }
                    // hasCtor=true but screenClass mismatch — might be wrong displayGuiScreen
                    if (bestCandidate == null && hasScreenFields(cls)) {
                        bestCandidate = cls;
                        debugLog("  candidate " + candidate
                                + ": hasCtor+screenFields, deferring (screenClass mismatch)");
                    }
                } catch (ClassNotFoundException ignored) {}
            }
        }

        // Try shifting the second-to-last character ±1 through ±5
        chars = className.toCharArray();
        char origSecondLast = chars[chars.length - 2];
        for (int delta = 1; delta <= 5; delta++) {
            for (int sign : new int[]{1, -1}) {
                char c2 = (char) (origSecondLast + sign * delta);
                if (!Character.isLetter(c2)) continue;
                chars[chars.length - 2] = c2;
                chars[chars.length - 1] = origLast; // reset last char
                String candidate = new String(chars);
                try {
                    Class<?> cls = Class.forName(candidate);
                    if (!hasPlayerConstructor(cls)) continue;
                    if (screenClass == null || screenClass.isAssignableFrom(cls)) {
                        debugLog("loadGuiClassWithFallback: " + className + " -> " + candidate);
                        return cls;
                    }
                    if (bestCandidate == null && hasScreenFields(cls)) {
                        bestCandidate = cls;
                    }
                } catch (ClassNotFoundException ignored) {}
            }
        }

        // If we found a candidate with Player constructor + screen fields but
        // the screenClass was wrong (displayGuiScreenMethod picked wrong overload),
        // re-resolve displayGuiScreenMethod using the candidate's actual Screen superclass.
        if (bestCandidate != null) {
            Class<?> realScreenClass = findScreenAncestor(bestCandidate);
            if (realScreenClass != null) {
                debugLog("loadGuiClassWithFallback: re-resolving displayGuiScreen for "
                        + realScreenClass.getName());
                reResolveDisplayGuiScreenMethod(realScreenClass);
            }
            debugLog("loadGuiClassWithFallback: " + className + " -> " + bestCandidate.getName());
            return bestCandidate;
        }

        // Last resort: fall back to exact class (will likely fail at constructor resolution)
        return Class.forName(className);
    }

    private boolean hasPlayerConstructor(Class<?> cls) {
        Object player = gameState.getPlayer();
        if (player == null) return false;
        for (Constructor<?> c : cls.getDeclaredConstructors()) {
            Class<?>[] params = c.getParameterTypes();
            if (params.length >= 1 && params[0].isAssignableFrom(player.getClass())) {
                return true;
            }
        }
        return false;
    }

    /** Check if a class or any ancestor has Screen-like fields (width/height). */
    private boolean hasScreenFields(Class<?> cls) {
        String wf = mappings.guiScreenWidthFieldName();
        if (wf == null) return false;
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                c.getDeclaredField(wf);
                return true;
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        return false;
    }

    /**
     * Walk up the class hierarchy to find the ancestor that declares Screen fields.
     * Returns the highest ancestor (closest to Object) that has width/height fields.
     */
    private Class<?> findScreenAncestor(Class<?> cls) {
        String wf = mappings.guiScreenWidthFieldName();
        if (wf == null) return null;
        Class<?> screenClass = null;
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            try {
                c.getDeclaredField(wf);
                screenClass = c; // keep going up to find the highest
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        return screenClass;
    }

    /**
     * Re-resolve displayGuiScreenMethod using the correct Screen class.
     * Called when the original resolution picked the wrong overload of the method.
     */
    private void reResolveDisplayGuiScreenMethod(Class<?> screenClass) {
        String methodName = mappings.displayGuiScreenMethodName();
        Class<?> c = gameState.getMinecraftInstance().getClass();
        while (c != null && c != Object.class) {
            try {
                Method m = c.getDeclaredMethod(methodName, screenClass);
                m.setAccessible(true);
                displayGuiScreenMethod = m;
                debugLog("reResolveDisplayGuiScreenMethod: found " + methodName
                        + "(" + screenClass.getName() + ") on " + c.getName());
                return;
            } catch (NoSuchMethodException ignored) {}
            c = c.getSuperclass();
        }
        debugLog("reResolveDisplayGuiScreenMethod: could not find " + methodName
                + "(" + screenClass.getName() + ")");
    }

    private String getSuperChain(Class<?> cls) {
        StringBuilder sb = new StringBuilder();
        Class<?> c = cls;
        while (c != null && c != Object.class) {
            if (sb.length() > 0) sb.append(" -> ");
            sb.append(c.getName());
            c = c.getSuperclass();
        }
        return sb.toString();
    }

    private void openAlphaInventory(Object player) throws Exception {
        ensureInventoryFields(player);
        Object inventory = inventoryField.get(player);
        if (inventory == null) {
            System.err.println("[McTestAgent] Cannot open inventory: inventory null");
            return;
        }

        // Get the InventoryPlayer's interface (hi) and craftingInventory (fp[])
        if (craftingInventoryField == null) {
            Class<?> c = inventory.getClass();
            while (c != null && c != Object.class) {
                try {
                    Field f = c.getDeclaredField(mappings.craftingInventoryFieldName());
                    f.setAccessible(true);
                    craftingInventoryField = f;
                    break;
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
            if (craftingInventoryField == null) {
                throw new RuntimeException("craftingInventory field not found");
            }
        }
        Object craftingInv = craftingInventoryField.get(inventory);

        // Create GuiInventory: className(hi, fp[]) where hi is an interface of InventoryPlayer
        if (guiInventoryConstructor == null) {
            String guiClassName = mappings.guiInventoryClassName() != null
                    ? mappings.guiInventoryClassName() : "ne";
            Class<?> neClass = Class.forName(guiClassName);
            Class<?> hiClass = null;
            for (Class<?> iface : inventory.getClass().getInterfaces()) {
                hiClass = iface;
                break;
            }
            if (hiClass == null) {
                Class<?> c = inventory.getClass().getSuperclass();
                while (c != null && c != Object.class) {
                    if (c.getInterfaces().length > 0) {
                        hiClass = c.getInterfaces()[0];
                        break;
                    }
                    c = c.getSuperclass();
                }
            }
            if (hiClass == null) {
                throw new RuntimeException("No interface found on inventory class "
                        + inventory.getClass().getName());
            }
            Class<?> arrayClass = craftingInv.getClass();
            guiInventoryConstructor = neClass.getConstructor(hiClass, arrayClass);
            guiInventoryConstructor.setAccessible(true);
        }
        Object guiInv = guiInventoryConstructor.newInstance(inventory, craftingInv);
        displayGuiScreenMethod.invoke(gameState.getMinecraftInstance(), guiInv);
        System.out.println("[McTestAgent] Opened inventory GUI");
    }

    /**
     * Close any open screen. If the mappings provide a closeContainerMethodName,
     * call the player's close-container method (which sends CloseWindowPacket to the
     * server and then calls displayGuiScreen(null)). Otherwise fall back to
     * calling displayGuiScreen(null) directly.
     */
    public void closeScreen() {
        try {
            agentOpenedScreen = false;
            String closeMethodName = mappings.closeContainerMethodName();
            if (closeMethodName != null) {
                Object player = gameState.getPlayer();
                if (player != null) {
                    Method closeMethod = player.getClass().getMethod(closeMethodName);
                    closeMethod.invoke(player);
                    System.out.println("[McTestAgent] Closed screen via player." + closeMethodName + "()");
                    return;
                }
            }
            ensureDisplayGuiScreenMethod();
            displayGuiScreenMethod.invoke(gameState.getMinecraftInstance(), (Object) null);
            System.out.println("[McTestAgent] Closed screen via displayGuiScreen(null)");
        } catch (Exception e) {
            System.err.println("[McTestAgent] closeScreen error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Click on an inventory slot by window slot index (0-44).
     * Computes pixel coordinates from slot layout and calls GuiContainer.mouseClicked.
     *
     * @param windowSlot slot index in the inventory window
     * @param button     0=left click, 1=right click
     */
    public void clickInventorySlot(int windowSlot, int button) {
        if (windowSlot < 0 || windowSlot >= SLOT_POSITIONS.length) {
            System.err.println("[McTestAgent] Invalid slot: " + windowSlot);
            return;
        }
        Object screen = gameState.getCurrentScreen();
        if (screen == null) {
            System.err.println("[McTestAgent] Cannot click slot: no screen open");
            return;
        }
        try {
            ensureGuiContainerFields(screen);

            int scaledWidth = guiScreenWidthField.getInt(screen);
            int scaledHeight = guiScreenHeightField.getInt(screen);

            int containerWidth = 176;
            int containerHeight = 166;
            int guiLeft = (scaledWidth - containerWidth) / 2;
            int guiTop = (scaledHeight - containerHeight) / 2;

            int[] slotPos = SLOT_POSITIONS[windowSlot];
            int mouseX = guiLeft + slotPos[0] + 8; // center of slot
            int mouseY = guiTop + slotPos[1] + 8;

            guiContainerMouseClickedMethod.invoke(screen, mouseX, mouseY, button);
            System.out.println("[McTestAgent] Clicked slot " + windowSlot
                    + " at (" + mouseX + "," + mouseY + ") button=" + button);
        } catch (Exception e) {
            System.err.println("[McTestAgent] clickInventorySlot error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Click at a container-relative position with specified container dimensions.
     */
    private void clickAtContainerPosition(int containerX, int containerY,
            int containerW, int containerH, int button) {
        Object screen = gameState.getCurrentScreen();
        if (screen == null) return;
        try {
            ensureGuiContainerFields(screen);
            int scaledWidth = guiScreenWidthField.getInt(screen);
            int scaledHeight = guiScreenHeightField.getInt(screen);
            int guiLeft = (scaledWidth - containerW) / 2;
            int guiTop = (scaledHeight - containerH) / 2;
            int mouseX = guiLeft + containerX + 8; // center of slot
            int mouseY = guiTop + containerY + 8;
            guiContainerMouseClickedMethod.invoke(screen, mouseX, mouseY, button);
        } catch (Exception e) {
            System.err.println("[McTestAgent] clickAtContainerPosition error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Click a creative inventory grid slot (8 columns, rows scroll).
     *
     * @param gridIndex slot index in the creative grid (0-based)
     * @param button    0=left click, 1=right click
     */
    public void clickCreativeGridSlot(int gridIndex, int button) {
        int col = gridIndex % 8;
        int row = gridIndex / 8;
        int containerX = 8 + col * 18;
        int containerY = 18 + row * 18;
        clickAtContainerPosition(containerX, containerY, 176, 208, button);
        System.out.println("[McTestAgent] Clicked creative grid slot " + gridIndex
                + " (col=" + col + ",row=" + row + ") button=" + button);
    }

    /**
     * Click a creative inventory hotbar slot (0-8).
     *
     * @param hotbarIndex hotbar slot index (0-8)
     * @param button      0=left click, 1=right click
     */
    public void clickCreativeHotbar(int hotbarIndex, int button) {
        int containerX = 8 + hotbarIndex * 18;
        int containerY = 184;
        clickAtContainerPosition(containerX, containerY, 176, 208, button);
        System.out.println("[McTestAgent] Clicked creative hotbar slot " + hotbarIndex
                + " button=" + button);
    }

    /**
     * Click outside the inventory to drop items from cursor.
     * Uses coordinates outside the container bounds.
     *
     * @param button 0=left (drop all), 1=right (drop one)
     */
    public void clickOutsideInventory(int button) {
        Object screen = gameState.getCurrentScreen();
        if (screen == null) {
            System.err.println("[McTestAgent] Cannot click outside: no screen open");
            return;
        }
        try {
            ensureGuiContainerFields(screen);
            // Click at (0, 0) which is outside the container
            guiContainerMouseClickedMethod.invoke(screen, 0, 0, button);
            System.out.println("[McTestAgent] Clicked outside inventory button=" + button);
        } catch (Exception e) {
            System.err.println("[McTestAgent] clickOutsideInventory error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void ensureInventoryFields(Object player) throws Exception {
        if (inventoryField == null) {
            Class<?> c = player.getClass();
            while (c != null && c != Object.class) {
                try {
                    Field f = c.getDeclaredField(mappings.inventoryFieldName());
                    f.setAccessible(true);
                    inventoryField = f;
                    break;
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
            if (inventoryField == null) {
                throw new RuntimeException("inventory field not found");
            }
        }
        if (currentItemField == null) {
            Object inventory = inventoryField.get(player);
            if (inventory != null) {
                Class<?> c = inventory.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Field f = c.getDeclaredField(mappings.currentItemFieldName());
                        f.setAccessible(true);
                        currentItemField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                    c = c.getSuperclass();
                }
            }
        }
    }

    private void ensureDisplayGuiScreenMethod() throws Exception {
        if (displayGuiScreenMethod != null) return;
        String methodName = mappings.displayGuiScreenMethodName();
        // Try exact Screen class match first
        try {
            Class<?> guiScreenClass = Class.forName(mappings.guiScreenClassName());
            Class<?> c = gameState.getMinecraftInstance().getClass();
            while (c != null && c != Object.class) {
                try {
                    Method m = c.getDeclaredMethod(methodName, guiScreenClass);
                    m.setAccessible(true);
                    displayGuiScreenMethod = m;
                    return;
                } catch (NoSuchMethodException ignored) {}
                c = c.getSuperclass();
            }
        } catch (ClassNotFoundException ignored) {}
        // Fallback: find a single-param void method with the right name where the
        // param type is not a primitive (handles obfuscation mismatch from auto-detect)
        Class<?> c = gameState.getMinecraftInstance().getClass();
        while (c != null && c != Object.class) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(methodName)
                        && m.getReturnType() == void.class
                        && m.getParameterCount() == 1
                        && !m.getParameterTypes()[0].isPrimitive()) {
                    // Verify param type looks like a Screen class (has width/height fields)
                    Class<?> paramType = m.getParameterTypes()[0];
                    try {
                        paramType.getDeclaredField(mappings.guiScreenWidthFieldName());
                        m.setAccessible(true);
                        displayGuiScreenMethod = m;
                        System.out.println("[McTestAgent] displayGuiScreen: resolved via fallback "
                                + c.getName() + "." + methodName + "(" + paramType.getName() + ")");
                        return;
                    } catch (NoSuchFieldException ignored2) {}
                }
            }
            c = c.getSuperclass();
        }
        throw new RuntimeException("displayGuiScreen method not found");
    }

    private void ensureGuiContainerFields(Object screen) throws Exception {
        if (guiContainerMouseClickedMethod == null) {
            // ex.a(int, int, int) = GuiContainer.mouseClicked
            Class<?> c = screen.getClass();
            while (c != null && c != Object.class) {
                try {
                    Method m = c.getDeclaredMethod("a",
                            int.class, int.class, int.class);
                    m.setAccessible(true);
                    guiContainerMouseClickedMethod = m;
                    break;
                } catch (NoSuchMethodException ignored) {}
                c = c.getSuperclass();
            }
            if (guiContainerMouseClickedMethod == null) {
                throw new RuntimeException("mouseClicked method not found on " + screen.getClass().getName());
            }
        }
        if (guiScreenWidthField == null) {
            String wName = mappings.guiScreenWidthFieldName();
            String hName = mappings.guiScreenHeightFieldName();
            Class<?> c = screen.getClass();
            while (c != null && c != Object.class) {
                try {
                    Field fw = c.getDeclaredField(wName);
                    if (fw.getType() == int.class) {
                        fw.setAccessible(true);
                        guiScreenWidthField = fw;
                        Field fh = c.getDeclaredField(hName);
                        fh.setAccessible(true);
                        guiScreenHeightField = fh;
                        break;
                    }
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
            if (guiScreenWidthField == null) {
                throw new RuntimeException("GuiScreen width/height fields (" + wName + "/" + hName + ") not found");
            }
        }
    }

    /**
     * Find a method by name and parameter types, walking the class hierarchy.
     */
    private Method findMethod(Class<?> clazz, String name, Class<?>[] paramTypes,
                              boolean checkReturnNonVoid) throws Exception {
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                Method m = c.getDeclaredMethod(name, paramTypes);
                if (!checkReturnNonVoid || m.getReturnType() != void.class) {
                    m.setAccessible(true);
                    return m;
                }
            } catch (NoSuchMethodException ignored) {}
            c = c.getSuperclass();
        }
        throw new RuntimeException("Method " + name + " not found on " + clazz.getName());
    }
}
