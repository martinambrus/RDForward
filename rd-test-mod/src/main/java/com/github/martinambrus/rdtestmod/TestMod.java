package com.github.martinambrus.rdtestmod;

import com.github.martinambrus.rdforward.api.event.EventPriority;
import com.github.martinambrus.rdforward.api.event.EventResult;
import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.api.mod.Reloadable;
import com.github.martinambrus.rdforward.api.mod.ServerMod;
import com.github.martinambrus.rdforward.api.scheduler.ScheduledTask;
import com.github.martinambrus.rdforward.api.server.Server;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Integration-test mod for rd-mod-loader. Exercises every major slice of
 * the API so a single hot-reload verifies events, commands, scheduler
 * tracking, and {@link Reloadable} state handoff all still work.
 *
 * <p>Not production code — lives in its own module so the fat test jar
 * can be dropped into {@code dataDir/mods/} during a reload test.
 */
public final class TestMod implements ServerMod, Reloadable {

    private static final String MOD_ID = "rd-test-mod";

    private final AtomicLong ticks = new AtomicLong();
    private ScheduledTask heartbeatTask;
    private Server server;

    @Override
    public void onEnable(Server server) {
        this.server = server;
        System.out.println("[rd-test-mod] enabled, current tick = " + ticks.get());

        ServerEvents.BLOCK_PLACE.register(EventPriority.MONITOR, (player, x, y, z, blockType) -> {
            System.out.println("[rd-test-mod] observed " + player + " placing block "
                    + blockType + " at (" + x + ", " + y + ", " + z + ")");
            return EventResult.PASS;
        }, MOD_ID);

        ServerEvents.PLAYER_JOIN.register((name, version) ->
                System.out.println("[rd-test-mod] welcome " + name + " (" + version + ")"));

        server.getCommandRegistry().register(MOD_ID, "rdtest",
                "RDForward test mod diagnostic command",
                ctx -> ctx.reply("rd-test-mod: ticks=" + ticks.get()
                        + " mods=" + server.getModManager().all().size()));

        heartbeatTask = server.getScheduler().runRepeating(MOD_ID, 200, 200, () -> {
            long t = ticks.incrementAndGet();
            if (t % 5 == 0) System.out.println("[rd-test-mod] heartbeat " + t);
        });
    }

    @Override
    public void onDisable() {
        System.out.println("[rd-test-mod] disabled, final tick = " + ticks.get());
        if (heartbeatTask != null) heartbeatTask.cancel();
    }

    @Override
    public Object onSaveState() {
        return ticks.get();
    }

    @Override
    public void onRestoreState(Object savedState) {
        if (savedState instanceof Long l) ticks.set(l);
    }
}
