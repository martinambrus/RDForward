package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CopperGolem extends org.bukkit.entity.Golem, io.papermc.paper.entity.Shearable {
    io.papermc.paper.world.WeatheringCopperState getWeatheringState();
    void setWeatheringState(io.papermc.paper.world.WeatheringCopperState arg0);
    org.bukkit.entity.CopperGolem$State getGolemState();
    void setGolemState(org.bukkit.entity.CopperGolem$State arg0);
    org.bukkit.entity.CopperGolem$Oxidizing getOxidizing();
    void setOxidizing(org.bukkit.entity.CopperGolem$Oxidizing arg0);
}
