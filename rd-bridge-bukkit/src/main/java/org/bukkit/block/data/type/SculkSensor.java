package org.bukkit.block.data.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SculkSensor extends org.bukkit.block.data.AnaloguePowerable, org.bukkit.block.data.Waterlogged {
    default org.bukkit.block.data.type.SculkSensor$Phase getPhase() {
        return null;
    }
    default void setPhase(org.bukkit.block.data.type.SculkSensor$Phase arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.type.SculkSensor.setPhase(Lorg/bukkit/block/data/type/SculkSensor$Phase;)V");
    }
    org.bukkit.block.data.type.SculkSensor$Phase getSculkSensorPhase();
    void setSculkSensorPhase(org.bukkit.block.data.type.SculkSensor$Phase arg0);
}
