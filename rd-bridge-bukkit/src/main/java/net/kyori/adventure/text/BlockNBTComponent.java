package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlockNBTComponent extends net.kyori.adventure.text.NBTComponent, net.kyori.adventure.text.ScopedComponent {
    net.kyori.adventure.text.BlockNBTComponent$Pos pos();
    net.kyori.adventure.text.BlockNBTComponent pos(net.kyori.adventure.text.BlockNBTComponent$Pos arg0);
    default net.kyori.adventure.text.BlockNBTComponent localPos(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent.localPos(DDD)Lnet/kyori/adventure/text/BlockNBTComponent;");
        return this;
    }
    default net.kyori.adventure.text.BlockNBTComponent worldPos(net.kyori.adventure.text.BlockNBTComponent$WorldPos$Coordinate arg0, net.kyori.adventure.text.BlockNBTComponent$WorldPos$Coordinate arg1, net.kyori.adventure.text.BlockNBTComponent$WorldPos$Coordinate arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent.worldPos(Lnet/kyori/adventure/text/BlockNBTComponent$WorldPos$Coordinate;Lnet/kyori/adventure/text/BlockNBTComponent$WorldPos$Coordinate;Lnet/kyori/adventure/text/BlockNBTComponent$WorldPos$Coordinate;)Lnet/kyori/adventure/text/BlockNBTComponent;");
        return this;
    }
    default net.kyori.adventure.text.BlockNBTComponent absoluteWorldPos(int arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent.absoluteWorldPos(III)Lnet/kyori/adventure/text/BlockNBTComponent;");
        return this;
    }
    default net.kyori.adventure.text.BlockNBTComponent relativeWorldPos(int arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent.relativeWorldPos(III)Lnet/kyori/adventure/text/BlockNBTComponent;");
        return this;
    }
    default java.util.stream.Stream examinableProperties() {
        return null;
    }
}
