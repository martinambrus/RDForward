package org.bukkit.block.data.type;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Switch extends org.bukkit.block.data.Directional, org.bukkit.block.data.FaceAttachable, org.bukkit.block.data.Powerable {
    org.bukkit.block.data.FaceAttachable$AttachedFace getAttachedFace();
    void setAttachedFace(org.bukkit.block.data.FaceAttachable$AttachedFace arg0);
    default org.bukkit.block.data.type.Switch$Face getFace() {
        return null;
    }
    default void setFace(org.bukkit.block.data.type.Switch$Face arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.data.type.Switch.setFace(Lorg/bukkit/block/data/type/Switch$Face;)V");
    }
}
