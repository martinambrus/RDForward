package io.papermc.paper.threadedregions.scheduler;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AsyncScheduler {
    io.papermc.paper.threadedregions.scheduler.ScheduledTask runNow(org.bukkit.plugin.Plugin arg0, java.util.function.Consumer arg1);
    io.papermc.paper.threadedregions.scheduler.ScheduledTask runDelayed(org.bukkit.plugin.Plugin arg0, java.util.function.Consumer arg1, long arg2, java.util.concurrent.TimeUnit arg3);
    io.papermc.paper.threadedregions.scheduler.ScheduledTask runAtFixedRate(org.bukkit.plugin.Plugin arg0, java.util.function.Consumer arg1, long arg2, long arg3, java.util.concurrent.TimeUnit arg4);
    void cancelTasks(org.bukkit.plugin.Plugin arg0);
}
