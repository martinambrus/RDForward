package org.bukkit.block.data.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Repeater extends org.bukkit.block.data.Directional, org.bukkit.block.data.Powerable {
    int getDelay();
    void setDelay(int arg0);
    int getMinimumDelay();
    int getMaximumDelay();
    boolean isLocked();
    void setLocked(boolean arg0);
}
