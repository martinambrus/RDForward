package co.aikar.timings;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Timing extends java.lang.AutoCloseable {
    co.aikar.timings.Timing startTiming();
    void stopTiming();
    co.aikar.timings.Timing startTimingIfSync();
    void stopTimingIfSync();
    void abort();
    co.aikar.timings.TimingHandler getTimingHandler();
    void close();
}
