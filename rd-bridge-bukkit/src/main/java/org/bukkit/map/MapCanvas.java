package org.bukkit.map;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MapCanvas {
    org.bukkit.map.MapView getMapView();
    org.bukkit.map.MapCursorCollection getCursors();
    void setCursors(org.bukkit.map.MapCursorCollection arg0);
    void setPixelColor(int arg0, int arg1, java.awt.Color arg2);
    java.awt.Color getPixelColor(int arg0, int arg1);
    java.awt.Color getBasePixelColor(int arg0, int arg1);
    void setPixel(int arg0, int arg1, byte arg2);
    byte getPixel(int arg0, int arg1);
    byte getBasePixel(int arg0, int arg1);
    void drawImage(int arg0, int arg1, java.awt.Image arg2);
    void drawText(int arg0, int arg1, org.bukkit.map.MapFont arg2, java.lang.String arg3);
}
