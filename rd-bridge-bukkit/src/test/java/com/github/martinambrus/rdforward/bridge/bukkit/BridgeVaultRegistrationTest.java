package com.github.martinambrus.rdforward.bridge.bukkit;

import com.github.martinambrus.rdforward.api.event.server.ServerEvents;
import com.github.martinambrus.rdforward.bridge.bukkit.fixtures.StubRdServer;
import com.github.martinambrus.rdforward.bridge.bukkit.vault.BridgeStubEconomy;
import com.github.martinambrus.rdforward.bridge.bukkit.vault.BridgeStubPermission;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the bridge's stub Vault provider behaviour:
 * <ul>
 *   <li>{@link BukkitBridge#install} registers a stub {@code Economy} +
 *       {@code Permission} when no real Vault plugin is loaded;</li>
 *   <li>both register at {@link ServicePriority#Lowest}, so a plugin
 *       registering at {@code Normal} (or higher) wins the
 *       {@code getRegistration} lookup;</li>
 *   <li>{@link BukkitBridge#uninstall} drops the stub registrations.</li>
 * </ul>
 */
class BridgeVaultRegistrationTest {

    @BeforeEach
    void clearBefore() {
        ServerEvents.clearAll();
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin("Vault");
        clearAllServices();
    }

    @AfterEach
    void clearAfter() {
        ServerEvents.clearAll();
        BukkitBridge.uninstall();
        BukkitBridge.unregisterPlugin("Vault");
        clearAllServices();
    }

    /** The static services manager backing {@code Bukkit.getServer().getServicesManager()}
     *  persists across tests, so any registration we make lingers into
     *  the next test unless we wipe it. We only ever register for
     *  Economy / Permission in these tests, so clearing those two is
     *  enough — {@code unregister(Class, null)} drops every entry for
     *  that service class.
     *
     *  <p>Reads the manager via reflection because the field
     *  {@code Bukkit.server} may still be the previous test's adapter
     *  when this runs (BukkitBridge.uninstall() clears it but the
     *  bridge install in the test body needs a fresh start). */
    private static void clearAllServices() {
        ServicesManager services;
        try {
            Class<?> sup = Class.forName("org.bukkit.ServerSupport");
            java.lang.reflect.Field f = sup.getDeclaredField("SERVICES");
            f.setAccessible(true);
            Object v = f.get(null);
            services = v instanceof ServicesManager s ? s : null;
        } catch (ReflectiveOperationException e) {
            return;
        }
        if (services == null) return;
        services.unregister(Economy.class, null);
        services.unregister(Permission.class, null);
    }

    @Test
    void installRegistersStubEconomyAndPermissionWhenNoRealVault() {
        BukkitBridge.install(new StubRdServer());
        ServicesManager services = Bukkit.getServer().getServicesManager();

        RegisteredServiceProvider econ = services.getRegistration(Economy.class);
        assertNotNull(econ, "stub Economy should be registered after install");
        assertInstanceOf(BridgeStubEconomy.class, econ.getProvider());
        assertSame(ServicePriority.Lowest, econ.getPriority());
        assertNotNull(econ.getPlugin(), "stub registration must carry a plugin owner");
        assertSame("Vault", econ.getPlugin().getName(),
                "owner plugin name must be 'Vault' so getRegistration().getPlugin().getName() reports correctly");

        RegisteredServiceProvider perm = services.getRegistration(Permission.class);
        assertNotNull(perm, "stub Permission should be registered after install");
        assertInstanceOf(BridgeStubPermission.class, perm.getProvider());
        assertSame(ServicePriority.Lowest, perm.getPriority());

        assertNotNull(Bukkit.getPluginManager().getPlugin("Vault"),
                "synthetic Vault owner must be reachable via PluginManager.getPlugin");
    }

    @Test
    void higherPriorityRealProviderWinsLookup() {
        BukkitBridge.install(new StubRdServer());
        ServicesManager services = Bukkit.getServer().getServicesManager();

        Economy real = new FakeRealEconomy();
        JavaPlugin owner = new FakeOwnerPlugin();
        services.register(Economy.class, real, owner, ServicePriority.Normal);

        RegisteredServiceProvider top = services.getRegistration(Economy.class);
        assertSame(real, top.getProvider(),
                "Normal-priority registration must beat the bridge's Lowest stub");
    }

    @Test
    void uninstallDropsStubRegistrations() {
        BukkitBridge.install(new StubRdServer());
        ServicesManager services = Bukkit.getServer().getServicesManager();
        assertNotNull(services.getRegistration(Economy.class));
        assertNotNull(services.getRegistration(Permission.class));

        BukkitBridge.uninstall();

        // post-uninstall the static services manager should no longer
        // hold our owner's entries
        assertNull(services.getRegistration(Economy.class),
                "uninstall must unregister stub Economy");
        assertNull(services.getRegistration(Permission.class),
                "uninstall must unregister stub Permission");
        assertNull(BukkitBridge.lookupPlugin("Vault"),
                "uninstall must drop the synthetic Vault plugin entry");
    }

    @Test
    void installSkipsStubIfRealVaultAlreadyRegistered() {
        // Simulate a real Vault.jar having registered itself before install.
        FakeOwnerPlugin realOwner = new FakeOwnerPlugin("Vault");
        BukkitBridge.registerPlugin("Vault", realOwner);

        BukkitBridge.install(new StubRdServer());
        ServicesManager services = Bukkit.getServer().getServicesManager();

        // No stub should have been registered — the lookup table still
        // points to the pre-existing real plugin owner.
        assertSame(realOwner, BukkitBridge.lookupPlugin("Vault"),
                "real Vault registration must not be clobbered by the stub");
        assertNull(services.getRegistration(Economy.class),
                "no stub Economy should be registered when real Vault is present");
    }

    static final class FakeOwnerPlugin extends JavaPlugin {
        private final String name;
        FakeOwnerPlugin() { this("FakePlugin"); }
        FakeOwnerPlugin(String name) {
            this.name = name;
            setDescription(new org.bukkit.plugin.PluginDescriptionFile(
                    name, "1.0", FakeOwnerPlugin.class.getName()));
            setEnabled(true);
        }
        @Override public String getName() { return name; }
    }

    static final class FakeRealEconomy implements Economy {
        @Override public boolean isEnabled() { return true; }
        @Override public String getName() { return "FakeRealEconomy"; }
        @Override public boolean hasBankSupport() { return false; }
        @Override public int fractionalDigits() { return 2; }
        @Override public String format(double amount) { return amount + "$"; }
        @Override public String currencyNamePlural() { return "$"; }
        @Override public String currencyNameSingular() { return "$"; }
        @Override public boolean hasAccount(String playerName) { return true; }
        @Override public boolean hasAccount(OfflinePlayer player) { return true; }
        @Override public boolean hasAccount(String playerName, String worldName) { return true; }
        @Override public boolean hasAccount(OfflinePlayer player, String worldName) { return true; }
        @Override public double getBalance(String playerName) { return 0; }
        @Override public double getBalance(OfflinePlayer player) { return 0; }
        @Override public double getBalance(String playerName, String world) { return 0; }
        @Override public double getBalance(OfflinePlayer player, String world) { return 0; }
        @Override public boolean has(String playerName, double amount) { return false; }
        @Override public boolean has(OfflinePlayer player, double amount) { return false; }
        @Override public boolean has(String playerName, String worldName, double amount) { return false; }
        @Override public boolean has(OfflinePlayer player, String worldName, double amount) { return false; }
        @Override public EconomyResponse withdrawPlayer(String playerName, double amount) { return ok(); }
        @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) { return ok(); }
        @Override public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) { return ok(); }
        @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) { return ok(); }
        @Override public EconomyResponse depositPlayer(String playerName, double amount) { return ok(); }
        @Override public EconomyResponse depositPlayer(OfflinePlayer player, double amount) { return ok(); }
        @Override public EconomyResponse depositPlayer(String playerName, String worldName, double amount) { return ok(); }
        @Override public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) { return ok(); }
        @Override public EconomyResponse createBank(String name, String player) { return ok(); }
        @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return ok(); }
        @Override public EconomyResponse deleteBank(String name) { return ok(); }
        @Override public EconomyResponse bankBalance(String name) { return ok(); }
        @Override public EconomyResponse bankHas(String name, double amount) { return ok(); }
        @Override public EconomyResponse bankWithdraw(String name, double amount) { return ok(); }
        @Override public EconomyResponse bankDeposit(String name, double amount) { return ok(); }
        @Override public EconomyResponse isBankOwner(String name, String playerName) { return ok(); }
        @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return ok(); }
        @Override public EconomyResponse isBankMember(String name, String playerName) { return ok(); }
        @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return ok(); }
        @Override public List<String> getBanks() { return List.of(); }
        @Override public boolean createPlayerAccount(String playerName) { return true; }
        @Override public boolean createPlayerAccount(OfflinePlayer player) { return true; }
        @Override public boolean createPlayerAccount(String playerName, String worldName) { return true; }
        @Override public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return true; }
        private static EconomyResponse ok() {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.SUCCESS, "");
        }
    }

    @Test
    void bukkitStaticGetServicesManagerDelegatesToServer() {
        // Vault's bStats Metrics constructor calls Bukkit.getServicesManager()
        // directly (not via Bukkit.getServer().getServicesManager()) and
        // throws NoSuchMethodError if the static is missing. Pins the
        // delegation so a future refactor doesn't reintroduce that NSME.
        BukkitBridge.install(new StubRdServer());
        ServicesManager viaStatic = Bukkit.getServicesManager();
        ServicesManager viaInstance = Bukkit.getServer().getServicesManager();
        assertSame(viaInstance, viaStatic,
                "Bukkit.getServicesManager() must return the same instance as the server-bound accessor");
    }

    @Test
    void stubEconomyReportsFailureForTransactions() {
        BukkitBridge.install(new StubRdServer());
        Economy stub = (Economy) Bukkit.getServer().getServicesManager()
                .getRegistration(Economy.class).getProvider();
        EconomyResponse r = stub.withdrawPlayer("anyone", 100.0);
        assertFalse(r.transactionSuccess(),
                "stub economy must report transaction failure (no money model)");
        assertSame(EconomyResponse.ResponseType.FAILURE, r.type);
        assertTrue(stub.format(50.0).contains("coins"));
    }
}
