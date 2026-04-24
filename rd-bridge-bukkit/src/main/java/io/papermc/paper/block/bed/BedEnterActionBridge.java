package io.papermc.paper.block.bed;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
interface BedEnterActionBridge {
    static io.papermc.paper.block.bed.BedEnterActionBridge instance() {
        return null;
    }
    io.papermc.paper.block.bed.BedEnterProblem createTooFarAwayProblem();
    io.papermc.paper.block.bed.BedEnterProblem createObstructedProblem();
    io.papermc.paper.block.bed.BedEnterProblem createNotSafeProblem();
    io.papermc.paper.block.bed.BedEnterProblem createExplosionProblem();
    io.papermc.paper.block.bed.BedEnterProblem createOtherProblem();
}
