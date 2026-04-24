package io.papermc.paper.raytracing;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PositionedRayTraceConfigurationBuilder {
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder start(org.bukkit.Location arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder direction(org.bukkit.util.Vector arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder maxDistance(double arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder fluidCollisionMode(org.bukkit.FluidCollisionMode arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder blockCollisionMode(io.papermc.paper.raytracing.BlockCollisionMode arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder ignorePassableBlocks(boolean arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder raySize(double arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder entityFilter(java.util.function.Predicate arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder blockFilter(java.util.function.Predicate arg0);
    io.papermc.paper.raytracing.PositionedRayTraceConfigurationBuilder targets(io.papermc.paper.raytracing.RayTraceTarget arg0, io.papermc.paper.raytracing.RayTraceTarget[] arg1);
}
