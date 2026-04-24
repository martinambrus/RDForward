package org.bukkit.map;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class MapRenderer {
    public MapRenderer() {}
    public MapRenderer(boolean arg0) {}
    public final boolean isContextual() {
        return false;
    }
    public void initialize(org.bukkit.map.MapView arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.map.MapRenderer.initialize(Lorg/bukkit/map/MapView;)V");
    }
    public abstract void render(org.bukkit.map.MapView arg0, org.bukkit.map.MapCanvas arg1, org.bukkit.entity.Player arg2);
}
