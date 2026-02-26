package com.github.martinambrus.rdforward.e2e.agent.mappings;

/**
 * Field mappings for Beta 1.6 (protocol v12).
 * Verified by CFR decompile of the official Beta 1.6 client JAR.
 *
 * Differences from Beta 1.7.3 (BetaV14Mappings):
 * - Entity position fields shifted by -1 (aL/aM/aN vs aM/aN/aO)
 * - Yaw/pitch shifted (aR/aS vs aS/aT)
 * - onGround shifted (aW vs aX)
 * - GuiScreen class is cy (not da)
 * - GuiInventory is tz (not ue)
 */
public class BetaV12Mappings extends BetaV14Mappings {

    @Override
    public String posXFieldName() {
        return "aL";
    }

    @Override
    public String posYFieldName() {
        return "aM";
    }

    @Override
    public String posZFieldName() {
        return "aN";
    }

    @Override
    public String yawFieldName() {
        return "aR";
    }

    @Override
    public String pitchFieldName() {
        return "aS";
    }

    @Override
    public String onGroundFieldName() {
        return "aW";
    }

    @Override
    public String guiScreenClassName() {
        return "cy";
    }

    @Override
    public String guiInventoryClassName() {
        return "tz";
    }
}
