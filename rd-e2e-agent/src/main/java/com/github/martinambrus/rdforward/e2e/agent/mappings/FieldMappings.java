package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Version-specific field/method name mappings for a Minecraft client JAR.
 * Each client version has its own obfuscation scheme; implementations
 * provide the SRG or obfuscated names for the fields the agent needs.
 */
public interface FieldMappings {

    /** Fully-qualified Minecraft main class name. */
    String minecraftClassName();

    /** The run() method name (usually not obfuscated). */
    String runMethodName();

    /** The tick method name (e.g. "func_6246_i" or "i"). */
    String tickMethodName();

    /** Field name for thePlayer (EntityPlayerSP) on Minecraft class. */
    String playerFieldName();

    /** Field name for theWorld (World) on Minecraft class. */
    String worldFieldName();

    /** Field name for serverName/host (String) on Minecraft class. */
    String serverHostFieldName();

    /** Field name for serverPort (int) on Minecraft class. */
    String serverPortFieldName();

    /** Field name for displayWidth (int) on Minecraft class. */
    String displayWidthFieldName();

    /** Field name for displayHeight (int) on Minecraft class. */
    String displayHeightFieldName();

    /** Field name for posX (double) on Entity class. */
    String posXFieldName();

    /** Field name for posY (double) on Entity class. */
    String posYFieldName();

    /** Field name for posZ (double) on Entity class. */
    String posZFieldName();
}
