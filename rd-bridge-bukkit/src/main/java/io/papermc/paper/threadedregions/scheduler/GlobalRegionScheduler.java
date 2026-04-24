package io.papermc.paper.threadedregions.scheduler;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface GlobalRegionScheduler {
    void execute(org.bukkit.plugin.Plugin arg0, java.lang.Runnable arg1);
    io.papermc.paper.threadedregions.scheduler.ScheduledTask run(org.bukkit.plugin.Plugin arg0, java.util.function.Consumer arg1);
    io.papermc.paper.threadedregions.scheduler.ScheduledTask runDelayed(org.bukkit.plugin.Plugin arg0, java.util.function.Consumer arg1, long arg2);
    io.papermc.paper.threadedregions.scheduler.ScheduledTask runAtFixedRate(org.bukkit.plugin.Plugin arg0, java.util.function.Consumer arg1, long arg2, long arg3);
    void cancelTasks(org.bukkit.plugin.Plugin arg0);
}
