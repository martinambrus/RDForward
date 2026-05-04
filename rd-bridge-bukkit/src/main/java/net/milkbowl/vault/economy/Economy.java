// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.milkbowl.vault.economy;

import org.bukkit.OfflinePlayer;

import java.util.List;

/**
 * Vault 1.7 Economy contract. Mirrors the upstream method set so any
 * plugin that compiles against Vault links cleanly against the bridge
 * stub. The bridge supplies a default {@link AbstractEconomy} subclass
 * ({@code BridgeStubEconomy}) registered into the
 * {@link org.bukkit.plugin.ServicesManager} at lowest priority — a real
 * Vault.jar dropped into {@code plugins/} wins via priority ordering.
 */
public interface Economy {

    boolean isEnabled();

    String getName();

    boolean hasBankSupport();

    int fractionalDigits();

    String format(double amount);

    String currencyNamePlural();

    String currencyNameSingular();

    boolean hasAccount(String playerName);
    boolean hasAccount(OfflinePlayer player);
    boolean hasAccount(String playerName, String worldName);
    boolean hasAccount(OfflinePlayer player, String worldName);

    double getBalance(String playerName);
    double getBalance(OfflinePlayer player);
    double getBalance(String playerName, String world);
    double getBalance(OfflinePlayer player, String world);

    boolean has(String playerName, double amount);
    boolean has(OfflinePlayer player, double amount);
    boolean has(String playerName, String worldName, double amount);
    boolean has(OfflinePlayer player, String worldName, double amount);

    EconomyResponse withdrawPlayer(String playerName, double amount);
    EconomyResponse withdrawPlayer(OfflinePlayer player, double amount);
    EconomyResponse withdrawPlayer(String playerName, String worldName, double amount);
    EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount);

    EconomyResponse depositPlayer(String playerName, double amount);
    EconomyResponse depositPlayer(OfflinePlayer player, double amount);
    EconomyResponse depositPlayer(String playerName, String worldName, double amount);
    EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount);

    EconomyResponse createBank(String name, String player);
    EconomyResponse createBank(String name, OfflinePlayer player);
    EconomyResponse deleteBank(String name);
    EconomyResponse bankBalance(String name);
    EconomyResponse bankHas(String name, double amount);
    EconomyResponse bankWithdraw(String name, double amount);
    EconomyResponse bankDeposit(String name, double amount);
    EconomyResponse isBankOwner(String name, String playerName);
    EconomyResponse isBankOwner(String name, OfflinePlayer player);
    EconomyResponse isBankMember(String name, String playerName);
    EconomyResponse isBankMember(String name, OfflinePlayer player);

    List<String> getBanks();

    boolean createPlayerAccount(String playerName);
    boolean createPlayerAccount(OfflinePlayer player);
    boolean createPlayerAccount(String playerName, String worldName);
    boolean createPlayerAccount(OfflinePlayer player, String worldName);
}
