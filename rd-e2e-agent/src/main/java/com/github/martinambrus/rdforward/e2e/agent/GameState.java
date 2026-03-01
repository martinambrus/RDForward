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

    private java.lang.reflect.Method setPositionMethod;

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
                            break;
                        }
                    } catch (NoSuchMethodException ignored) {}
                    c = c.getSuperclass();
                }
                if (setPositionMethod == null) {
                    System.err.println("[McTestAgent] setPosition method not found, falling back to field writes");
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
                        // Collect numeric instance fields from this object's class
                        java.util.List<java.lang.reflect.Field> numericFields =
                                new java.util.ArrayList<java.lang.reflect.Field>();
                        for (java.lang.reflect.Field sf : val.getClass().getDeclaredFields()) {
                            if (java.lang.reflect.Modifier.isStatic(sf.getModifiers())) continue;
                            if (sf.getType() == double.class || sf.getType() == float.class) {
                                sf.setAccessible(true);
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
                inventoryField = resolveField(player.getClass(),
                        mappings.inventoryFieldName(), null);
            }
            Object inventory = inventoryField.get(player);
            if (inventory == null) return null;

            if (mainInventoryField == null) {
                mainInventoryField = resolveField(inventory.getClass(),
                        mappings.mainInventoryFieldName(), null);
            }
            Object rawInv = mainInventoryField.get(inventory);
            if (rawInv == null) return null;

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
            System.err.println("[McTestAgent] Failed to read inventory: " + e.getMessage());
            e.printStackTrace();
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
                inventoryField = resolveField(player.getClass(),
                        mappings.inventoryFieldName(), null);
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
                inventoryField = resolveField(player.getClass(),
                        mappings.inventoryFieldName(), null);
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
    public int getTotalCobblestone() {
        if (mappings.inventoryFieldName() == null) return -1;
        int[][] slots = getInventorySlots();
        if (slots == null) return -1;
        int total = 0;
        for (int[] slot : slots) {
            if (slot[0] == 4) total += slot[1];
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
        Class<?> c = clazz;
        while (c != null && c != Object.class) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {}
            c = c.getSuperclass();
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
