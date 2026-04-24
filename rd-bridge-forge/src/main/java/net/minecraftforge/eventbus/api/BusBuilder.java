package net.minecraftforge.eventbus.api;

/** Auto-generated stub from eventbus-6.2.5.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BusBuilder {
    static net.minecraftforge.eventbus.api.BusBuilder builder() {
        return null;
    }
    net.minecraftforge.eventbus.api.BusBuilder setTrackPhases(boolean arg0);
    net.minecraftforge.eventbus.api.BusBuilder setExceptionHandler(net.minecraftforge.eventbus.api.IEventExceptionHandler arg0);
    net.minecraftforge.eventbus.api.BusBuilder startShutdown();
    net.minecraftforge.eventbus.api.BusBuilder checkTypesOnDispatch();
    net.minecraftforge.eventbus.api.BusBuilder markerType(java.lang.Class arg0);
    net.minecraftforge.eventbus.api.BusBuilder useModLauncher();
    net.minecraftforge.eventbus.api.IEventBus build();
}
