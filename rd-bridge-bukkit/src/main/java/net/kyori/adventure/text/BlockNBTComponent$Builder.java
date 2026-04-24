package net.kyori.adventure.text;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface BlockNBTComponent$Builder extends net.kyori.adventure.text.NBTComponentBuilder {
    net.kyori.adventure.text.BlockNBTComponent$Builder pos(net.kyori.adventure.text.BlockNBTComponent$Pos arg0);
    default net.kyori.adventure.text.BlockNBTComponent$Builder localPos(double arg0, double arg1, double arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent$Builder.localPos(DDD)Lnet/kyori/adventure/text/BlockNBTComponent$Builder;");
        return this;
    }
    default net.kyori.adventure.text.BlockNBTComponent$Builder worldPos(net.kyori.adventure.text.BlockNBTComponent$WorldPos$Coordinate arg0, net.kyori.adventure.text.BlockNBTComponent$WorldPos$Coordinate arg1, net.kyori.adventure.text.BlockNBTComponent$WorldPos$Coordinate arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent$Builder.worldPos(Lnet/kyori/adventure/text/BlockNBTComponent$WorldPos$Coordinate;Lnet/kyori/adventure/text/BlockNBTComponent$WorldPos$Coordinate;Lnet/kyori/adventure/text/BlockNBTComponent$WorldPos$Coordinate;)Lnet/kyori/adventure/text/BlockNBTComponent$Builder;");
        return this;
    }
    default net.kyori.adventure.text.BlockNBTComponent$Builder absoluteWorldPos(int arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent$Builder.absoluteWorldPos(III)Lnet/kyori/adventure/text/BlockNBTComponent$Builder;");
        return this;
    }
    default net.kyori.adventure.text.BlockNBTComponent$Builder relativeWorldPos(int arg0, int arg1, int arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.text.BlockNBTComponent$Builder.relativeWorldPos(III)Lnet/kyori/adventure/text/BlockNBTComponent$Builder;");
        return this;
    }
}
