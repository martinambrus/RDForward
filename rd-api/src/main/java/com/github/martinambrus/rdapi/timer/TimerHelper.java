package com.github.martinambrus.rdapi.timer;

public class TimerHelper {

    public static void runTaskDelayed(final Runnable runnable, final long delayMillis) {
        final long requested = System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // The while is just to ignore interruption.
                while (true) {
                    try {
                        long leftToSleep = requested + delayMillis - System.currentTimeMillis();
                        if (leftToSleep > 0) {
                            Thread.sleep(leftToSleep);
                        }
                        break;
                    } catch (InterruptedException ignored) {
                    }
                }
                runnable.run();
            }
        }).start();
    }
}
