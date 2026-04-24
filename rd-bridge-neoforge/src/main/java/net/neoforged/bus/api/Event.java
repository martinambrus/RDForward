package net.neoforged.bus.api;

/** Auto-generated stub from bus-7.2.0.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class Event {
    protected Event() {}
    public final boolean hasResult() {
        return false;
    }
    public final net.neoforged.bus.api.Event$Result getResult() {
        return null;
    }
    public void setResult(net.neoforged.bus.api.Event$Result arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.neoforged.bus.api.Event.setResult(Lnet/neoforged/bus/api/Event$Result;)V");
    }
}
