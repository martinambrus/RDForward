package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlockType$Typed extends org.bukkit.block.BlockType {
    java.lang.Class getBlockDataClass();
    org.bukkit.block.data.BlockData createBlockData(java.util.function.Consumer arg0);
    org.bukkit.block.data.BlockData createBlockData();
    java.util.Collection createBlockDataStates();
    org.bukkit.block.data.BlockData createBlockData(java.lang.String arg0);
}
