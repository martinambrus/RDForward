package pocketmine.scheduler;

/**
 * Base class for PocketMine tasks. The concrete PocketMine API also has
 * {@code PluginTask} and {@code AsyncTask}; only the synchronous
 * tick-based {@code onRun} entry point crosses the bridge.
 */
public abstract class Task {

    public abstract void onRun(int currentTick);

    public void onCancel() {}
}
