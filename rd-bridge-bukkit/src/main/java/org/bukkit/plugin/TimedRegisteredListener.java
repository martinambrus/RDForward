package org.bukkit.plugin;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class TimedRegisteredListener extends org.bukkit.plugin.RegisteredListener {
    public TimedRegisteredListener(org.bukkit.event.Listener arg0, org.bukkit.plugin.EventExecutor arg1, org.bukkit.event.EventPriority arg2, org.bukkit.plugin.Plugin arg3, boolean arg4) { super((org.bukkit.event.Listener) null, (org.bukkit.plugin.EventExecutor) null, (org.bukkit.event.EventPriority) null, (org.bukkit.plugin.Plugin) null, false); }
    public TimedRegisteredListener() { super((org.bukkit.event.Listener) null, (org.bukkit.plugin.EventExecutor) null, (org.bukkit.event.EventPriority) null, (org.bukkit.plugin.Plugin) null, false); }
    public void callEvent(org.bukkit.event.Event arg0) throws org.bukkit.event.EventException {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.TimedRegisteredListener.callEvent(Lorg/bukkit/event/Event;)V");
    }
    public void reset() {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.plugin.TimedRegisteredListener.reset()V");
    }
    public int getCount() {
        return 0;
    }
    public long getTotalTime() {
        return 0L;
    }
    public java.lang.Class getEventClass() {
        return null;
    }
    public boolean hasMultiple() {
        return false;
    }
}
