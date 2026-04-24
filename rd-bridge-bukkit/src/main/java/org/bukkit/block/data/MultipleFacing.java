package org.bukkit.block.data;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface MultipleFacing extends org.bukkit.block.data.BlockData {
    boolean hasFace(org.bukkit.block.BlockFace arg0);
    void setFace(org.bukkit.block.BlockFace arg0, boolean arg1);
    java.util.Set getFaces();
    java.util.Set getAllowedFaces();
}
