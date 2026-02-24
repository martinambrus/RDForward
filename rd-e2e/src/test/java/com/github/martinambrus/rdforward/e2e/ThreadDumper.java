import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class ThreadDumper {
    public static void dumpThreads() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] infos = bean.dumpAllThreads(true, true);
        System.out.println("=== THREAD DUMP ===");
        for (ThreadInfo info : infos) {
            System.out.println(info.toString());
        }
    }
}
