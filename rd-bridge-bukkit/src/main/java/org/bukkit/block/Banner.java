package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Banner extends org.bukkit.block.TileState, org.bukkit.Nameable {
    org.bukkit.DyeColor getBaseColor();
    void setBaseColor(org.bukkit.DyeColor arg0);
    java.util.List getPatterns();
    void setPatterns(java.util.List arg0);
    void addPattern(org.bukkit.block.banner.Pattern arg0);
    org.bukkit.block.banner.Pattern getPattern(int arg0);
    org.bukkit.block.banner.Pattern removePattern(int arg0);
    void setPattern(int arg0, org.bukkit.block.banner.Pattern arg1);
    int numberOfPatterns();
}
