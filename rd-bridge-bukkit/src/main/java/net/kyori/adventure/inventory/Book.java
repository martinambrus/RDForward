package net.kyori.adventure.inventory;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface Book extends net.kyori.adventure.util.Buildable, net.kyori.examination.Examinable {
    static net.kyori.adventure.inventory.Book book(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.Component arg1, java.util.Collection arg2) {
        return null;
    }
    static net.kyori.adventure.inventory.Book book(net.kyori.adventure.text.Component arg0, net.kyori.adventure.text.Component arg1, net.kyori.adventure.text.Component[] arg2) {
        return null;
    }
    static net.kyori.adventure.inventory.Book$Builder builder() {
        return null;
    }
    net.kyori.adventure.text.Component title();
    net.kyori.adventure.inventory.Book title(net.kyori.adventure.text.Component arg0);
    net.kyori.adventure.text.Component author();
    net.kyori.adventure.inventory.Book author(net.kyori.adventure.text.Component arg0);
    java.util.List pages();
    default net.kyori.adventure.inventory.Book pages(net.kyori.adventure.text.Component[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "net.kyori.adventure.inventory.Book.pages([Lnet/kyori/adventure/text/Component;)Lnet/kyori/adventure/inventory/Book;");
        return this;
    }
    net.kyori.adventure.inventory.Book pages(java.util.List arg0);
    default net.kyori.adventure.inventory.Book$Builder toBuilder() {
        return null;
    }
}
