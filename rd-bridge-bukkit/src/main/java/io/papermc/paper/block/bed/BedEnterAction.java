package io.papermc.paper.block.bed;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BedEnterAction {
    io.papermc.paper.block.bed.BedRuleResult canSleep();
    io.papermc.paper.block.bed.BedRuleResult canSetSpawn();
    io.papermc.paper.block.bed.BedEnterProblem problem();
    net.kyori.adventure.text.Component errorMessage();
}
