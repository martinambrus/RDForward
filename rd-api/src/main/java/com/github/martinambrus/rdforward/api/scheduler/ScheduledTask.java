package com.github.martinambrus.rdforward.api.scheduler;

/** Handle for a scheduled task. */
public interface ScheduledTask {

    /** Cancel the task. No-op if already cancelled or finished. */
    void cancel();

    boolean isCancelled();
}
