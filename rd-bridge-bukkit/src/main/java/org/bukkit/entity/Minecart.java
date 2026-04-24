package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Minecart extends org.bukkit.entity.Vehicle, io.papermc.paper.entity.Frictional {
    void setDamage(double arg0);
    double getDamage();
    double getMaxSpeed();
    void setMaxSpeed(double arg0);
    boolean isSlowWhenEmpty();
    void setSlowWhenEmpty(boolean arg0);
    org.bukkit.util.Vector getFlyingVelocityMod();
    void setFlyingVelocityMod(org.bukkit.util.Vector arg0);
    org.bukkit.util.Vector getDerailedVelocityMod();
    void setDerailedVelocityMod(org.bukkit.util.Vector arg0);
    void setDisplayBlock(org.bukkit.material.MaterialData arg0);
    org.bukkit.material.MaterialData getDisplayBlock();
    void setDisplayBlockData(org.bukkit.block.data.BlockData arg0);
    org.bukkit.block.data.BlockData getDisplayBlockData();
    void setDisplayBlockOffset(int arg0);
    int getDisplayBlockOffset();
    org.bukkit.Material getMinecartMaterial();
}
