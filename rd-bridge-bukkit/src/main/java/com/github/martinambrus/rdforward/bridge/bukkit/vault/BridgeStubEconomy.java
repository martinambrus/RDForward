// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.github.martinambrus.rdforward.bridge.bukkit.vault;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import org.bukkit.OfflinePlayer;

import java.util.Collections;
import java.util.List;

/**
 * Default Vault {@code Economy} provider registered by the Bukkit
 * bridge when no real Vault.jar is present. RDForward owns no money
 * model, so every transaction reports {@link ResponseType#FAILURE} with
 * a descriptive message — plugins like Bananas's {@code setupEconomy()}
 * still find a non-null provider and run their startup branches; their
 * {@code withdrawPlayer(...).transactionSuccess()} simply returns false
 * and the caller's "no funds" branch fires.
 *
 * <p>Registered at {@link org.bukkit.plugin.ServicePriority#Lowest}, so
 * any real economy plugin (iConomy, Essentials Eco, etc.) registering at
 * Normal or higher takes over via
 * {@link org.bukkit.plugin.ServicesManager#getRegistration} which returns
 * the highest-priority entry.
 */
public final class BridgeStubEconomy extends AbstractEconomy {

    private static final String PLUGIN = "Vault";
    private static final String NO_ECON = "RDForward has no economy provider";

    private static EconomyResponse fail(String sig) {
        StubCallLog.logOnce(PLUGIN, "Economy." + sig);
        return new EconomyResponse(0.0d, 0.0d, ResponseType.FAILURE, NO_ECON);
    }

    @Override public boolean isEnabled() { return true; }
    @Override public String getName() { return "RDForwardStubEconomy"; }
    @Override public boolean hasBankSupport() { return false; }
    @Override public int fractionalDigits() { return 2; }
    @Override public String currencyNamePlural() { return "coins"; }
    @Override public String currencyNameSingular() { return "coin"; }

    @Override
    public String format(double amount) {
        return amount + " coins";
    }

    @Override public boolean hasAccount(String playerName) { return false; }
    @Override public boolean hasAccount(OfflinePlayer player) { return false; }
    @Override public boolean hasAccount(String playerName, String worldName) { return false; }
    @Override public boolean hasAccount(OfflinePlayer player, String worldName) { return false; }

    @Override public double getBalance(String playerName) { return 0.0d; }
    @Override public double getBalance(OfflinePlayer player) { return 0.0d; }
    @Override public double getBalance(String playerName, String world) { return 0.0d; }
    @Override public double getBalance(OfflinePlayer player, String world) { return 0.0d; }

    @Override public boolean has(String playerName, double amount) { return false; }
    @Override public boolean has(OfflinePlayer player, double amount) { return false; }
    @Override public boolean has(String playerName, String worldName, double amount) { return false; }
    @Override public boolean has(OfflinePlayer player, String worldName, double amount) { return false; }

    @Override public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return fail("withdrawPlayer(String,double)");
    }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        return fail("withdrawPlayer(OfflinePlayer,double)");
    }
    @Override public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return fail("withdrawPlayer(String,String,double)");
    }
    @Override public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return fail("withdrawPlayer(OfflinePlayer,String,double)");
    }

    @Override public EconomyResponse depositPlayer(String playerName, double amount) {
        return fail("depositPlayer(String,double)");
    }
    @Override public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        return fail("depositPlayer(OfflinePlayer,double)");
    }
    @Override public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return fail("depositPlayer(String,String,double)");
    }
    @Override public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return fail("depositPlayer(OfflinePlayer,String,double)");
    }

    @Override public EconomyResponse createBank(String name, String player) { return fail("createBank"); }
    @Override public EconomyResponse createBank(String name, OfflinePlayer player) { return fail("createBank"); }
    @Override public EconomyResponse deleteBank(String name) { return fail("deleteBank"); }
    @Override public EconomyResponse bankBalance(String name) { return fail("bankBalance"); }
    @Override public EconomyResponse bankHas(String name, double amount) { return fail("bankHas"); }
    @Override public EconomyResponse bankWithdraw(String name, double amount) { return fail("bankWithdraw"); }
    @Override public EconomyResponse bankDeposit(String name, double amount) { return fail("bankDeposit"); }
    @Override public EconomyResponse isBankOwner(String name, String playerName) { return fail("isBankOwner"); }
    @Override public EconomyResponse isBankOwner(String name, OfflinePlayer player) { return fail("isBankOwner"); }
    @Override public EconomyResponse isBankMember(String name, String playerName) { return fail("isBankMember"); }
    @Override public EconomyResponse isBankMember(String name, OfflinePlayer player) { return fail("isBankMember"); }

    @Override public List<String> getBanks() { return Collections.emptyList(); }

    @Override public boolean createPlayerAccount(String playerName) { return false; }
    @Override public boolean createPlayerAccount(OfflinePlayer player) { return false; }
    @Override public boolean createPlayerAccount(String playerName, String worldName) { return false; }
    @Override public boolean createPlayerAccount(OfflinePlayer player, String worldName) { return false; }
}
