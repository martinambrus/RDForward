package org.bukkit.entity;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Display extends org.bukkit.entity.Entity {
    org.bukkit.util.Transformation getTransformation();
    void setTransformation(org.bukkit.util.Transformation arg0);
    void setTransformationMatrix(org.joml.Matrix4f arg0);
    int getInterpolationDuration();
    void setInterpolationDuration(int arg0);
    int getTeleportDuration();
    void setTeleportDuration(int arg0);
    float getViewRange();
    void setViewRange(float arg0);
    float getShadowRadius();
    void setShadowRadius(float arg0);
    float getShadowStrength();
    void setShadowStrength(float arg0);
    float getDisplayWidth();
    void setDisplayWidth(float arg0);
    float getDisplayHeight();
    void setDisplayHeight(float arg0);
    int getInterpolationDelay();
    void setInterpolationDelay(int arg0);
    org.bukkit.entity.Display$Billboard getBillboard();
    void setBillboard(org.bukkit.entity.Display$Billboard arg0);
    org.bukkit.Color getGlowColorOverride();
    void setGlowColorOverride(org.bukkit.Color arg0);
    org.bukkit.entity.Display$Brightness getBrightness();
    void setBrightness(org.bukkit.entity.Display$Brightness arg0);
}
