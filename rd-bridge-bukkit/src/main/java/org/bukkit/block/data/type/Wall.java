package org.bukkit.block.data.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Wall extends org.bukkit.block.data.Waterlogged {
    boolean isUp();
    void setUp(boolean arg0);
    org.bukkit.block.data.type.Wall$Height getHeight(org.bukkit.block.BlockFace arg0);
    void setHeight(org.bukkit.block.BlockFace arg0, org.bukkit.block.data.type.Wall$Height arg1);
}
