// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.milkbowl.vault.economy;

import org.bukkit.OfflinePlayer;

import java.util.List;

/**
 * Minimal stand-in for Vault's {@code AbstractEconomy}. Real Vault
 * delegates the {@code String}-based overloads to {@code OfflinePlayer}
 * ones via {@code Bukkit.getOfflinePlayer(name)}. RDForward's bridge has
 * no offline-player resolver, so the abstract layer leaves both shapes
 * abstract — concrete subclasses (notably {@code BridgeStubEconomy})
 * implement them directly and the two paths simply do the same thing.
 */
public abstract class AbstractEconomy implements Economy {

    @Override public abstract boolean isEnabled();
    @Override public abstract String getName();
    @Override public abstract boolean hasBankSupport();
    @Override public abstract int fractionalDigits();
    @Override public abstract String format(double amount);
    @Override public abstract String currencyNamePlural();
    @Override public abstract String currencyNameSingular();

    @Override public abstract boolean hasAccount(String playerName);
    @Override public abstract boolean hasAccount(OfflinePlayer player);
    @Override public abstract boolean hasAccount(String playerName, String worldName);
    @Override public abstract boolean hasAccount(OfflinePlayer player, String worldName);

    @Override public abstract double getBalance(String playerName);
    @Override public abstract double getBalance(OfflinePlayer player);
    @Override public abstract double getBalance(String playerName, String world);
    @Override public abstract double getBalance(OfflinePlayer player, String world);

    @Override public abstract boolean has(String playerName, double amount);
    @Override public abstract boolean has(OfflinePlayer player, double amount);
    @Override public abstract boolean has(String playerName, String worldName, double amount);
    @Override public abstract boolean has(OfflinePlayer player, String worldName, double amount);

    @Override public abstract EconomyResponse withdrawPlayer(String playerName, double amount);
    @Override public abstract EconomyResponse withdrawPlayer(OfflinePlayer player, double amount);
    @Override public abstract EconomyResponse withdrawPlayer(String playerName, String worldName, double amount);
    @Override public abstract EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount);

    @Override public abstract EconomyResponse depositPlayer(String playerName, double amount);
    @Override public abstract EconomyResponse depositPlayer(OfflinePlayer player, double amount);
    @Override public abstract EconomyResponse depositPlayer(String playerName, String worldName, double amount);
    @Override public abstract EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount);

    @Override public abstract EconomyResponse createBank(String name, String player);
    @Override public abstract EconomyResponse createBank(String name, OfflinePlayer player);
    @Override public abstract EconomyResponse deleteBank(String name);
    @Override public abstract EconomyResponse bankBalance(String name);
    @Override public abstract EconomyResponse bankHas(String name, double amount);
    @Override public abstract EconomyResponse bankWithdraw(String name, double amount);
    @Override public abstract EconomyResponse bankDeposit(String name, double amount);
    @Override public abstract EconomyResponse isBankOwner(String name, String playerName);
    @Override public abstract EconomyResponse isBankOwner(String name, OfflinePlayer player);
    @Override public abstract EconomyResponse isBankMember(String name, String playerName);
    @Override public abstract EconomyResponse isBankMember(String name, OfflinePlayer player);

    @Override public abstract List<String> getBanks();

    @Override public abstract boolean createPlayerAccount(String playerName);
    @Override public abstract boolean createPlayerAccount(OfflinePlayer player);
    @Override public abstract boolean createPlayerAccount(String playerName, String worldName);
    @Override public abstract boolean createPlayerAccount(OfflinePlayer player, String worldName);
}
