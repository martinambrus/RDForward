package pocketmine.scheduler;

/**
 * PocketMine-flavoured wrapper around rd-api's {@code Scheduler}. Each
 * plugin receives its own instance; the bridge binds the plugin id so
 * cancellations on unload sweep every outstanding task owned by it.
 */
public interface TaskScheduler {

    /** Schedule {@code task} to run {@code delayTicks} server ticks from now. */
    TaskHandle scheduleDelayedTask(Task task, int delayTicks);

    /** Schedule {@code task} to run every {@code periodTicks} ticks until cancelled. */
    TaskHandle scheduleRepeatingTask(Task task, int periodTicks);

    /** Schedule {@code task} after an initial delay, then every {@code periodTicks} ticks. */
    TaskHandle scheduleDelayedRepeatingTask(Task task, int delayTicks, int periodTicks);

    /** Cancel every scheduled task owned by the plugin this scheduler belongs to. */
    void cancelAllTasks();

    interface TaskHandle {
        void cancel();
        boolean isCancelled();
    }
}
