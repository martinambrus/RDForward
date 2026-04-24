package org.bukkit.block.data.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Vault extends org.bukkit.block.data.Directional {
    org.bukkit.block.data.type.Vault$State getVaultState();
    default org.bukkit.block.data.type.Vault$State getTrialSpawnerState() {
        return null;
    }
    void setVaultState(org.bukkit.block.data.type.Vault$State arg0);
    default void setTrialSpawnerState(org.bukkit.block.data.type.Vault$State arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.type.Vault.setTrialSpawnerState(Lorg/bukkit/block/data/type/Vault$State;)V");
    }
    boolean isOminous();
    void setOminous(boolean arg0);
}
