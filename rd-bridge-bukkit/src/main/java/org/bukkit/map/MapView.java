package org.bukkit.map;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MapView {
    int getId();
    boolean isVirtual();
    org.bukkit.map.MapView$Scale getScale();
    void setScale(org.bukkit.map.MapView$Scale arg0);
    int getCenterX();
    int getCenterZ();
    void setCenterX(int arg0);
    void setCenterZ(int arg0);
    org.bukkit.World getWorld();
    void setWorld(org.bukkit.World arg0);
    java.util.List getRenderers();
    void addRenderer(org.bukkit.map.MapRenderer arg0);
    boolean removeRenderer(org.bukkit.map.MapRenderer arg0);
    boolean isTrackingPosition();
    void setTrackingPosition(boolean arg0);
    boolean isUnlimitedTracking();
    void setUnlimitedTracking(boolean arg0);
    boolean isLocked();
    void setLocked(boolean arg0);
}
