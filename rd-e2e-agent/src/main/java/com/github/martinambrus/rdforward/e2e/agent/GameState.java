package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.mappings.FieldMappings;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private Method getBlockIdMethod;

    // Phase 3: screen, chat, cursor fields
    private Field currentScreenField;
    private Field ingameGuiField;
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
            if (displayWidthField == null) {
                displayWidthField = resolveField(minecraftInstance.getClass(),
                        mappings.displayWidthFieldName(), null);
            }
            return displayWidthField.getInt(minecraftInstance);
        } catch (Exception e) {
            return 854; // default
        }
    }

    public int getDisplayHeight() {
        try {
            if (displayHeightField == null) {
                displayHeightField = resolveField(minecraftInstance.getClass(),
                        mappings.displayHeightFieldName(), null);
            }
            return displayHeightField.getInt(minecraftInstance);
        } catch (Exception e) {
            return 480; // default
        }
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
            }
            return new double[]{
                posXField.getDouble(player),
                posYField.getDouble(player),
                posZField.getDouble(player)
            };
        } catch (Exception e) {
            return null;
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
     * Uses World.getBlockId(int,int,int) via reflection.
     */
    public int getBlockId(int x, int y, int z) {
        Object world = getWorld();
        if (world == null) return -1;
        try {
            if (getBlockIdMethod == null) {
                // Find the method with exact signature (int,int,int)->int or boolean
                // Walk up hierarchy since World may be a subclass
                Class<?> c = world.getClass();
                while (c != null && c != Object.class) {
                    try {
                        Method m = c.getDeclaredMethod(mappings.getBlockIdMethodName(),
                                int.class, int.class, int.class);
                        if (m.getReturnType() == int.class || m.getReturnType() == boolean.class) {
                            m.setAccessible(true);
                            getBlockIdMethod = m;
                            break;
                        }
                    } catch (NoSuchMethodException ignored) {}
                    c = c.getSuperclass();
                }
                if (getBlockIdMethod == null) {
                    throw new RuntimeException("getBlockId method not found on "
                            + world.getClass().getName());
                }
            }
            Object result = getBlockIdMethod.invoke(world, x, y, z);
            if (result instanceof Boolean) {
                return ((Boolean) result) ? 1 : 0;
            }
            return (Integer) result;
        } catch (Exception e) {
            return -1;
        }
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
            Object[] mainInventory = (Object[]) mainInventoryField.get(inventory);
            if (mainInventory == null) return null;

            int len = Math.min(mainInventory.length, 36);
            int[][] result = new int[len][2];
            for (int i = 0; i < len; i++) {
                Object stack = mainInventory[i];
                if (stack == null) {
                    result[i][0] = 0;
                    result[i][1] = 0;
                } else {
                    if (itemIdField == null) {
                        itemIdField = resolveField(stack.getClass(),
                                mappings.itemIdFieldName(), int.class);
                        stackSizeField = resolveField(stack.getClass(),
                                mappings.stackSizeFieldName(), int.class);
                    }
                    result[i][0] = itemIdField.getInt(stack);
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
            if (mouseGrabbedField == null) {
                mouseGrabbedField = resolveField(minecraftInstance.getClass(),
                        mappings.mouseGrabbedFieldName(), boolean.class);
            }
            return mouseGrabbedField.getBoolean(minecraftInstance);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Sets the mouse grabbed state.
     */
    public void setMouseGrabbed(boolean grabbed) {
        try {
            if (mouseGrabbedField == null) {
                mouseGrabbedField = resolveField(minecraftInstance.getClass(),
                        mappings.mouseGrabbedFieldName(), boolean.class);
            }
            mouseGrabbedField.setBoolean(minecraftInstance, grabbed);
        } catch (Exception e) {
            System.err.println("[McTestAgent] Failed to set mouseGrabbed: " + e.getMessage());
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

            if (chatLinesField == null) {
                chatLinesField = resolveField(hud.getClass(),
                        mappings.chatLinesFieldName(), null);
            }
            List<?> chatLines = (List<?>) chatLinesField.get(hud);
            if (chatLines == null || chatLines.isEmpty()) return result;

            int limit = Math.min(count, chatLines.size());
            for (int i = 0; i < limit; i++) {
                Object chatLine = chatLines.get(i);
                if (chatLine == null) continue;

                if (chatLineTextField == null) {
                    chatLineTextField = resolveField(chatLine.getClass(),
                            mappings.chatLineTextFieldName(), String.class);
                }
                String text = (String) chatLineTextField.get(chatLine);
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
                itemIdField = resolveField(cursorStack.getClass(),
                        mappings.itemIdFieldName(), int.class);
                stackSizeField = resolveField(cursorStack.getClass(),
                        mappings.stackSizeFieldName(), int.class);
            }
            return new int[]{itemIdField.getInt(cursorStack), stackSizeField.getInt(cursorStack)};
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
     * Resolve a field by name, searching the class hierarchy.
     * If the exact name isn't found and expectedType is provided,
     * falls back to scanning all fields for one matching the expected type.
     */
    private Field resolveField(Class<?> clazz, String name, Class<?> expectedType) {
        // First: try exact name match up the hierarchy
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
