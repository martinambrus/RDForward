package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.mappings.FieldMappings;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Reflection-based reader for Minecraft client game state.
 * Caches Field handles after first lookup for performance.
 * Includes a type-scan fallback that finds fields by their Java type
 * if the exact SRG name doesn't match (SRG names may differ slightly
 * between Alpha sub-versions).
 */
public class GameState {

    private final FieldMappings mappings;
    private final Object minecraftInstance;

    // Cached field handles (resolved lazily)
    private Field playerField;
    private Field worldField;
    private Field serverHostField;
    private Field serverPortField;
    private Field displayObjectField; // Window sub-object on Minecraft (1.14+)
    private Field displayWidthField;
    private Field displayHeightField;

    // Entity position fields (resolved from the player object's class hierarchy)
    private Field posXField;
    private Field posYField;
    private Field posZField;

    // Phase 2: rotation, ground, inventory, world, mouse fields
    private Field yawField;
    private Field pitchField;
    private Field onGroundField;
    private Field inventoryField;
    private Field mainInventoryField;
    private Field currentItemField;
    private Field itemIdField;
    private Field stackSizeField;
    private Field mouseGrabbedField;
    private Field mouseHelperField;
    private Method getBlockIdMethod;
    private boolean getBlockIdUsesBlockPos;
    private Constructor<?> blockPosConstructor;
    private Method blockToIdMethod;
    private Method stateGetBlockMethod;
    private Method itemToIdMethod;
    private Method chatTextMethod;

    // Phase 3: screen, chat, cursor fields
    private Field currentScreenField;
    private Field ingameGuiField;
    private Field chatGuiField; // GuiNewChat sub-object on GuiIngame (1.3.1+)
    private Field chatLinesField;
    private Field chatLineTextField;
    private Field cursorItemField;
    private Field craftingInventoryField;

    public GameState(FieldMappings mappings, Object minecraftInstance) {
        this.mappings = mappings;
        this.minecraftInstance = minecraftInstance;
    }

    public Object getMinecraftInstance() {
        return minecraftInstance;
    }

    public void setUsername(String name) {
        if (mappings.sessionFieldName() == null) {
            System.out.println("[McTestAgent] Skipping setUsername: no session field (RubyDung)");
            return;
        }
        try {
            Field sessionField = resolveField(minecraftInstance.getClass(),
                    mappings.sessionFieldName(), null);
            Object session = sessionField.get(minecraftInstance);
            if (session != null) {
                Field usernameField = resolveField(session.getClass(),
                        mappings.sessionUsernameFieldName(), String.class);
                usernameField.set(session, name);
                System.out.println("[McTestAgent] Username set to: " + name);
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to set username: " + e.getMessage());
        }
    }

    public Object getPlayer() {
        try {
            if (playerField == null) {
                playerField = resolveField(minecraftInstance.getClass(),
                        mappings.playerFieldName(), null);
            }
            return playerField.get(minecraftInstance);
        } catch (Exception e) {
            return null;
        }
    }

    public Object getWorld() {
        try {
            if (worldField == null) {
                worldField = resolveField(minecraftInstance.getClass(),
                        mappings.worldFieldName(), null);
            }
            return worldField.get(minecraftInstance);
        } catch (Exception e) {
            return null;
        }
    }

    public void setServerHost(String host) {
        try {
            if (serverHostField == null) {
                // No type-scan fallback for String/int — too many fields match.
                // The field name must be exact.
                serverHostField = resolveField(minecraftInstance.getClass(),
                        mappings.serverHostFieldName(), null);
            }
            serverHostField.set(minecraftInstance, host);
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to set serverHost: " + e.getMessage());
        }
    }

    public void setServerPort(int port) {
        try {
            if (serverPortField == null) {
                serverPortField = resolveField(minecraftInstance.getClass(),
                        mappings.serverPortFieldName(), null);
            }
            serverPortField.setInt(minecraftInstance, port);
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to set serverPort: " + e.getMessage());
        }
    }

    public int getDisplayWidth() {
        try {
            Object target = getDisplayObject();
            if (displayWidthField == null) {
                displayWidthField = resolveField(target.getClass(),
                        mappings.displayWidthFieldName(), null);
            }
            return displayWidthField.getInt(target);
        } catch (Exception e) {
            return 854; // default
        }
    }

    public int getDisplayHeight() {
        try {
            Object target = getDisplayObject();
            if (displayHeightField == null) {
                displayHeightField = resolveField(target.getClass(),
                        mappings.displayHeightFieldName(), null);
            }
            return displayHeightField.getInt(target);
        } catch (Exception e) {
            return 480; // default
        }
    }

    /** Returns the Window sub-object (1.14+) or the Minecraft instance itself. */
    private Object getDisplayObject() throws Exception {
        if (mappings.displayObjectFieldName() == null) {
            return minecraftInstance;
        }
        if (displayObjectField == null) {
            displayObjectField = resolveField(minecraftInstance.getClass(),
                    mappings.displayObjectFieldName(), null);
        }
        return displayObjectField.get(minecraftInstance);
    }

    /**
     * Returns [x, y, z] of the player, or null if player is not available.
     */
    public double[] getPlayerPosition() {
        Object player = getPlayer();
        if (player == null) return null;
        try {
            if (posXField == null) {
                posXField = resolveField(player.getClass(),
                        mappings.posXFieldName(), double.class);
                posYField = resolveField(player.getClass(),
                        mappings.posYFieldName(), double.class);
                posZField = resolveField(player.getClass(),
                        mappings.posZFieldName(), double.class);
                System.out.println("[McTestAgent] posY field resolved: "
                        + posYField.getDeclaringClass().getName() + "." + posYField.getName()
                        + " type=" + posYField.getType().getName()
                        + " posYIsFeetLevel=" + mappings.posYIsFeetLevel());
            }
            double y = posYField.getDouble(player);
            if (mappings.posYIsFeetLevel()) {
                y += (double) 1.62f; // normalize feet → eye-level
            }
            return new double[]{
                posXField.getDouble(player),
                y,
                posZField.getDouble(player)
            };
        } catch (Exception e) {
            return null;
        }
    }

    public double getRawPosY() {
        Object player = getPlayer();
        if (player == null) return Double.NaN;
        try {
            if (posYField == null) getPlayerPosition();
            return posYField.getDouble(player);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    /**
     * Debug: returns "oldY=... vec3Y=..." showing both the old position field
     * and the actual Vec3d position. For diagnosing teleport reset issues.
     */
    public String getPositionDebug() {
        Object player = getPlayer();
        if (player == null) return "no_player";
        try {
            if (posYField == null) getPlayerPosition();
            double oldY = posYField.getDouble(player);
            double posX = posXField.getDouble(player);
            double posZ = posZField.getDouble(player);
            // Try to find the Vec3d position field by scanning patterns
            String vec3Info = "not_found";
            double bestErr = Double.MAX_VALUE;
            String[][] patterns = {{"b","c","d"}, {"c","d","e"}, {"d","e","f"}, {"g","h","i"}};
            Class<?> c = player.getClass();
            while (c != null && c != Object.class) {
                for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                    if (f.getType().isPrimitive() || f.getType() == String.class) continue;
                    if (f.getType().isArray()) continue;
                    f.setAccessible(true);
                    Object val = f.get(player);
                    if (val == null) continue;
                    for (String[] pat : patterns) {
                        try {
                            java.lang.reflect.Field fb = val.getClass().getDeclaredField(pat[0]);
                            java.lang.reflect.Field fc = val.getClass().getDeclaredField(pat[1]);
                            java.lang.reflect.Field fd = val.getClass().getDeclaredField(pat[2]);
                            if (fb.getType() == double.class && fc.getType() == double.class
                                    && fd.getType() == double.class) {
                                fb.setAccessible(true); fc.setAccessible(true); fd.setAccessible(true);
                                double[] vals = {fb.getDouble(val), fc.getDouble(val), fd.getDouble(val)};
                                // Try all permutations to find best X/Y/Z match
                                int[][] perms = {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
                                for (int[] perm : perms) {
                                    double err = Math.abs(vals[perm[0]] - posX)
                                            + Math.abs(vals[perm[1]] - oldY)
                                            + Math.abs(vals[perm[2]] - posZ);
                                    // Count non-static double fields
                                    int dblCount = 0;
                                    for (java.lang.reflect.Field sf : val.getClass().getDeclaredFields()) {
                                        if (!java.lang.reflect.Modifier.isStatic(sf.getModifiers())
                                                && sf.getType() == double.class) dblCount++;
                                    }
                                    // Prefer 3-field (Vec3) over 6-field (AABB)
                                    double adjustedErr = err + (dblCount > 3 ? 10.0 : 0.0);
                                    if (adjustedErr < bestErr) {
                                        bestErr = adjustedErr;
                                        vec3Info = String.format("%.2f,%.2f,%.2f fld=%s@%s",
                                                vals[perm[1]], vals[perm[0]], vals[perm[2]],
                                                f.getName(), c.getSimpleName());
                                    }
                                }
                            }
                        } catch (NoSuchFieldException ignored) {}
                    }
                }
                c = c.getSuperclass();
            }
            return String.format("oldY=%.2f vec3=%s", oldY, vec3Info);
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    private java.lang.reflect.Method setPositionMethod;
    private java.lang.reflect.Field vec3PosField;
    private java.lang.reflect.Field vec3XField;
    private java.lang.reflect.Field vec3YField;
    // deltaMovement Vec3 field and its Y sub-field (for velocity-based void fall)
    private java.lang.reflect.Field deltaMovementField;
    private java.lang.reflect.Field deltaMovementYField;
    private java.lang.reflect.Field vec3ZField;
    // Constructor arg order: vec3CtorOrder[0..2] = 0 for X, 1 for Y, 2 for Z
    // Maps constructor parameter index to coordinate type
    private int[] vec3CtorOrder;

    /**
     * Force-sets the player's position by directly writing to both the Vec3d
     * position field AND the old position fields. This bypasses any method-level
     * guards (like world type checks) that might prevent setPosition() from working.
     * Also patches the bounding box and sets onGround=false.
     *
     * @param x eye-level X
     * @param y eye-level Y
     * @param z eye-level Z
     */
    public boolean forcePosition(double x, double y, double z) {
        Object player = getPlayer();
        if (player == null) return false;
        double storageY = mappings.posYIsFeetLevel() ? y - (double) 1.62f : y;
        debugForcePos("forcePosition x=" + x + " y=" + y + " storageY=" + storageY + " z=" + z);
        try {
            if (posYField == null) getPlayerPosition();

            // 1. Find the Vec3d position field if not yet found
            if (vec3PosField == null) {
                double posX = posXField.getDouble(player);
                double posY = posYField.getDouble(player);
                double posZ = posZField.getDouble(player);
                // Track best candidate: prefer lower dblCount (3-field Vec3 over 6-field extended Vec3)
                // but accept higher counts if no better candidate exists.
                double bestMatchErr = Double.MAX_VALUE;
                int bestDblCount = Integer.MAX_VALUE;
                Class<?> c = player.getClass();
                while (c != null && c != Object.class) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                        if (f.getType().isPrimitive() || f.getType() == String.class) continue;
                        if (f.getType().isArray()) continue;
                        f.setAccessible(true);
                        Object val = f.get(player);
                        if (val == null) continue;
                        // Vec3d field names vary by version:
                        // 1.17-1.18: x=b, y=c, z=d
                        // 1.19+:     x=c, y=d, z=e
                        // 1.21.5+:   x=d, y=e, z=f (fgc class)
                        String[][] vec3Patterns = {{"b","c","d"}, {"c","d","e"}, {"d","e","f"}, {"g","h","i"}};
                        for (String[] pat : vec3Patterns) {
                            try {
                                java.lang.reflect.Field fb = val.getClass().getDeclaredField(pat[0]);
                                java.lang.reflect.Field fc = val.getClass().getDeclaredField(pat[1]);
                                java.lang.reflect.Field fd = val.getClass().getDeclaredField(pat[2]);
                                if (fb.getType() == double.class && fc.getType() == double.class
                                        && fd.getType() == double.class) {
                                    fb.setAccessible(true); fc.setAccessible(true); fd.setAccessible(true);
                                    int dblCount = 0;
                                    for (java.lang.reflect.Field df : val.getClass().getDeclaredFields()) {
                                        if (!java.lang.reflect.Modifier.isStatic(df.getModifiers())
                                                && df.getType() == double.class) {
                                            dblCount++;
                                        }
                                    }
                                    java.lang.reflect.Field[] dblFields = {fb, fc, fd};
                                    double[] vals = {fb.getDouble(val), fc.getDouble(val), fd.getDouble(val)};
                                    // Try all 6 permutations to find best X/Y/Z assignment
                                    int[][] perms = {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
                                    double minErr = Double.MAX_VALUE;
                                    int bestPerm = 0;
                                    for (int p = 0; p < perms.length; p++) {
                                        double err = Math.abs(vals[perms[p][0]] - posX)
                                                + Math.abs(vals[perms[p][1]] - posY)
                                                + Math.abs(vals[perms[p][2]] - posZ);
                                        if (err < minErr) { minErr = err; bestPerm = p; }
                                    }
                                    debugForcePos("  candidate " + f.getName() + "@" + val.getClass().getSimpleName()
                                            + " pat=" + pat[0] + "," + pat[1] + "," + pat[2]
                                            + " dblCount=" + dblCount + " err=" + String.format("%.2f", minErr));
                                    // Accept if error is reasonable AND (better dblCount OR better error)
                                    if (minErr < 5.0
                                            && (dblCount < bestDblCount
                                                || (dblCount == bestDblCount && minErr < bestMatchErr))) {
                                        bestMatchErr = minErr;
                                        bestDblCount = dblCount;
                                        vec3PosField = f;
                                        vec3XField = dblFields[perms[bestPerm][0]];
                                        vec3YField = dblFields[perms[bestPerm][1]];
                                        vec3ZField = dblFields[perms[bestPerm][2]];
                                        vec3CtorOrder = new int[3];
                                        for (int i = 0; i < 3; i++) {
                                            if (dblFields[i] == vec3XField) vec3CtorOrder[i] = 0;
                                            else if (dblFields[i] == vec3YField) vec3CtorOrder[i] = 1;
                                            else vec3CtorOrder[i] = 2;
                                        }
                                        debugForcePos("    -> accepted: X=" + vec3XField.getName()
                                                + " Y=" + vec3YField.getName()
                                                + " Z=" + vec3ZField.getName());
                                    }
                                }
                            } catch (NoSuchFieldException ignored) {}
                        }
                    }
                    if (vec3PosField != null && bestDblCount <= 4) break; // found ideal Vec3, stop searching
                    c = c.getSuperclass();
                }
                if (vec3PosField != null) {
                    debugForcePos("SELECTED: " + vec3PosField.getDeclaringClass().getSimpleName()
                            + "." + vec3PosField.getName()
                            + " valType=" + vec3PosField.get(player).getClass().getSimpleName()
                            + " X=" + vec3XField.getName()
                            + " Y=" + vec3YField.getName()
                            + " Z=" + vec3ZField.getName()
                            + " dblCount=" + bestDblCount);
                }
            }

            // 2. Write to Vec3d position field
            debugForcePos("vec3PosField=" + (vec3PosField != null ? vec3PosField.getName() : "null")
                    + " vec3CtorOrder=" + (vec3CtorOrder != null
                        ? vec3CtorOrder[0] + "," + vec3CtorOrder[1] + "," + vec3CtorOrder[2] : "null"));
            if (vec3PosField != null) {
                Object vec3 = vec3PosField.get(player);
                if (vec3 != null) {
                    java.lang.reflect.Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
                    uf.setAccessible(true);
                    sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
                    // Clone the Vec3 via Unsafe.allocateInstance (no constructor) and copy all fields.
                    // Then overwrite X/Y/Z. This handles immutable Vec3 objects that get replaced each tick.
                    Object newVec3 = unsafe.allocateInstance(vec3.getClass());
                    for (java.lang.reflect.Field ff : vec3.getClass().getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(ff.getModifiers())) continue;
                        ff.setAccessible(true);
                        long offset = unsafe.objectFieldOffset(ff);
                        if (ff.getType() == double.class) {
                            unsafe.putDouble(newVec3, offset, unsafe.getDouble(vec3, offset));
                        } else if (ff.getType() == float.class) {
                            unsafe.putFloat(newVec3, offset, unsafe.getFloat(vec3, offset));
                        } else if (ff.getType() == int.class) {
                            unsafe.putInt(newVec3, offset, unsafe.getInt(vec3, offset));
                        } else if (ff.getType() == long.class) {
                            unsafe.putLong(newVec3, offset, unsafe.getLong(vec3, offset));
                        } else if (ff.getType() == boolean.class) {
                            unsafe.putBoolean(newVec3, offset, unsafe.getBoolean(vec3, offset));
                        } else if (!ff.getType().isPrimitive()) {
                            unsafe.putObject(newVec3, offset, unsafe.getObject(vec3, offset));
                        }
                    }
                    // Overwrite X/Y/Z in the clone
                    long offX = unsafe.objectFieldOffset(vec3XField);
                    long offY = unsafe.objectFieldOffset(vec3YField);
                    long offZ = unsafe.objectFieldOffset(vec3ZField);
                    debugForcePos("  offsets: X=" + offX + "(" + vec3XField.getName() + "@" + vec3XField.getDeclaringClass().getSimpleName()
                            + ") Y=" + offY + "(" + vec3YField.getName() + "@" + vec3YField.getDeclaringClass().getSimpleName()
                            + ") Z=" + offZ + "(" + vec3ZField.getName() + "@" + vec3ZField.getDeclaringClass().getSimpleName() + ")");
                    debugForcePos("  before overwrite: X=" + unsafe.getDouble(newVec3, offX)
                            + " Y=" + unsafe.getDouble(newVec3, offY)
                            + " Z=" + unsafe.getDouble(newVec3, offZ));
                    unsafe.putDouble(newVec3, offX, x);
                    unsafe.putDouble(newVec3, offY, storageY);
                    unsafe.putDouble(newVec3, offZ, z);
                    debugForcePos("  after overwrite: X=" + unsafe.getDouble(newVec3, offX)
                            + " Y=" + unsafe.getDouble(newVec3, offY)
                            + " Z=" + unsafe.getDouble(newVec3, offZ));
                    // Replace the field on the entity with the clone
                    long posFldOff = unsafe.objectFieldOffset(vec3PosField);
                    unsafe.putObject(player, posFldOff, newVec3);
                    Object readBack = unsafe.getObject(player, posFldOff);
                    debugForcePos("wrote Vec3 via clone+Unsafe: replaced=" + (readBack == newVec3));
                }
            }

            // 3. Write to old position fields
            debugForcePos("writing posY=" + storageY + " to " + posYField.getName()
                    + " (was " + posYField.getDouble(player) + ")");
            posXField.setDouble(player, x);
            posYField.setDouble(player, storageY);
            posZField.setDouble(player, z);
            debugForcePos("after write, posY=" + posYField.getDouble(player));

            // 4. Also try calling setPosition method (in case it does additional setup)
            // Method name varies by version: "a" (1.17/1.18), "e" (1.19+)
            if (setPositionMethod == null) {
                String[] candidates = {"e", "f", "a", "o"}; // setPos (1.19+), setPos (1.19.3+), setPos (1.17/1.18), setPosRaw
                outer:
                for (String name : candidates) {
                    Class<?> c = player.getClass();
                    while (c != null && c != Object.class) {
                        try {
                            java.lang.reflect.Method m = c.getDeclaredMethod(name,
                                    double.class, double.class, double.class);
                            if (m.getReturnType() == void.class) {
                                m.setAccessible(true);
                                setPositionMethod = m;
                                break outer;
                            }
                        } catch (NoSuchMethodException ignored) {}
                        c = c.getSuperclass();
                    }
                }
            }
            if (setPositionMethod != null) {
                try {
                    setPositionMethod.invoke(player, x, storageY, z);
                } catch (Exception ignored) {
                    // Method may fail due to world type check — that's ok,
                    // we already wrote the fields directly
                }
            }

            // 5. Patch bounding box via teleportPlayer's BB logic
            teleportPlayer(x, y, z);

            // 6. Set onGround=false
            if (onGroundField == null && mappings.onGroundFieldName() != null) {
                onGroundField = resolveField(player.getClass(),
                        mappings.onGroundFieldName(), boolean.class);
            }
            if (onGroundField != null) {
                onGroundField.setBoolean(player, false);
            }

            debugForcePos("forcePosition complete");
            return true;
        } catch (Exception e) {
            debugForcePos("forcePosition error: " + e.getMessage());
            System.err.println("[McTestAgent] forcePosition error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void debugForcePos(String msg) {
        System.out.println("[McTestAgent] " + msg);
        try {
            java.io.File dir = McTestAgent.statusDir;
            if (dir != null) {
                try (java.io.PrintWriter pw = new java.io.PrintWriter(
                        new java.io.FileWriter(new java.io.File(dir, "debug_forcepos.log"), true))) {
                    pw.println(msg);
                }
            }
        } catch (Exception ignored) {}
    }

    /**
     * Sets the player's vertical velocity to a large negative value, causing
     * the game's own physics to push the player downward. This is a fallback
     * for versions where direct position writes don't persist across ticks.
     *
     * @param downwardSpeed positive value for downward speed (e.g. 200.0)
     * @return true if velocity was successfully set
     */
    public boolean setDownwardVelocity(double downwardSpeed) {
        Object player = getPlayer();
        if (player == null) return false;
        try {
            if (posYField == null) getPlayerPosition();
            double posX = posXField.getDouble(player);
            double posY = posYField.getDouble(player);
            double posZ = posZField.getDouble(player);

            // Find deltaMovement Vec3 field (if not cached)
            if (deltaMovementField == null) {
                // First pass: dump ALL fields on the entity hierarchy to understand structure
                String[][] vec3Patterns = {{"b","c","d"}, {"c","d","e"}, {"d","e","f"}, {"g","h","i"}};
                Class<?> c = player.getClass();
                while (c != null && c != Object.class) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                        if (f.getType().isPrimitive() || f.getType() == String.class) continue;
                        if (f.getType().isArray()) continue;
                        f.setAccessible(true);
                        Object val = f.get(player);
                        if (val == null) continue;
                        // Skip if this is the position field we already found
                        if (vec3PosField != null && f.getName().equals(vec3PosField.getName())
                                && f.getDeclaringClass() == vec3PosField.getDeclaringClass()) continue;
                        for (String[] pat : vec3Patterns) {
                            try {
                                java.lang.reflect.Field fb = val.getClass().getDeclaredField(pat[0]);
                                java.lang.reflect.Field fc = val.getClass().getDeclaredField(pat[1]);
                                java.lang.reflect.Field fd = val.getClass().getDeclaredField(pat[2]);
                                if (fb.getType() == double.class && fc.getType() == double.class
                                        && fd.getType() == double.class) {
                                    fb.setAccessible(true); fc.setAccessible(true); fd.setAccessible(true);
                                    double vb = fb.getDouble(val);
                                    double vc = fc.getDouble(val);
                                    double vd = fd.getDouble(val);
                                    // deltaMovement values should be near 0 (or small gravity)
                                    // and NOT near the player's position
                                    double maxAbs = Math.max(Math.abs(vb), Math.max(Math.abs(vc), Math.abs(vd)));
                                    if (maxAbs < 5.0) {
                                        // This looks like a velocity field (small values)
                                        // Use the Y field from our position mapping
                                        deltaMovementField = f;
                                        // deltaMovement has same Vec3 structure: find Y sub-field
                                        // Use permutation matching like forcePosition
                                        if (vec3YField != null) {
                                            deltaMovementYField = val.getClass().getDeclaredField(vec3YField.getName());
                                            deltaMovementYField.setAccessible(true);
                                        }
                                        debugForcePos("deltaMovement found: " + c.getSimpleName()
                                                + "." + f.getName() + "@" + val.getClass().getSimpleName()
                                                + " values=(" + String.format("%.4f,%.4f,%.4f", vb, vc, vd) + ")"
                                                + " Y-field=" + (deltaMovementYField != null ? deltaMovementYField.getName() : "null"));
                                        break;
                                    }
                                }
                            } catch (NoSuchFieldException ignored) {}
                        }
                        if (deltaMovementField != null) break;
                    }
                    if (deltaMovementField != null) break;
                    c = c.getSuperclass();
                }
            }

            if (deltaMovementField == null || deltaMovementYField == null) {
                // Dump entity fields for diagnostics
                StringBuilder dump = new StringBuilder("deltaMovement NOT found. Entity dump:\n");
                Class<?> dc = player.getClass();
                while (dc != null && dc != Object.class) {
                    for (java.lang.reflect.Field df : dc.getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(df.getModifiers())) continue;
                        if (df.getType().isPrimitive() || df.getType().isArray()) continue;
                        try {
                            df.setAccessible(true);
                            Object dv = df.get(player);
                            if (dv == null) {
                                // Log null fields that match position Vec3 type
                                if (vec3PosField != null && df.getType().getName().equals(vec3PosField.getType().getName())) {
                                    dump.append("  NULL ").append(dc.getSimpleName()).append(".").append(df.getName())
                                            .append(" type=").append(df.getType().getSimpleName()).append("\n");
                                }
                                continue;
                            }
                            int dblCount = 0;
                            StringBuilder dblInfo = new StringBuilder();
                            for (java.lang.reflect.Field sf : dv.getClass().getDeclaredFields()) {
                                if (!java.lang.reflect.Modifier.isStatic(sf.getModifiers())
                                        && sf.getType() == double.class) {
                                    sf.setAccessible(true);
                                    dblCount++;
                                    if (dblInfo.length() > 0) dblInfo.append(",");
                                    dblInfo.append(sf.getName()).append("=")
                                            .append(String.format("%.2f", sf.getDouble(dv)));
                                }
                            }
                            if (dblCount >= 3) {
                                dump.append("  ").append(dc.getSimpleName()).append(".").append(df.getName())
                                        .append("@").append(dv.getClass().getSimpleName())
                                        .append(" [").append(dblInfo).append("]\n");
                            }
                        } catch (Exception ignored) {}
                    }
                    dc = dc.getSuperclass();
                }
                debugForcePos(dump.toString());
                return false;
            }

            // Clone deltaMovement Vec3 with large negative Y
            Object dmVec3 = deltaMovementField.get(player);
            if (dmVec3 == null) {
                debugForcePos("deltaMovement value is null");
                return false;
            }
            java.lang.reflect.Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            uf.setAccessible(true);
            sun.misc.Unsafe unsafe = (sun.misc.Unsafe) uf.get(null);
            Object newDm = unsafe.allocateInstance(dmVec3.getClass());
            for (java.lang.reflect.Field ff : dmVec3.getClass().getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(ff.getModifiers())) continue;
                ff.setAccessible(true);
                long offset = unsafe.objectFieldOffset(ff);
                if (ff.getType() == double.class) {
                    unsafe.putDouble(newDm, offset, unsafe.getDouble(dmVec3, offset));
                } else if (ff.getType() == float.class) {
                    unsafe.putFloat(newDm, offset, unsafe.getFloat(dmVec3, offset));
                } else if (ff.getType() == int.class) {
                    unsafe.putInt(newDm, offset, unsafe.getInt(dmVec3, offset));
                } else if (ff.getType() == long.class) {
                    unsafe.putLong(newDm, offset, unsafe.getLong(dmVec3, offset));
                } else if (ff.getType() == boolean.class) {
                    unsafe.putBoolean(newDm, offset, unsafe.getBoolean(dmVec3, offset));
                } else if (!ff.getType().isPrimitive()) {
                    unsafe.putObject(newDm, offset, unsafe.getObject(dmVec3, offset));
                }
            }
            // Set Y to large negative
            long yOff = unsafe.objectFieldOffset(deltaMovementYField);
            unsafe.putDouble(newDm, yOff, -downwardSpeed);
            // Replace the field
            long fldOff = unsafe.objectFieldOffset(deltaMovementField);
            unsafe.putObject(player, fldOff, newDm);
            debugForcePos("set deltaMovement Y to -" + downwardSpeed);

            // Also set onGround=false so physics applies the velocity
            if (onGroundField == null && mappings.onGroundFieldName() != null) {
                onGroundField = resolveField(player.getClass(),
                        mappings.onGroundFieldName(), boolean.class);
            }
            if (onGroundField != null) {
                onGroundField.setBoolean(player, false);
            }
            return true;
        } catch (Exception e) {
            debugForcePos("setDownwardVelocity error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Teleports the player by invoking the entity's setPosition(x, y, z) method,
     * which updates both the position fields AND the bounding box.
     * Simply writing to posX/posY/posZ is insufficient because the bounding box
     * stays at the old position, causing the physics engine to override the change.
     */
    public boolean setPlayerPosition(double x, double y, double z) {
        Object player = getPlayer();
        if (player == null) return false;
        // Callers pass eye-level Y; convert to feet for 1.8+ clients
        double storageY = mappings.posYIsFeetLevel() ? y - (double) 1.62f : y;
        try {
            if (setPositionMethod == null) {
                // Entity.setPosition is obfuscated as "a" in all Alpha/Beta versions.
                // Signature: void a(double, double, double)
                Class<?> c = player.getClass();
                while (c != null && c != Object.class) {
                    try {
                        java.lang.reflect.Method m = c.getDeclaredMethod("a",
                                double.class, double.class, double.class);
                        if (m.getReturnType() == void.class) {
                            m.setAccessible(true);
                            setPositionMethod = m;
                            System.out.println("[McTestAgent] setPosition method: "
                                    + c.getName() + "." + m.getName()
                                    + "(double,double,double)");
                            break;
                        }
                    } catch (NoSuchMethodException ignored) {}
                    c = c.getSuperclass();
                }
                if (setPositionMethod == null) {
                    System.out.println("[McTestAgent] setPosition method not found, falling back to field writes");
                    if (posYField == null) getPlayerPosition();
                    posXField.setDouble(player, x);
                    posYField.setDouble(player, storageY);
                    posZField.setDouble(player, z);
                    return true;
                }
            }
            setPositionMethod.invoke(player, x, storageY, z);
            return true;
        } catch (Exception e) {
            System.err.println("[McTestAgent] setPlayerPosition error: " + e.getMessage());
            return false;
        }
    }

    // --- Phase 2: rotation, ground, block, inventory, mouse queries ---

    public FieldMappings getMappings() {
        return mappings;
    }

    public float getYaw() {
        Object player = getPlayer();
        if (player == null) return 0f;
        try {
            if (yawField == null) {
                yawField = resolveField(player.getClass(),
                        mappings.yawFieldName(), float.class);
            }
            return yawField.getFloat(player);
        } catch (Exception e) {
            return 0f;
        }
    }

    public float getPitch() {
        Object player = getPlayer();
        if (player == null) return 0f;
        try {
            if (pitchField == null) {
                pitchField = resolveField(player.getClass(),
                        mappings.pitchFieldName(), float.class);
            }
            return pitchField.getFloat(player);
        } catch (Exception e) {
            return 0f;
        }
    }

    /**
     * Forces player position using both Entity.a() and direct field writes.
     * Entity.a() updates the bounding box (needed for some versions like 1.2.0),
     * while field writes correct any wrong method resolution (needed for 1.2.2+
     * where Entity.a() may resolve to move() instead of setPosition()).
     * Also sets onGround=true to prevent the physics engine from overriding.
     */
    /**
     * Offsets the player's X position by the given delta using direct field
     * write only (no Entity.a() call). Safe on all versions including
     * Alpha 1.2.2+ where Entity.a() resolves to move().
     */
    public void offsetPlayerX(double dx) {
        Object player = getPlayer();
        if (player == null) return;
        try {
            if (posXField == null) getPlayerPosition(); // ensure fields resolved
            double curX = posXField.getDouble(player);
            posXField.setDouble(player, curX + dx);
        } catch (Exception e) {
            System.err.println("[McTestAgent] offsetPlayerX error: " + e.getMessage());
        }
    }

    /**
     * Offsets the player's Z position by the given delta using direct field
     * write only (no Entity.a() call). Safe on all versions including
     * Alpha 1.2.2+ where Entity.a() resolves to move().
     */
    public void offsetPlayerZ(double dz) {
        Object player = getPlayer();
        if (player == null) return;
        try {
            if (posZField == null) getPlayerPosition(); // ensure fields resolved
            double curZ = posZField.getDouble(player);
            posZField.setDouble(player, curZ + dz);
        } catch (Exception e) {
            System.err.println("[McTestAgent] offsetPlayerZ error: " + e.getMessage());
        }
    }

    public boolean forcePlayerPosition(double x, double y, double z) {
        Object player = getPlayer();
        if (player == null) return false;
        double storageY = mappings.posYIsFeetLevel() ? y - (double) 1.62f : y;
        try {
            // First: call Entity.a() to update bounding box
            setPlayerPosition(x, y, z);
            // Then: direct field writes to ensure position is correct
            if (posYField == null) getPlayerPosition(); // ensure fields resolved
            posXField.setDouble(player, x);
            posYField.setDouble(player, storageY);
            posZField.setDouble(player, z);
            // Also set onGround to prevent physics override
            if (onGroundField == null && mappings.onGroundFieldName() != null) {
                onGroundField = resolveField(player.getClass(),
                        mappings.onGroundFieldName(), boolean.class);
            }
            if (onGroundField != null) {
                onGroundField.setBoolean(player, true);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[McTestAgent] forcePlayerPosition error: " + e.getMessage());
            return false;
        }
    }

    // Cached BB reflection handles
    private java.lang.reflect.Field bbField;
    private java.lang.reflect.Field bbX0, bbY0, bbZ0, bbX1, bbY1, bbZ1;

    /**
     * Teleports the player by updating position fields, bounding box fields,
     * and setting onGround=false. Unlike forcePlayerPosition, this directly
     * patches the BB so physics doesn't snap the player back.
     * The player half-width is 0.3 blocks, height 1.8 blocks, eye height 1.62f.
     */
    public boolean teleportPlayer(double x, double y, double z) {
        Object player = getPlayer();
        if (player == null) return false;
        double storageY = mappings.posYIsFeetLevel() ? y - (double) 1.62f : y;
        try {
            if (posYField == null) getPlayerPosition(); // ensure position fields resolved

            // Read current position for BB validation
            double origX = posXField.getDouble(player);

            // Set position fields
            posXField.setDouble(player, x);
            posYField.setDouble(player, storageY);
            posZField.setDouble(player, z);

            // Find and update the bounding box directly
            if (bbField == null) {
                // Scan entity class hierarchy for the BB field.
                // Validate candidates: the field whose first numeric sub-field
                // approximately equals origX - 0.3 (player half-width) is the BB.
                double expectedMinX = origX - 0.3;
                Class<?> c = player.getClass();
                outer:
                while (c != null && c != Object.class) {
                    for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                        if (f.getType().isPrimitive() || f.getType() == String.class) continue;
                        if (f.getType().isArray()) continue;
                        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                        f.setAccessible(true);
                        Object val = f.get(player);
                        if (val == null) continue;
                        // Skip java.* classes (JPMS blocks setAccessible on them in Java 17+)
                        if (val.getClass().getName().startsWith("java.")) continue;
                        // Collect numeric instance fields from this object's class
                        java.util.List<java.lang.reflect.Field> numericFields =
                                new java.util.ArrayList<java.lang.reflect.Field>();
                        for (java.lang.reflect.Field sf : val.getClass().getDeclaredFields()) {
                            if (java.lang.reflect.Modifier.isStatic(sf.getModifiers())) continue;
                            if (sf.getType() == double.class || sf.getType() == float.class) {
                                try {
                                    sf.setAccessible(true);
                                } catch (Exception e) {
                                    continue; // Module system blocks access
                                }
                                numericFields.add(sf);
                            }
                        }
                        if (numericFields.size() < 6) continue;
                        // Validate: first numeric field should be ≈ origX - 0.3 (BB minX)
                        java.lang.reflect.Field candidate0 = numericFields.get(0);
                        double val0 = candidate0.getType() == float.class
                                ? candidate0.getFloat(val) : candidate0.getDouble(val);
                        if (Math.abs(val0 - expectedMinX) < 1.0) {
                            bbField = f;
                            bbX0 = numericFields.get(0);
                            bbY0 = numericFields.get(1);
                            bbZ0 = numericFields.get(2);
                            bbX1 = numericFields.get(3);
                            bbY1 = numericFields.get(4);
                            bbZ1 = numericFields.get(5);
                            break outer;
                        }
                    }
                    c = c.getSuperclass();
                }
                if (bbField == null) {
                    System.err.println("[McTestAgent] BB field NOT found!"
                            + " expectedMinX=" + String.format("%.2f", expectedMinX));
                }
            }

            if (bbField != null) {
                Object bb = bbField.get(player);
                if (bb != null) {
                    double w = 0.3;  // player half-width
                    double feetY = y - (double) 1.62f;
                    double headY = feetY + 1.8;
                    if (bbX0.getType() == float.class) {
                        bbX0.setFloat(bb, (float) (x - w));
                        bbY0.setFloat(bb, (float) feetY);
                        bbZ0.setFloat(bb, (float) (z - w));
                        bbX1.setFloat(bb, (float) (x + w));
                        bbY1.setFloat(bb, (float) headY);
                        bbZ1.setFloat(bb, (float) (z + w));
                    } else {
                        bbX0.setDouble(bb, x - w);
                        bbY0.setDouble(bb, feetY);
                        bbZ0.setDouble(bb, z - w);
                        bbX1.setDouble(bb, x + w);
                        bbY1.setDouble(bb, headY);
                        bbZ1.setDouble(bb, z + w);
                    }
                }
            }

            // Set onGround=false so physics applies gravity immediately
            if (onGroundField == null && mappings.onGroundFieldName() != null) {
                onGroundField = resolveField(player.getClass(),
                        mappings.onGroundFieldName(), boolean.class);
            }
            if (onGroundField != null) {
                onGroundField.setBoolean(player, false);
            }

            return true;
        } catch (Exception e) {
            System.err.println("[McTestAgent] teleportPlayer error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean isOnGround() {
        Object player = getPlayer();
        if (player == null) return false;
        try {
            if (onGroundField == null) {
                onGroundField = resolveField(player.getClass(),
                        mappings.onGroundFieldName(), boolean.class);
            }
            return onGroundField.getBoolean(player);
        } catch (Exception e) {
            return false;
        }
    }

    public void setOnGround(boolean value) {
        Object player = getPlayer();
        if (player == null) return;
        try {
            if (onGroundField == null && mappings.onGroundFieldName() != null) {
                onGroundField = resolveField(player.getClass(),
                        mappings.onGroundFieldName(), boolean.class);
            }
            if (onGroundField != null) {
                onGroundField.setBoolean(player, value);
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] setOnGround error: " + e.getMessage());
        }
    }

    /**
     * Returns the block ID at the given world coordinates.
     * Pre-Netty: World.getBlockId(int,int,int) returns int or boolean.
     * V4/V5 (1.7.x): World.a(int,int,int) returns Block object.
     * V47+ (1.8+): World.p(BlockPos) returns IBlockState object.
     */
    public int getBlockId(int x, int y, int z) {
        Object world = getWorld();
        if (world == null) {
            System.err.println("[McTestAgent] getBlockId: world is null at " + x + "," + y + "," + z);
            return -1;
        }
        try {
            if (getBlockIdMethod == null) {
                String methodName = mappings.getBlockIdMethodName();
                // Try (int,int,int) first — pre-Netty + V4/V5 (1.7.x)
                Class<?> c = world.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Method m = c.getDeclaredMethod(methodName,
                                int.class, int.class, int.class);
                        m.setAccessible(true);
                        getBlockIdMethod = m;
                        getBlockIdUsesBlockPos = false;
                        break;
                    } catch (NoSuchMethodException ignored) {}
                    c = c.getSuperclass();
                }
                // If not found with (int,int,int), search for 1-param method (V47+ BlockPos)
                if (getBlockIdMethod == null) {
                    c = world.getClass();
                    outer:
                    while (c != null && c != Object.class) {
                        for (Method m : c.getDeclaredMethods()) {
                            if (m.getName().equals(methodName) && m.getParameterCount() == 1
                                    && !m.getParameterTypes()[0].isPrimitive()) {
                                m.setAccessible(true);
                                getBlockIdMethod = m;
                                getBlockIdUsesBlockPos = true;
                                Class<?> bpClass = m.getParameterTypes()[0];
                                blockPosConstructor = bpClass.getConstructor(
                                        int.class, int.class, int.class);
                                blockPosConstructor.setAccessible(true);
                                break outer;
                            }
                        }
                        c = c.getSuperclass();
                    }
                }
                if (getBlockIdMethod == null) {
                    throw new RuntimeException("getBlockId method not found on "
                            + world.getClass().getName());
                }
            }
            Object result;
            if (getBlockIdUsesBlockPos) {
                Object pos = blockPosConstructor.newInstance(x, y, z);
                result = getBlockIdMethod.invoke(world, pos);
            } else {
                result = getBlockIdMethod.invoke(world, x, y, z);
            }
            if (result instanceof Integer) return (Integer) result;
            if (result instanceof Boolean) return ((Boolean) result) ? 1 : 0;
            // Object result: Block or IBlockState — convert to int block ID
            return convertToBlockId(result);
        } catch (Exception e) {
            System.err.println("[McTestAgent] getBlockId error: " + e.getClass().getName() + ": " + e.getMessage());
            return -1;
        }
    }

    /**
     * Converts a Block or IBlockState/BlockState object to an int block ID.
     * For Block: calls Block.getIdFromBlock(Block) static method.
     * For IBlockState: calls getBlock() first, then getIdFromBlock.
     */
    private int convertToBlockId(Object blockOrState) throws Exception {
        if (blockToIdMethod == null) {
            discoverBlockConversion(blockOrState);
        }
        Object block = blockOrState;
        if (stateGetBlockMethod != null) {
            block = stateGetBlockMethod.invoke(blockOrState);
        }
        return (int) blockToIdMethod.invoke(null, block);
    }

    private void discoverBlockConversion(Object blockOrState) throws Exception {
        Class<?> clazz = blockOrState.getClass();
        System.out.println("[McTestAgent] discoverBlockConversion: object class=" + clazz.getName());
        // Check if this class has a static int method taking itself (i.e., IS Block class)
        Method m = findStaticIntMethod(clazz);
        if (m != null) {
            blockToIdMethod = m;
            stateGetBlockMethod = null;
            System.out.println("[McTestAgent] Block direct: idMethod=" + m.getDeclaringClass().getName() + "." + m.getName());
            return;
        }
        // Must be IBlockState — find a no-arg method returning a type that has the static int method
        for (Method method : clazz.getMethods()) {
            if (method.getParameterCount() == 0 && !method.getReturnType().isPrimitive()
                    && method.getReturnType() != Object.class
                    && method.getReturnType() != String.class) {
                Method idMethod = findStaticIntMethod(method.getReturnType());
                if (idMethod != null) {
                    stateGetBlockMethod = method;
                    stateGetBlockMethod.setAccessible(true);
                    blockToIdMethod = idMethod;
                    System.out.println("[McTestAgent] IBlockState path: getBlock=" + method.getDeclaringClass().getName() + "." + method.getName()
                            + " -> " + method.getReturnType().getName()
                            + " idMethod=" + idMethod.getDeclaringClass().getName() + "." + idMethod.getName());
                    // List ALL candidate static int methods on block class for debugging
                    for (Method cm : method.getReturnType().getDeclaredMethods()) {
                        if (Modifier.isStatic(cm.getModifiers()) && cm.getReturnType() == int.class
                                && cm.getParameterCount() == 1 && cm.getParameterTypes()[0].isAssignableFrom(method.getReturnType())) {
                            System.out.println("[McTestAgent]   candidate: " + cm.getName() + "(" + cm.getParameterTypes()[0].getName() + ") -> int");
                        }
                    }
                    return;
                }
            }
        }
        // Fallback: try to find a static int method that takes the BlockState class directly
        // on any of its superclasses (e.g., Block.getRawIdFromState(BlockState) in 1.13+)
        for (Method method : clazz.getMethods()) {
            if (method.getParameterCount() == 0 && !method.getReturnType().isPrimitive()
                    && method.getReturnType() != Object.class
                    && method.getReturnType() != String.class) {
                // Check if the return type's class has a static int method that takes
                // the ORIGINAL class (BlockState) as param — e.g., Block.getRawIdFromState(BlockState)
                Method idMethod = findStaticIntMethodForParam(method.getReturnType(), clazz);
                if (idMethod != null) {
                    // Use this method directly with the original BlockState object (no getBlock step)
                    stateGetBlockMethod = null;
                    blockToIdMethod = idMethod;
                    System.out.println("[McTestAgent] BlockState direct ID path: idMethod="
                            + idMethod.getDeclaringClass().getName() + "." + idMethod.getName()
                            + "(" + clazz.getName() + ")");
                    return;
                }
            }
        }
        System.err.println("[McTestAgent] Cannot convert " + clazz.getName()
                + " to block ID. No-arg methods on class:");
        for (Method method : clazz.getMethods()) {
            if (method.getParameterCount() == 0 && !method.getReturnType().isPrimitive()
                    && method.getReturnType() != Object.class
                    && method.getReturnType() != String.class) {
                System.err.println("[McTestAgent]   " + method.getName() + "() -> " + method.getReturnType().getName());
            }
        }
        throw new RuntimeException("Cannot convert " + clazz.getName() + " to block ID");
    }

    /**
     * Finds a static method on a class that takes one argument of that class type
     * and returns int. Used for Block.getIdFromBlock(Block) and Item.getIdFromItem(Item).
     */
    private Method findStaticIntMethod(Class<?> clazz) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers())
                    && m.getReturnType() == int.class
                    && m.getParameterCount() == 1
                    && m.getParameterTypes()[0].isAssignableFrom(clazz)) {
                m.setAccessible(true);
                return m;
            }
        }
        Class<?> parent = clazz.getSuperclass();
        if (parent != null && parent != Object.class) {
            return findStaticIntMethod(parent);
        }
        return null;
    }

    /**
     * Finds a static method on searchClass (or its parents) that takes paramClass
     * as its single parameter and returns int. Used for e.g. Block.getRawIdFromState(BlockState).
     */
    private Method findStaticIntMethodForParam(Class<?> searchClass, Class<?> paramClass) {
        Class<?> c = searchClass;
        while (c != null && c != Object.class) {
            for (Method m : c.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())
                        && m.getReturnType() == int.class
                        && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isAssignableFrom(paramClass)) {
                    m.setAccessible(true);
                    return m;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    /**
     * Returns the block ID below the player's feet.
     * Player Y is eye-level; subtract 1.62 for feet, then -1 for block below.
     */
    public int getBlockBelowFeet() {
        double[] pos = getPlayerPosition();
        if (pos == null) return -1;
        int bx = (int) Math.floor(pos[0]);
        int by = (int) Math.floor(pos[1] - (double) 1.62f) - 1;
        int bz = (int) Math.floor(pos[2]);
        return getBlockId(bx, by, bz);
    }

    /**
     * Returns the block ID at the player's feet level.
     */
    public int getBlockAtFeet() {
        double[] pos = getPlayerPosition();
        if (pos == null) return -1;
        int bx = (int) Math.floor(pos[0]);
        int by = (int) Math.floor(pos[1] - (double) 1.62f);
        int bz = (int) Math.floor(pos[2]);
        return getBlockId(bx, by, bz);
    }

    /**
     * Returns inventory data as int[36][2] where [i][0]=itemId, [i][1]=stackSize.
     * Returns null if unavailable. Empty slots have itemId=0.
     */
    public int[][] getInventorySlots() {
        if (mappings.inventoryFieldName() == null) return null;
        Object player = getPlayer();
        if (player == null) return null;
        try {
            if (inventoryField == null) {
                inventoryField = resolveInventoryField(player);
            }
            Object inventory = inventoryField.get(player);
            if (inventory == null) {
                System.out.println("[McTestAgent] inventory field '" + mappings.inventoryFieldName()
                        + "' is null on " + player.getClass().getName());
                return null;
            }

            if (mainInventoryField == null) {
                mainInventoryField = resolveField(inventory.getClass(),
                        mappings.mainInventoryFieldName(), null);
            }
            Object rawInv = mainInventoryField.get(inventory);
            if (rawInv == null) {
                System.out.println("[McTestAgent] mainInventory field '" + mappings.mainInventoryFieldName()
                        + "' is null on " + inventory.getClass().getName());
                return null;
            }

            // mainInventory is Object[] in 1.7-1.9, NonNullList (List) in 1.12+
            int len;
            Object[] arrInv = null;
            List<?> listInv = null;
            if (rawInv instanceof Object[]) {
                arrInv = (Object[]) rawInv;
                len = Math.min(arrInv.length, 36);
            } else if (rawInv instanceof List) {
                listInv = (List<?>) rawInv;
                len = Math.min(listInv.size(), 36);
            } else {
                return null;
            }

            int[][] result = new int[len][2];
            for (int i = 0; i < len; i++) {
                Object stack = (arrInv != null) ? arrInv[i] : listInv.get(i);
                if (stack == null) {
                    result[i][0] = 0;
                    result[i][1] = 0;
                } else {
                    if (itemIdField == null) {
                        // Use null type filter: Netty versions store Item object, not int
                        itemIdField = resolveField(stack.getClass(),
                                mappings.itemIdFieldName(), null);
                        stackSizeField = resolveField(stack.getClass(),
                                mappings.stackSizeFieldName(), int.class);
                    }
                    result[i][0] = getItemId(stack);
                    result[i][1] = stackSizeField.getInt(stack);
                }
            }
            return result;
        } catch (Exception e) {
            System.out.println("[McTestAgent] Failed to read inventory: " + e
                    + " (invField=" + inventoryField + " mainInvField=" + mainInventoryField + ")");
            return null;
        }
    }

    /**
     * Extracts the int item ID from an ItemStack's item field.
     * Pre-Netty: field is int, return directly.
     * Netty: field is Item object, convert via Item.getIdFromItem(Item).
     */
    private int getItemId(Object stack) throws Exception {
        Object itemValue = itemIdField.get(stack);
        if (itemValue instanceof Integer) {
            return (Integer) itemValue;
        }
        if (itemValue == null) {
            return 0;
        }
        // Item object — find static int method on Item's class
        if (itemToIdMethod == null) {
            itemToIdMethod = findStaticIntMethod(itemValue.getClass());
            if (itemToIdMethod == null) {
                throw new RuntimeException("No static int method found on "
                        + itemValue.getClass().getName());
            }
        }
        return (int) itemToIdMethod.invoke(null, itemValue);
    }


    /**
     * Returns the active hotbar slot index (0-8).
     */
    public int getCurrentItemSlot() {
        if (mappings.inventoryFieldName() == null) return -1;
        Object player = getPlayer();
        if (player == null) return -1;
        try {
            if (inventoryField == null) {
                inventoryField = resolveInventoryField(player);
            }
            Object inventory = inventoryField.get(player);
            if (inventory == null) return -1;

            if (currentItemField == null) {
                currentItemField = resolveField(inventory.getClass(),
                        mappings.currentItemFieldName(), int.class);
            }
            return currentItemField.getInt(inventory);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Returns whether the mouse is currently grabbed (locked to window).
     */
    public boolean isMouseGrabbed() {
        try {
            Object target = getMouseGrabbedTarget();
            if (target == null) return false;
            if (mouseGrabbedField == null) {
                mouseGrabbedField = resolveField(target.getClass(),
                        mappings.mouseGrabbedFieldName(), boolean.class);
            }
            return mouseGrabbedField.getBoolean(target);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets the mouse grabbed state.
     */
    public void setMouseGrabbed(boolean grabbed) {
        try {
            Object target = getMouseGrabbedTarget();
            if (target == null) return;
            if (mouseGrabbedField == null) {
                mouseGrabbedField = resolveField(target.getClass(),
                        mappings.mouseGrabbedFieldName(), boolean.class);
            }
            mouseGrabbedField.setBoolean(target, grabbed);
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to set mouseGrabbed: " + e.getMessage());
        }
    }

    /**
     * Returns the object that holds the mouseGrabbed boolean field.
     * For LWJGL3 clients (1.13+), this is the MouseHelper sub-object.
     * For pre-LWJGL3 clients, this is the Minecraft instance directly.
     */
    private Object getMouseGrabbedTarget() throws Exception {
        if (mappings.mouseHelperFieldName() == null) {
            return minecraftInstance;
        }
        if (mouseHelperField == null) {
            mouseHelperField = resolveField(minecraftInstance.getClass(),
                    mappings.mouseHelperFieldName(), null);
        }
        return mouseHelperField.get(minecraftInstance);
    }

    // --- hitResult debug info ---

    private Field hitResultField;

    /**
     * Returns a debug string describing the current hitResult/objectMouseOver field
     * on the Minecraft instance. Used for debugging click() placement issues.
     */
    public String getHitResultInfo() {
        try {
            if (hitResultField == null) {
                // hitResult field name varies: 'v' in many versions, 'aa' in older ones.
                // Scan for a field whose type name contains "HitResult" or whose
                // simple type is "dpm" (1.18.2) / "evr" (1.20.6) etc.
                // We'll just scan for any field that is NOT a primitive, NOT a String,
                // and whose type toString contains "HitResult" or matches known patterns.
                Class<?> c = minecraftInstance.getClass();
                while (c != null && c != Object.class) {
                    for (Field f : c.getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                        String typeName = f.getType().getName();
                        // Try known hitResult type patterns
                        if (typeName.contains("HitResult") || typeName.equals("dpm")
                                || typeName.equals("evr") || typeName.equals("chn")
                                || typeName.equals("dcf")) {
                            f.setAccessible(true);
                            hitResultField = f;
                            break;
                        }
                    }
                    if (hitResultField != null) break;
                    c = c.getSuperclass();
                }
                if (hitResultField == null) return "field_not_found";
            }
            Object hr = hitResultField.get(minecraftInstance);
            if (hr == null) return "null";
            String type = hr.getClass().getSimpleName();
            // Try to extract block position — BlockHitResult has getBlockPos() method
            String posStr = "";
            try {
                // Find a no-arg method returning a non-primitive, non-String type
                // with int getX()/getY()/getZ() — that's BlockPos
                for (java.lang.reflect.Method m : hr.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && !m.getReturnType().isPrimitive()
                            && m.getReturnType() != String.class
                            && m.getReturnType() != Object.class) {
                        Object result = m.invoke(hr);
                        if (result == null) continue;
                        // Check if it has getX/getY/getZ
                        try {
                            java.lang.reflect.Method gx = result.getClass().getMethod("u");
                            java.lang.reflect.Method gy = result.getClass().getMethod("v");
                            java.lang.reflect.Method gz = result.getClass().getMethod("w");
                            int x = (Integer) gx.invoke(result);
                            int y = (Integer) gy.invoke(result);
                            int z = (Integer) gz.invoke(result);
                            posStr = " pos=(" + x + "," + y + "," + z + ")";
                            break;
                        } catch (NoSuchMethodException ignored) {
                            // Try getX/getY/getZ
                            try {
                                java.lang.reflect.Method gx = result.getClass().getMethod("getX");
                                java.lang.reflect.Method gy = result.getClass().getMethod("getY");
                                java.lang.reflect.Method gz = result.getClass().getMethod("getZ");
                                int x = (Integer) gx.invoke(result);
                                int y = (Integer) gy.invoke(result);
                                int z = (Integer) gz.invoke(result);
                                posStr = " pos=(" + x + "," + y + "," + z + ")";
                                break;
                            } catch (NoSuchMethodException ignored2) {}
                        }
                    }
                }
            } catch (Exception ignored) {}
            // Get hitResult type (BLOCK, MISS, ENTITY)
            String hitType = "";
            try {
                for (java.lang.reflect.Method m : hr.getClass().getMethods()) {
                    if (m.getParameterCount() == 0 && m.getReturnType().isEnum()) {
                        Object enumVal = m.invoke(hr);
                        if (enumVal != null) {
                            hitType = " type=" + enumVal.toString();
                            break;
                        }
                    }
                }
            } catch (Exception ignored) {}
            return type + hitType + posStr;
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    // --- Phase 3: screen, chat, cursor, cobblestone queries ---

    /**
     * Returns the current open screen's class, or null if no screen is open.
     */
    public Class<?> getCurrentScreenClass() {
        try {
            if (currentScreenField == null) {
                currentScreenField = resolveField(minecraftInstance.getClass(),
                        mappings.currentScreenFieldName(), null);
            }
            Object screen = currentScreenField.get(minecraftInstance);
            return screen != null ? screen.getClass() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns the current open screen object, or null if no screen is open.
     */
    public Object getCurrentScreen() {
        try {
            if (currentScreenField == null) {
                currentScreenField = resolveField(minecraftInstance.getClass(),
                        mappings.currentScreenFieldName(), null);
            }
            return currentScreenField.get(minecraftInstance);
        } catch (Exception e) {
            return null;
        }
    }

    // Cached RubyDung chat reflection handles
    private Field rdChatMessagesField;
    private Field rdChatEntryMessageField;

    /**
     * Returns the most recent chat messages (up to count).
     * Reads InGameHud.chatLines -> each ChatLine.text.
     * For RubyDung, reads from ChatRenderer.messages (static field).
     */
    public List<String> getChatMessages(int count) {
        // RubyDung: read from ChatRenderer (Fabric mixin layer)
        if (mappings.ingameGuiFieldName() == null) {
            return getChatMessagesRubyDung(count);
        }

        List<String> result = new ArrayList<String>();
        try {
            if (ingameGuiField == null) {
                ingameGuiField = resolveField(minecraftInstance.getClass(),
                        mappings.ingameGuiFieldName(), null);
            }
            Object hud = ingameGuiField.get(minecraftInstance);
            if (hud == null) return result;

            // In 1.3.1+ chatLines moved from GuiIngame to a GuiNewChat sub-object.
            // Try direct List access first; if the field isn't a List, auto-discover
            // the GuiNewChat intermediate that holds the actual chat lines.
            Object chatContainer = hud;
            if (chatLinesField == null) {
                Field candidate = resolveField(hud.getClass(),
                        mappings.chatLinesFieldName(), null);
                if (java.util.List.class.isAssignableFrom(candidate.getType())) {
                    chatLinesField = candidate;
                } else {
                    // Field "c" on GuiIngame isn't a List — look for GuiNewChat
                    chatGuiField = findFieldWithChild(hud.getClass(),
                            mappings.chatLinesFieldName());
                    if (chatGuiField != null) {
                        Object chatGui = chatGuiField.get(hud);
                        if (chatGui == null) return result;
                        chatLinesField = resolveField(chatGui.getClass(),
                                mappings.chatLinesFieldName(), null);
                    } else {
                        // Last resort: use the candidate anyway
                        chatLinesField = candidate;
                    }
                }
            }
            if (chatGuiField != null) {
                chatContainer = chatGuiField.get(hud);
                if (chatContainer == null) return result;
            }
            List<?> chatLines = (List<?>) chatLinesField.get(chatContainer);
            if (chatLines == null || chatLines.isEmpty()) return result;

            int limit = Math.min(count, chatLines.size());
            for (int i = 0; i < limit; i++) {
                Object chatLine = chatLines.get(i);
                if (chatLine == null) continue;

                if (chatLineTextField == null) {
                    // Use null type filter: Netty stores IChatComponent, not String
                    chatLineTextField = resolveField(chatLine.getClass(),
                            mappings.chatLineTextFieldName(), null);
                }
                Object textObj = chatLineTextField.get(chatLine);
                String text;
                if (textObj instanceof String) {
                    text = (String) textObj;
                } else if (textObj != null) {
                    if (chatTextMethod == null) {
                        chatTextMethod = discoverChatTextMethod(textObj);
                    }
                    text = (chatTextMethod != null)
                            ? (String) chatTextMethod.invoke(textObj)
                            : textObj.toString();
                } else {
                    continue;
                }
                if (text != null) result.add(text);
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to read chat: " + e.getMessage());
        }
        return result;
    }

    private List<String> getChatMessagesRubyDung(int count) {
        List<String> result = new ArrayList<String>();
        try {
            if (rdChatMessagesField == null) {
                ClassLoader cl = minecraftInstance.getClass().getClassLoader();
                Class<?> chatRenderer = cl.loadClass(
                        "com.github.martinambrus.rdforward.client.ChatRenderer");
                rdChatMessagesField = chatRenderer.getDeclaredField("messages");
                rdChatMessagesField.setAccessible(true);
            }
            List<?> messages = (List<?>) rdChatMessagesField.get(null); // static field
            if (messages == null || messages.isEmpty()) return result;

            int limit = Math.min(count, messages.size());
            for (int i = 0; i < limit; i++) {
                Object entry = messages.get(i);
                if (entry == null) continue;

                if (rdChatEntryMessageField == null) {
                    rdChatEntryMessageField = entry.getClass().getDeclaredField("message");
                    rdChatEntryMessageField.setAccessible(true);
                }
                String text = (String) rdChatEntryMessageField.get(entry);
                if (text != null) result.add(text);
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to read RD chat: " + e.getMessage());
        }
        return result;
    }

    /**
     * Returns cursor item as [itemId, stackSize] or null if no cursor item.
     */
    public int[] getCursorItem() {
        Object player = getPlayer();
        if (player == null) return null;
        try {
            if (inventoryField == null) {
                inventoryField = resolveInventoryField(player);
            }
            Object inventory = inventoryField.get(player);
            if (inventory == null) return null;

            if (cursorItemField == null) {
                cursorItemField = resolveField(inventory.getClass(),
                        mappings.cursorItemFieldName(), null);
            }
            Object cursorStack = cursorItemField.get(inventory);
            if (cursorStack == null) return null;

            if (itemIdField == null) {
                // Use null type filter: Netty versions store Item object, not int
                itemIdField = resolveField(cursorStack.getClass(),
                        mappings.itemIdFieldName(), null);
                stackSizeField = resolveField(cursorStack.getClass(),
                        mappings.stackSizeFieldName(), int.class);
            }
            int itemId = getItemId(cursorStack);
            if (itemId == 0) return null; // empty item = no cursor item
            return new int[]{itemId, stackSizeField.getInt(cursorStack)};
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns total cobblestone (id=4) count across all inventory slots.
     */
    private int cobblestoneItemId = -1;

    /**
     * Returns the cobblestone item ID as seen in the client inventory.
     * Auto-detected from the first non-empty slot (server only gives cobblestone).
     * Falls back to 4 (legacy) if no slot has items.
     */
    public int getCobblestoneItemId() {
        if (cobblestoneItemId > 0) return cobblestoneItemId;
        int[][] slots = getInventorySlots();
        if (slots != null) {
            for (int[] slot : slots) {
                if (slot[0] != 0 && slot[1] > 0) {
                    cobblestoneItemId = slot[0];
                    return cobblestoneItemId;
                }
            }
        }
        return 4; // legacy fallback
    }

    public int getTotalCobblestone() {
        if (mappings.inventoryFieldName() == null) return -1;
        int[][] slots = getInventorySlots();
        if (slots == null) return -1;
        int cobbleId = getCobblestoneItemId();
        int total = 0;
        for (int[] slot : slots) {
            if (slot[0] == cobbleId) total += slot[1];
        }
        return total;
    }

    /**
     * Discovers the method to extract plain text from an IChatComponent/ITextComponent.
     * Uses explicit mapping if available, otherwise scans for no-arg String methods.
     */
    private Method discoverChatTextMethod(Object textComponent) {
        try {
            // Use explicit mapping if available
            if (mappings.chatTextMethodName() != null) {
                Method m = textComponent.getClass().getMethod(mappings.chatTextMethodName());
                m.setAccessible(true);
                System.out.println("[McTestAgent] Chat text method (explicit): "
                        + mappings.chatTextMethodName());
                return m;
            }
            // Runtime discovery: find no-arg methods returning String
            for (Method m : textComponent.getClass().getMethods()) {
                if (m.getParameterCount() == 0 && m.getReturnType() == String.class
                        && !m.getName().equals("toString")
                        && !m.getName().equals("getClass")
                        && !m.getDeclaringClass().equals(Object.class)) {
                    m.setAccessible(true);
                    // Validate: try calling it and check for non-null result
                    try {
                        String test = (String) m.invoke(textComponent);
                        if (test != null && !test.isEmpty() && !test.startsWith("{")) {
                            System.out.println("[McTestAgent] Chat text method (discovered): "
                                    + m.getName() + " -> \"" + test + "\"");
                            return m;
                        }
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to discover chat text method: "
                    + e.getMessage());
        }
        return null;
    }

    /**
     * Resolves the inventory field on the player, handling the case where
     * inventoryFieldName and movementInputFieldName have the same obfuscated
     * name but live on different classes (e.g., "cp" on LocalPlayer = input,
     * "cp" on Player = inventory). We validate by checking that the field's
     * type contains the mainInventoryFieldName field.
     */
    private Field resolveInventoryField(Object player) {
        String invName = mappings.inventoryFieldName();
        String mainInvName = mappings.mainInventoryFieldName();
        Class<?> c = player.getClass();
        while (c != null && c != Object.class) {
            try {
                Field f = c.getDeclaredField(invName);
                if (!Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    // Validate: the field's type must contain mainInventoryFieldName
                    // as a List/array AND currentItemFieldName as an int.
                    // This distinguishes Inventory from unrelated types that happen
                    // to have a similarly-named List field (e.g., ClientPacketListener.deferredPackets).
                    if (mainInvName != null) {
                        boolean hasMainInv = false;
                        boolean hasSelectedSlot = false;
                        String selSlotName = mappings.currentItemFieldName();
                        Class<?> ft = f.getType();
                        while (ft != null && ft != Object.class) {
                            if (!hasMainInv) {
                                try {
                                    Field mainInvField = ft.getDeclaredField(mainInvName);
                                    Class<?> mainInvType = mainInvField.getType();
                                    if (List.class.isAssignableFrom(mainInvType)
                                            || mainInvType.isArray()) {
                                        hasMainInv = true;
                                    }
                                } catch (NoSuchFieldException ignored) {}
                            }
                            if (!hasSelectedSlot && selSlotName != null) {
                                try {
                                    Field selField = ft.getDeclaredField(selSlotName);
                                    if (selField.getType() == int.class) {
                                        hasSelectedSlot = true;
                                    }
                                } catch (NoSuchFieldException ignored) {}
                            }
                            ft = ft.getSuperclass();
                        }
                        if (!hasMainInv || (selSlotName != null && !hasSelectedSlot)) {
                            System.out.println("[McTestAgent] resolveInventoryField: skipping "
                                    + c.getName() + "." + invName + " (type=" + f.getType().getName()
                                    + ") — missing items/selected fields");
                            c = c.getSuperclass();
                            continue;
                        }
                    }
                    System.out.println("[McTestAgent] resolveInventoryField: found "
                            + c.getName() + "." + invName + " (type=" + f.getType().getName() + ")");
                    return f;
                }
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        System.out.println("[McTestAgent] resolveInventoryField: fallback to resolveField");
        // Fallback to standard resolution
        return resolveField(player.getClass(), invName, null);
    }

    /**
     * Resolve a field by name, searching the class hierarchy.
     * If the exact name isn't found and expectedType is provided,
     * falls back to scanning all fields for one matching the expected type.
     */
    private Field resolveField(Class<?> clazz, String name, Class<?> expectedType) {
        // First: try exact name + type match up the hierarchy (avoids shadowed fields)
        if (expectedType != null) {
            Class<?> c = clazz;
            while (c != null && c != Object.class) {
                try {
                    Field f = c.getDeclaredField(name);
                    if (f.getType() == expectedType) {
                        f.setAccessible(true);
                        return f;
                    }
                    // Name matches but wrong type — keep searching parent classes
                } catch (NoSuchFieldException ignored) {}
                c = c.getSuperclass();
            }
        }

        // Name-only match (no type filter or typed match failed)
        // Skip static fields to avoid shadowing (e.g. ept.co = static final int
        // shadows boj.co = private final inventory in 1.18.2)
        Class<?> c = clazz;
        Field staticFallback = null;
        while (c != null && c != Object.class) {
            try {
                Field f = c.getDeclaredField(name);
                if (!java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
                    f.setAccessible(true);
                    return f;
                }
                if (staticFallback == null) staticFallback = f;
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
        }
        // If only static fields matched, return the first one as last resort
        if (staticFallback != null) {
            staticFallback.setAccessible(true);
            return staticFallback;
        }

        // Fallback: type-based scan (for when SRG names differ between sub-versions)
        if (expectedType != null) {
            c = clazz;
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getType() == expectedType) {
                        f.setAccessible(true);
                        System.out.println("[McTestAgent] Type-scan fallback: " + name
                                + " -> " + c.getName() + "." + f.getName());
                        return f;
                    }
                }
                c = c.getSuperclass();
            }
        }

        throw new RuntimeException("Field not found: " + name + " in " + clazz.getName());
    }

    /**
     * Finds a field on parentClass whose type has a declared field named childFieldName
     * of List type. Used to auto-discover GuiNewChat (1.3.1+) from GuiIngame.
     */
    private Field findFieldWithChild(Class<?> parentClass, String childFieldName) {
        Class<?> c = parentClass;
        while (c != null && c != Object.class) {
            for (Field f : c.getDeclaredFields()) {
                Class<?> ft = f.getType();
                if (ft == Object.class || ft.isPrimitive() || ft.isArray()
                        || ft.getName().startsWith("java.")) continue;
                try {
                    Field child = ft.getDeclaredField(childFieldName);
                    if (java.util.List.class.isAssignableFrom(child.getType())) {
                        f.setAccessible(true);
                        System.out.println("[McTestAgent] Chat sub-object: "
                                + c.getName() + "." + f.getName() + " (" + ft.getName() + ")");
                        return f;
                    }
                } catch (NoSuchFieldException ignored) {}
            }
            c = c.getSuperclass();
        }
        return null;
    }

    /**
     * Dumps all fields of the Minecraft instance for debugging.
     * Useful when SRG names don't match and you need to discover correct names.
     */
    public void dumpFields() {
        System.out.println("[McTestAgent] === Field dump for " + minecraftInstance.getClass().getName() + " ===");
        Class<?> c = minecraftInstance.getClass();
        while (c != null && c != Object.class) {
            System.out.println("[McTestAgent] Class: " + c.getName());
            for (Field f : c.getDeclaredFields()) {
                f.setAccessible(true);
                try {
                    Object val = f.get(minecraftInstance);
                    System.out.println("[McTestAgent]   " + f.getType().getSimpleName()
                            + " " + f.getName() + " = " + val);
                } catch (Exception e) {
                    System.out.println("[McTestAgent]   " + f.getType().getSimpleName()
                            + " " + f.getName() + " = <error: " + e.getMessage() + ">");
                }
            }
            c = c.getSuperclass();
        }
        System.out.println("[McTestAgent] === End field dump ===");
    }
}
