package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Alpha 1.2.6 (protocol v6).
 * Verified by field dump of the official Alpha 1.2.6 client JAR.
 *
 * Class names are human-readable but ALL field and method names use
 * single-letter ProGuard obfuscation (NOT SRG names — those only exist
 * in RetroMCP-deobfuscated JARs, not in official Mojang client JARs).
 */
public class AlphaV6Mappings implements FieldMappings {

    @Override
    public String minecraftClassName() {
        return "net.minecraft.client.Minecraft";
    }

    // run() is NOT obfuscated (implements Runnable)
    @Override
    public String runMethodName() {
        return "run";
    }

    // i() = runTick (obfuscated as single letter "i")
    @Override
    public String tickMethodName() {
        return "i";
    }

    // g = thePlayer (type bq = EntityPlayerSP)
    @Override
    public String playerFieldName() {
        return "g";
    }

    // e = theWorld (type cy = World)
    @Override
    public String worldFieldName() {
        return "e";
    }

    // V = serverName (String, null when no server set)
    @Override
    public String serverHostFieldName() {
        return "V";
    }

    // W = serverPort (int, 0 when no server set)
    @Override
    public String serverPortFieldName() {
        return "W";
    }

    // c = displayWidth (int, default 854)
    @Override
    public String displayWidthFieldName() {
        return "c";
    }

    // d = displayHeight (int, default 480)
    @Override
    public String displayHeightFieldName() {
        return "d";
    }

    // Entity position fields in Entity base class (lw)
    // aw=posX, ax=posY, ay=posZ (verified from decompiled setPosition/b(d,d,d))
    @Override
    public String posXFieldName() {
        return "aw";
    }

    @Override
    public String posYFieldName() {
        return "ax";
    }

    @Override
    public String posZFieldName() {
        return "ay";
    }
}
