package com.github.martinambrus.rdforward.e2e.agent;

import com.github.martinambrus.rdforward.e2e.agent.mappings.FieldMappings;

import java.lang.reflect.Field;

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

    public GameState(FieldMappings mappings, Object minecraftInstance) {
        this.mappings = mappings;
        this.minecraftInstance = minecraftInstance;
    }

    public Object getMinecraftInstance() {
        return minecraftInstance;
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
