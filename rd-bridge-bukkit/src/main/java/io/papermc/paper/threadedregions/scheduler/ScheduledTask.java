package io.papermc.paper.threadedregions.scheduler;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ScheduledTask {
    org.bukkit.plugin.Plugin getOwningPlugin();
    boolean isRepeatingTask();
    io.papermc.paper.threadedregions.scheduler.ScheduledTask$CancelledState cancel();
    io.papermc.paper.threadedregions.scheduler.ScheduledTask$ExecutionState getExecutionState();
    default boolean isCancelled() {
        return false;
    }
}
