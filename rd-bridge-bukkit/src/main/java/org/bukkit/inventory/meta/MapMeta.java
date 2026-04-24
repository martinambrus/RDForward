package org.bukkit.inventory.meta;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MapMeta extends org.bukkit.inventory.meta.ItemMeta {
    boolean hasMapId();
    int getMapId();
    void setMapId(int arg0);
    boolean hasMapView();
    org.bukkit.map.MapView getMapView();
    void setMapView(org.bukkit.map.MapView arg0);
    boolean isScaling();
    void setScaling(boolean arg0);
    boolean hasLocationName();
    java.lang.String getLocationName();
    void setLocationName(java.lang.String arg0);
    boolean hasColor();
    org.bukkit.Color getColor();
    void setColor(org.bukkit.Color arg0);
    org.bukkit.inventory.meta.MapMeta clone();
}
