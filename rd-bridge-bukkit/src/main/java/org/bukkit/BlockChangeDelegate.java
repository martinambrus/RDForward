package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlockChangeDelegate {
    boolean setBlockData(int arg0, int arg1, int arg2, org.bukkit.block.data.BlockData arg3);
    org.bukkit.block.data.BlockData getBlockData(int arg0, int arg1, int arg2);
    int getHeight();
    boolean isEmpty(int arg0, int arg1, int arg2);
}
