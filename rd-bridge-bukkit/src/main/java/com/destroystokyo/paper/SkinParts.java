package com.destroystokyo.paper;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface SkinParts {
    static com.destroystokyo.paper.SkinParts$Mutable allParts() {
        return null;
    }
    boolean hasCapeEnabled();
    boolean hasJacketEnabled();
    boolean hasLeftSleeveEnabled();
    boolean hasRightSleeveEnabled();
    boolean hasLeftPantsEnabled();
    boolean hasRightPantsEnabled();
    boolean hasHatsEnabled();
    int getRaw();
    com.destroystokyo.paper.SkinParts$Mutable mutableCopy();
}
