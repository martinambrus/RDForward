package io.papermc.paper.threadedregions.scheduler;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface RegionScheduler {
    void execute(org.bukkit.plugin.Plugin arg0, org.bukkit.World arg1, int arg2, int arg3, java.lang.Runnable arg4);
    default void execute(org.bukkit.plugin.Plugin arg0, org.bukkit.Location arg1, java.lang.Runnable arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.threadedregions.scheduler.RegionScheduler.execute(Lorg/bukkit/plugin/Plugin;Lorg/bukkit/Location;Ljava/lang/Runnable;)V");
    }
    io.papermc.paper.threadedregions.scheduler.ScheduledTask run(org.bukkit.plugin.Plugin arg0, org.bukkit.World arg1, int arg2, int arg3, java.util.function.Consumer arg4);
    default io.papermc.paper.threadedregions.scheduler.ScheduledTask run(org.bukkit.plugin.Plugin arg0, org.bukkit.Location arg1, java.util.function.Consumer arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.threadedregions.scheduler.RegionScheduler.run(Lorg/bukkit/plugin/Plugin;Lorg/bukkit/Location;Ljava/util/function/Consumer;)Lio/papermc/paper/threadedregions/scheduler/ScheduledTask;");
        return null;
    }
    io.papermc.paper.threadedregions.scheduler.ScheduledTask runDelayed(org.bukkit.plugin.Plugin arg0, org.bukkit.World arg1, int arg2, int arg3, java.util.function.Consumer arg4, long arg5);
    default io.papermc.paper.threadedregions.scheduler.ScheduledTask runDelayed(org.bukkit.plugin.Plugin arg0, org.bukkit.Location arg1, java.util.function.Consumer arg2, long arg3) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.threadedregions.scheduler.RegionScheduler.runDelayed(Lorg/bukkit/plugin/Plugin;Lorg/bukkit/Location;Ljava/util/function/Consumer;J)Lio/papermc/paper/threadedregions/scheduler/ScheduledTask;");
        return null;
    }
    io.papermc.paper.threadedregions.scheduler.ScheduledTask runAtFixedRate(org.bukkit.plugin.Plugin arg0, org.bukkit.World arg1, int arg2, int arg3, java.util.function.Consumer arg4, long arg5, long arg6);
    default io.papermc.paper.threadedregions.scheduler.ScheduledTask runAtFixedRate(org.bukkit.plugin.Plugin arg0, org.bukkit.Location arg1, java.util.function.Consumer arg2, long arg3, long arg4) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.threadedregions.scheduler.RegionScheduler.runAtFixedRate(Lorg/bukkit/plugin/Plugin;Lorg/bukkit/Location;Ljava/util/function/Consumer;JJ)Lio/papermc/paper/threadedregions/scheduler/ScheduledTask;");
        return null;
    }
}
