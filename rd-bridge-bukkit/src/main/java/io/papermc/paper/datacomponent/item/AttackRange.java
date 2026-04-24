package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface AttackRange {
    static io.papermc.paper.datacomponent.item.AttackRange$Builder attackRange() {
        return null;
    }
    float minReach();
    float maxReach();
    float minCreativeReach();
    float maxCreativeReach();
    float hitboxMargin();
    float mobFactor();
}
