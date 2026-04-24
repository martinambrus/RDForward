package net.kyori.adventure.audience;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
class EmptyAudience implements net.kyori.adventure.audience.Audience {
    public EmptyAudience() {}
    public java.util.Optional get(net.kyori.adventure.pointer.Pointer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.get(Lnet/kyori/adventure/pointer/Pointer;)Ljava/util/Optional;");
        return java.util.Optional.empty();
    }
    public java.lang.Object getOrDefault(net.kyori.adventure.pointer.Pointer arg0, java.lang.Object arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.getOrDefault(Lnet/kyori/adventure/pointer/Pointer;Ljava/lang/Object;)Ljava/lang/Object;");
        return null;
    }
    public java.lang.Object getOrDefaultFrom(net.kyori.adventure.pointer.Pointer arg0, java.util.function.Supplier arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.getOrDefaultFrom(Lnet/kyori/adventure/pointer/Pointer;Ljava/util/function/Supplier;)Ljava/lang/Object;");
        return null;
    }
    public net.kyori.adventure.audience.Audience filterAudience(java.util.function.Predicate arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.filterAudience(Ljava/util/function/Predicate;)Lnet/kyori/adventure/audience/Audience;");
        return null;
    }
    public void forEachAudience(java.util.function.Consumer arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.forEachAudience(Ljava/util/function/Consumer;)V");
    }
    public void sendMessage(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendMessage(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    public void sendMessage(net.kyori.adventure.text.Component arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendMessage(Lnet/kyori/adventure/text/Component;)V");
    }
    public void sendMessage(net.kyori.adventure.identity.Identified arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendMessage(Lnet/kyori/adventure/identity/Identified;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    public void sendMessage(net.kyori.adventure.identity.Identity arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.audience.MessageType arg2) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendMessage(Lnet/kyori/adventure/identity/Identity;Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/audience/MessageType;)V");
    }
    public void sendMessage(net.kyori.adventure.text.Component arg0, net.kyori.adventure.chat.ChatType$Bound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendMessage(Lnet/kyori/adventure/text/Component;Lnet/kyori/adventure/chat/ChatType$Bound;)V");
    }
    public void sendMessage(net.kyori.adventure.chat.SignedMessage arg0, net.kyori.adventure.chat.ChatType$Bound arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendMessage(Lnet/kyori/adventure/chat/SignedMessage;Lnet/kyori/adventure/chat/ChatType$Bound;)V");
    }
    public void deleteMessage(net.kyori.adventure.chat.SignedMessage$Signature arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.deleteMessage(Lnet/kyori/adventure/chat/SignedMessage$Signature;)V");
    }
    public void sendActionBar(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendActionBar(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    public void sendPlayerListHeader(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendPlayerListHeader(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    public void sendPlayerListFooter(net.kyori.adventure.text.ComponentLike arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendPlayerListFooter(Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    public void sendPlayerListHeaderAndFooter(net.kyori.adventure.text.ComponentLike arg0, net.kyori.adventure.text.ComponentLike arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendPlayerListHeaderAndFooter(Lnet/kyori/adventure/text/ComponentLike;Lnet/kyori/adventure/text/ComponentLike;)V");
    }
    public void openBook(net.kyori.adventure.inventory.Book$Builder arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.openBook(Lnet/kyori/adventure/inventory/Book$Builder;)V");
    }
    public void sendResourcePacks(net.kyori.adventure.resource.ResourcePackInfoLike arg0, net.kyori.adventure.resource.ResourcePackInfoLike[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.sendResourcePacks(Lnet/kyori/adventure/resource/ResourcePackInfoLike;[Lnet/kyori/adventure/resource/ResourcePackInfoLike;)V");
    }
    public void removeResourcePacks(net.kyori.adventure.resource.ResourcePackRequest arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.removeResourcePacks(Lnet/kyori/adventure/resource/ResourcePackRequest;)V");
    }
    public void removeResourcePacks(net.kyori.adventure.resource.ResourcePackInfoLike arg0, net.kyori.adventure.resource.ResourcePackInfoLike[] arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.removeResourcePacks(Lnet/kyori/adventure/resource/ResourcePackInfoLike;[Lnet/kyori/adventure/resource/ResourcePackInfoLike;)V");
    }
    public boolean equals(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.audience.EmptyAudience.equals(Ljava/lang/Object;)Z");
        return false;
    }
    public int hashCode() {
        return 0;
    }
    public java.lang.String toString() {
        return null;
    }
}
