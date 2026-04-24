package org.bukkit.advancement;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AdvancementProgress {
    org.bukkit.advancement.Advancement getAdvancement();
    boolean isDone();
    boolean awardCriteria(java.lang.String arg0);
    boolean revokeCriteria(java.lang.String arg0);
    java.util.Date getDateAwarded(java.lang.String arg0);
    java.util.Collection getRemainingCriteria();
    java.util.Collection getAwardedCriteria();
}
