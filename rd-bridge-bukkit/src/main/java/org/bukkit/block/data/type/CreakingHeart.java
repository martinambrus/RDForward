package org.bukkit.block.data.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CreakingHeart extends org.bukkit.block.data.Orientable {
    default boolean isActive() {
        return false;
    }
    default void setActive(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.type.CreakingHeart.setActive(Z)V");
    }
    org.bukkit.block.data.type.CreakingHeart$State getCreakingHeartState();
    void setCreakingHeartState(org.bukkit.block.data.type.CreakingHeart$State arg0);
    boolean isNatural();
    void setNatural(boolean arg0);
}
