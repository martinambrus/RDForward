package co.aikar.timings;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TimingsReportListener implements net.kyori.adventure.audience.ForwardingAudience, org.bukkit.command.MessageCommandSender {
    public TimingsReportListener(org.bukkit.command.CommandSender arg0) {}
    public TimingsReportListener(org.bukkit.command.CommandSender arg0, java.lang.Runnable arg1) {}
    public TimingsReportListener(java.util.List arg0) {}
    public TimingsReportListener(java.util.List arg0, java.lang.Runnable arg1) {}
    public TimingsReportListener() {}
    public java.lang.String getTimingsURL() {
        return null;
    }
    public void done() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.TimingsReportListener.done()V");
    }
    public void done(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.TimingsReportListener.done(Ljava/lang/String;)V");
    }
    public void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.TimingsReportListener.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    public java.lang.Iterable audiences() {
        return java.util.Collections.emptyList();
    }
    public void sendMessage(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.TimingsReportListener.sendMessage(Ljava/lang/String;)V");
    }
    public void addConsoleIfNeeded() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "co.aikar.timings.TimingsReportListener.addConsoleIfNeeded()V");
    }
}
