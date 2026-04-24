package io.papermc.paper.tag;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public abstract class BaseTag implements org.bukkit.Tag {
    public BaseTag(java.lang.Class arg0, org.bukkit.NamespacedKey arg1, java.util.function.Predicate arg2) {}
    public BaseTag(java.lang.Class arg0, org.bukkit.NamespacedKey arg1, org.bukkit.Keyed[] arg2) {}
    public BaseTag(java.lang.Class arg0, org.bukkit.NamespacedKey arg1, java.util.Collection arg2) {}
    public BaseTag(java.lang.Class arg0, org.bukkit.NamespacedKey arg1, java.util.Collection arg2, java.util.function.Predicate[] arg3) {}
    protected BaseTag() {}
    public io.papermc.paper.tag.BaseTag lock() {
        return null;
    }
    public boolean isLocked() {
        return false;
    }
    public org.bukkit.NamespacedKey getKey() {
        return null;
    }
    public java.util.Set getValues() {
        return java.util.Collections.emptySet();
    }
    public boolean isTagged(org.bukkit.Keyed arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.isTagged(Lorg/bukkit/Keyed;)Z");
        return false;
    }
    public io.papermc.paper.tag.BaseTag add(org.bukkit.Tag[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.add([Lorg/bukkit/Tag;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag add(org.bukkit.Keyed[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.add([Lorg/bukkit/Keyed;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag add(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.add(Ljava/util/Collection;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag add(java.util.function.Predicate arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.add(Ljava/util/function/Predicate;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag contains(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.contains(Ljava/lang/String;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag endsWith(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.endsWith(Ljava/lang/String;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag startsWith(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.startsWith(Ljava/lang/String;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag not(org.bukkit.Tag[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.not([Lorg/bukkit/Tag;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag not(org.bukkit.Keyed[] arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.not([Lorg/bukkit/Keyed;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag not(java.util.Collection arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.not(Ljava/util/Collection;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag not(java.util.function.Predicate arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.not(Ljava/util/function/Predicate;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag notContains(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.notContains(Ljava/lang/String;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag notEndsWith(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.notEndsWith(Ljava/lang/String;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag notStartsWith(java.lang.String arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.notStartsWith(Ljava/lang/String;)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    public io.papermc.paper.tag.BaseTag ensureSize(java.lang.String arg0, int arg1) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "io.papermc.paper.tag.BaseTag.ensureSize(Ljava/lang/String;I)Lio/papermc/paper/tag/BaseTag;");
        return this;
    }
    protected abstract java.util.Set getAllPossibleValues();
    protected abstract java.lang.String getName(org.bukkit.Keyed arg0);
}
