package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface EndGateway extends org.bukkit.block.TileState {
    org.bukkit.Location getExitLocation();
    void setExitLocation(org.bukkit.Location arg0);
    boolean isExactTeleport();
    void setExactTeleport(boolean arg0);
    long getAge();
    void setAge(long arg0);
}
