package io.papermc.paper.datacomponent.item.blocksattacks;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface ItemDamageFunction {
    static io.papermc.paper.datacomponent.item.blocksattacks.ItemDamageFunction$Builder itemDamageFunction() {
        return null;
    }
    float threshold();
    float base();
    float factor();
    int damageToApply(float arg0);
}
