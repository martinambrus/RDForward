package io.papermc.paper.datacomponent.item;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface WrittenBookContent {
    static io.papermc.paper.datacomponent.item.WrittenBookContent$Builder writtenBookContent(java.lang.String arg0, java.lang.String arg1) {
        return null;
    }
    static io.papermc.paper.datacomponent.item.WrittenBookContent$Builder writtenBookContent(io.papermc.paper.text.Filtered arg0, java.lang.String arg1) {
        return null;
    }
    io.papermc.paper.text.Filtered title();
    java.lang.String author();
    int generation();
    java.util.List pages();
    boolean resolved();
}
