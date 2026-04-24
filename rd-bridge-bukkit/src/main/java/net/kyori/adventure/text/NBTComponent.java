package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface NBTComponent extends net.kyori.adventure.text.BuildableComponent {
    java.lang.String nbtPath();
    net.kyori.adventure.text.NBTComponent nbtPath(java.lang.String arg0);
    boolean interpret();
    net.kyori.adventure.text.NBTComponent interpret(boolean arg0);
    net.kyori.adventure.text.Component separator();
    net.kyori.adventure.text.NBTComponent separator(net.kyori.adventure.text.ComponentLike arg0);
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}
