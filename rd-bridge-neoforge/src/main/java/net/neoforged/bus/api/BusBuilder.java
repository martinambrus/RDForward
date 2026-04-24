package net.neoforged.bus.api;

/** Auto-generated stub from bus-7.2.0.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BusBuilder {
    static net.neoforged.bus.api.BusBuilder builder() {
        return null;
    }
    net.neoforged.bus.api.BusBuilder setExceptionHandler(net.neoforged.bus.api.IEventExceptionHandler arg0);
    net.neoforged.bus.api.BusBuilder startShutdown();
    net.neoforged.bus.api.BusBuilder checkTypesOnDispatch();
    default net.neoforged.bus.api.BusBuilder markerType(java.lang.Class arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.neoforged.bus.api.BusBuilder.markerType(Ljava/lang/Class;)Lnet/neoforged/bus/api/BusBuilder;");
        return this;
    }
    net.neoforged.bus.api.BusBuilder classChecker(net.neoforged.bus.api.IEventClassChecker arg0);
    net.neoforged.bus.api.BusBuilder allowPerPhasePost();
    net.neoforged.bus.api.IEventBus build();
}
