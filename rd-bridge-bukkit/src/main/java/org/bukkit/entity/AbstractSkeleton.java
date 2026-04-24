package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AbstractSkeleton extends org.bukkit.entity.Monster, com.destroystokyo.paper.entity.RangedEntity {
    org.bukkit.entity.Skeleton$SkeletonType getSkeletonType();
    void setSkeletonType(org.bukkit.entity.Skeleton$SkeletonType arg0);
    boolean shouldBurnInDay();
    void setShouldBurnInDay(boolean arg0);
}
