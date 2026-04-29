---
name: BukkitPluginWrapper must fire PluginEnableEvent / PluginDisableEvent
description: Plugins relying on PluginEnableEvent (Essentials's permissions handler swap) silently break unless BukkitPluginWrapper dispatches the event after onEnable
type: project
originSessionId: 5502dcd6-95a6-49a3-a950-70fdccebac2e
---
`BukkitPluginWrapper.onEnable` must dispatch `PluginEnableEvent` (and `onDisable` must dispatch `PluginDisableEvent`) via `BukkitEventAdapter.dispatchPluginEvent` AFTER the plugin's own `onEnable()` / `onDisable()` returns. Listeners the plugin registers DURING its own `onEnable` need to receive their own enable event — that is real Bukkit's order.

**Why:** Essentials's `EssentialsPluginListener.onPluginEnable` is the ONLY caller of `PermissionsHandler.checkPermissions()`. Without `PluginEnableEvent` firing, Essentials's `permissionsHandler.handler` field stays at the default `NullPermissionsHandler` (every `hasPermission` returns false). Result: every Essentials command — `/tree`, `/heal`, `/spawn` — denies every player, OPs included, with `[Essentials] [WARNING] <name> was denied access to command.` Confirmed via 2.9.5 decompile.

**How to apply:**
- Don't drop the dispatch when refactoring `BukkitPluginWrapper` — order is `plugin.onEnable()` → bridge listeners → `registerCommands` → `dispatchPluginEvent(new PluginEnableEvent(plugin))`.
- `PluginEvent` / `PluginEnableEvent` / `PluginDisableEvent` are hand-tuned (`@rdforward:preserve`); they store the plugin reference in `PluginEvent.plugin` and own a static `HandlerList` so generic Bukkit code that calls `getHandlerList()` reflectively gets a non-null value. Don't regenerate them from the auto-stub template — that template passes `null` to `super` and returns `null` from `getPlugin()`, which silently breaks any listener that calls `event.getPlugin()`.
- Other plugins likely depend on the same hook (Vault providers, dynmap reload, AlternativeCommandsHandler payment-method detection). When a plugin's behaviour seems to "miss" lifecycle context, suspect this dispatch first.
- Test pin: `PluginEnableEventDispatchTest` verifies dispatch order (post-onEnable) and that `event.getPlugin()` carries the plugin reference.
