package net.kyori.adventure.sound;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Sound$Builder extends net.kyori.adventure.builder.AbstractBuilder {
    net.kyori.adventure.sound.Sound$Builder type(net.kyori.adventure.key.Key arg0);
    net.kyori.adventure.sound.Sound$Builder type(net.kyori.adventure.sound.Sound$Type arg0);
    net.kyori.adventure.sound.Sound$Builder type(java.util.function.Supplier arg0);
    net.kyori.adventure.sound.Sound$Builder source(net.kyori.adventure.sound.Sound$Source arg0);
    net.kyori.adventure.sound.Sound$Builder source(net.kyori.adventure.sound.Sound$Source$Provider arg0);
    net.kyori.adventure.sound.Sound$Builder volume(float arg0);
    net.kyori.adventure.sound.Sound$Builder pitch(float arg0);
    net.kyori.adventure.sound.Sound$Builder seed(long arg0);
    net.kyori.adventure.sound.Sound$Builder seed(java.util.OptionalLong arg0);
}
