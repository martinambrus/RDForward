package io.papermc.paper.block.fluid;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface FluidData extends java.lang.Cloneable {
    org.bukkit.Fluid getFluidType();
    io.papermc.paper.block.fluid.FluidData clone();
    org.bukkit.util.Vector computeFlowDirection(org.bukkit.Location arg0);
    int getLevel();
    float computeHeight(org.bukkit.Location arg0);
    boolean isSource();
}
