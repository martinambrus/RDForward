package org.bukkit.block.data.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BrewingStand extends org.bukkit.block.data.BlockData {
    boolean hasBottle(int arg0);
    void setBottle(int arg0, boolean arg1);
    java.util.Set getBottles();
    int getMaximumBottles();
}
