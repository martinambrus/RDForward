package io.papermc.paper.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SchoolableFish extends org.bukkit.entity.Fish {
    void startFollowing(io.papermc.paper.entity.SchoolableFish arg0);
    void stopFollowing();
    int getSchoolSize();
    int getMaxSchoolSize();
    io.papermc.paper.entity.SchoolableFish getSchoolLeader();
}
