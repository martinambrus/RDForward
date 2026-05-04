// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.milkbowl.vault.economy;

/**
 * Vault's response wrapper for every transaction call. Carrier shape
 * matches the public Vault 1.7 contract so plugins compiled against
 * Vault link cleanly: public final fields, nested {@link ResponseType}
 * enum, {@link #transactionSuccess()} accessor.
 */
public class EconomyResponse {

    public final double amount;
    public final double balance;
    public final ResponseType type;
    public final String errorMessage;

    public EconomyResponse(double amount, double balance, ResponseType type, String errorMessage) {
        this.amount = amount;
        this.balance = balance;
        this.type = type;
        this.errorMessage = errorMessage;
    }

    public boolean transactionSuccess() {
        return type == ResponseType.SUCCESS;
    }

    public enum ResponseType {
        SUCCESS(1),
        FAILURE(2),
        NOT_IMPLEMENTED(3);

        private final int id;

        ResponseType(int id) { this.id = id; }

        public int getId() { return id; }
    }
}
