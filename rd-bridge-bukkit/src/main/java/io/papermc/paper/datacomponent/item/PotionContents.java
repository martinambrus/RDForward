package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface PotionContents {
    static io.papermc.paper.datacomponent.item.PotionContents$Builder potionContents() {
        return null;
    }
    org.bukkit.potion.PotionType potion();
    org.bukkit.Color customColor();
    java.util.List customEffects();
    java.lang.String customName();
    java.util.List allEffects();
    org.bukkit.Color computeEffectiveColor();
}
