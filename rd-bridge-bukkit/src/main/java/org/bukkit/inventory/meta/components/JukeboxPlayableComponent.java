package org.bukkit.inventory.meta.components;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface JukeboxPlayableComponent extends org.bukkit.configuration.serialization.ConfigurationSerializable {
    org.bukkit.JukeboxSong getSong();
    org.bukkit.NamespacedKey getSongKey();
    void setSong(org.bukkit.JukeboxSong arg0);
    void setSongKey(org.bukkit.NamespacedKey arg0);
    default boolean isShowInTooltip() {
        return false;
    }
    default void setShowInTooltip(boolean arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.inventory.meta.components.JukeboxPlayableComponent.setShowInTooltip(Z)V");
    }
}
