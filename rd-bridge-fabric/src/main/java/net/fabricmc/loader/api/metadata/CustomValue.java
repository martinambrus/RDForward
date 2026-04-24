package net.fabricmc.loader.api.metadata;

/** Auto-generated stub from fabric-loader-0.18.4.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface CustomValue {
    net.fabricmc.loader.api.metadata.CustomValue$CvType getType();
    net.fabricmc.loader.api.metadata.CustomValue$CvObject getAsObject();
    net.fabricmc.loader.api.metadata.CustomValue$CvArray getAsArray();
    java.lang.String getAsString();
    java.lang.Number getAsNumber();
    boolean getAsBoolean();
}
