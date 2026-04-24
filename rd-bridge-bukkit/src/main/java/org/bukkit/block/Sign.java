package org.bukkit.block;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Sign extends org.bukkit.block.TileState, org.bukkit.material.Colorable {
    java.util.List lines();
    net.kyori.adventure.text.Component line(int arg0) throws java.lang.IndexOutOfBoundsException;
    void line(int arg0, net.kyori.adventure.text.Component arg1) throws java.lang.IndexOutOfBoundsException;
    java.lang.String[] getLines();
    java.lang.String getLine(int arg0) throws java.lang.IndexOutOfBoundsException;
    void setLine(int arg0, java.lang.String arg1) throws java.lang.IndexOutOfBoundsException;
    boolean isEditable();
    void setEditable(boolean arg0);
    boolean isWaxed();
    void setWaxed(boolean arg0);
    boolean isGlowingText();
    void setGlowingText(boolean arg0);
    org.bukkit.DyeColor getColor();
    void setColor(org.bukkit.DyeColor arg0);
    org.bukkit.block.sign.SignSide getSide(org.bukkit.block.sign.Side arg0);
    org.bukkit.block.sign.SignSide getTargetSide(org.bukkit.entity.Player arg0);
    org.bukkit.entity.Player getAllowedEditor();
    java.util.UUID getAllowedEditorUniqueId();
    void setAllowedEditorUniqueId(java.util.UUID arg0);
    default org.bukkit.block.sign.Side getInteractableSideFor(org.bukkit.entity.Entity arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.Sign.getInteractableSideFor(Lorg/bukkit/entity/Entity;)Lorg/bukkit/block/sign/Side;");
        return null;
    }
    default org.bukkit.block.sign.Side getInteractableSideFor(io.papermc.paper.math.Position arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.block.Sign.getInteractableSideFor(Lio/papermc/paper/math/Position;)Lorg/bukkit/block/sign/Side;");
        return null;
    }
    org.bukkit.block.sign.Side getInteractableSideFor(double arg0, double arg1);
}
