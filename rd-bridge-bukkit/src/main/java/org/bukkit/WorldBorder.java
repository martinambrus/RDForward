package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface WorldBorder {
    org.bukkit.World getWorld();
    void reset();
    double getSize();
    void setSize(double arg0);
    void changeSize(double arg0, long arg1);
    default void setSize(double arg0, long arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.WorldBorder.setSize(DJ)V");
    }
    default void setSize(double arg0, java.util.concurrent.TimeUnit arg1, long arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.WorldBorder.setSize(DLjava/util/concurrent/TimeUnit;J)V");
    }
    org.bukkit.Location getCenter();
    void setCenter(double arg0, double arg1);
    void setCenter(org.bukkit.Location arg0);
    double getDamageBuffer();
    void setDamageBuffer(double arg0);
    double getDamageAmount();
    void setDamageAmount(double arg0);
    default int getWarningTime() {
        return 0;
    }
    default void setWarningTime(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.WorldBorder.setWarningTime(I)V");
    }
    int getWarningTimeTicks();
    void setWarningTimeTicks(int arg0);
    int getWarningDistance();
    void setWarningDistance(int arg0);
    boolean isInside(org.bukkit.Location arg0);
    double getMaxSize();
    double getMaxCenterCoordinate();
}
