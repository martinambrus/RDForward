package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Interaction extends org.bukkit.entity.Entity {
    float getInteractionWidth();
    void setInteractionWidth(float arg0);
    float getInteractionHeight();
    void setInteractionHeight(float arg0);
    boolean isResponsive();
    void setResponsive(boolean arg0);
    org.bukkit.entity.Interaction$PreviousInteraction getLastAttack();
    org.bukkit.entity.Interaction$PreviousInteraction getLastInteraction();
}
