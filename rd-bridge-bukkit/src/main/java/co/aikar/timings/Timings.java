package co.aikar.timings;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class Timings {
    public static final co.aikar.timings.Timing NULL_HANDLER = null;
    public Timings() {}
    public static co.aikar.timings.Timing of(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        return null;
    }
    public static co.aikar.timings.Timing of(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, co.aikar.timings.Timing arg2) {
        return null;
    }
    public static co.aikar.timings.Timing ofStart(org.bukkit.plugin.Plugin arg0, java.lang.String arg1) {
        return null;
    }
    public static co.aikar.timings.Timing ofStart(org.bukkit.plugin.Plugin arg0, java.lang.String arg1, co.aikar.timings.Timing arg2) {
        return null;
    }
    public static boolean isTimingsEnabled() {
        return false;
    }
    public static void setTimingsEnabled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.Timings.setTimingsEnabled(Z)V");
    }
    public static net.kyori.adventure.text.Component deprecationMessage() {
        return null;
    }
    public static boolean isVerboseTimingsEnabled() {
        return false;
    }
    public static void setVerboseTimingsEnabled(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.Timings.setVerboseTimingsEnabled(Z)V");
    }
    public static int getHistoryInterval() {
        return 0;
    }
    public static void setHistoryInterval(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.Timings.setHistoryInterval(I)V");
    }
    public static int getHistoryLength() {
        return 0;
    }
    public static void setHistoryLength(int arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.Timings.setHistoryLength(I)V");
    }
    public static void reset() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.Timings.reset()V");
    }
    public static void generateReport(org.bukkit.command.CommandSender arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.Timings.generateReport(Lorg/bukkit/command/CommandSender;)V");
    }
    public static void generateReport(co.aikar.timings.TimingsReportListener arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.Timings.generateReport(Lco/aikar/timings/TimingsReportListener;)V");
    }
}
