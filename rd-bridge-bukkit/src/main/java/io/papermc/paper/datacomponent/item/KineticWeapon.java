package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface KineticWeapon {
    static io.papermc.paper.datacomponent.item.KineticWeapon$Builder kineticWeapon() {
        return null;
    }
    static io.papermc.paper.datacomponent.item.KineticWeapon$Condition condition(int arg0, float arg1, float arg2) {
        return null;
    }
    int contactCooldownTicks();
    int delayTicks();
    io.papermc.paper.datacomponent.item.KineticWeapon$Condition dismountConditions();
    io.papermc.paper.datacomponent.item.KineticWeapon$Condition knockbackConditions();
    io.papermc.paper.datacomponent.item.KineticWeapon$Condition damageConditions();
    float forwardMovement();
    float damageMultiplier();
    net.kyori.adventure.key.Key sound();
    net.kyori.adventure.key.Key hitSound();
}
