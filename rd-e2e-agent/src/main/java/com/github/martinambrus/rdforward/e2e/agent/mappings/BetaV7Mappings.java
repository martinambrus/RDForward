package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.0 (protocol v7).
 * Verified by CFR decompile of the official Beta 1.0 client JAR.
 *
 * Very similar to Beta 1.2_02 (BetaV8Mappings).
 * Differences from Beta 1.2_02:
 * - Entity position fields shifted (aG/aH/aI vs aF/aG/aH)
 * - Yaw/pitch shifted (aM/aN vs aL/aM)
 * - onGround shifted (aR vs aQ)
 * - dropOneItemMethodName is "w" (not "z")
 * - GuiScreen class is br, GuiInventory is ns
 */
public class BetaV7Mappings extends BetaV8Mappings {

    @Override
    public String posXFieldName() {
        return "aG";
    }

    @Override
    public String posYFieldName() {
        return "aH";
    }

    @Override
    public String posZFieldName() {
        return "aI";
    }

    @Override
    public String yawFieldName() {
        return "aM";
    }

    @Override
    public String pitchFieldName() {
        return "aN";
    }

    @Override
    public String onGroundFieldName() {
        return "aR";
    }

    @Override
    public String dropOneItemMethodName() {
        return "w";
    }

    @Override
    public String guiScreenClassName() {
        return "br";
    }

    @Override
    public String guiInventoryClassName() {
        return "ns";
    }

    @Override
    public String closeContainerMethodName() {
        return "m";
    }
}
