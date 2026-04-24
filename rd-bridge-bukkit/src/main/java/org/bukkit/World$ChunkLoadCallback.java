package org.bukkit;

/** Auto-generated stub from paper-api-26.1.2.build.20-alpha.jar. See PLAN-FULL-STUBS.md. */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public interface World$ChunkLoadCallback extends java.util.function.Consumer {
    void onLoad(org.bukkit.Chunk arg0);
    default void accept(org.bukkit.Chunk arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.World$ChunkLoadCallback.accept(Lorg/bukkit/Chunk;)V");
    }
    default void accept(java.lang.Object arg0) {
        com.github.martinambrus.rdforward.api.stub.StubCallLog.logOnce(null, "org.bukkit.World$ChunkLoadCallback.accept(Ljava/lang/Object;)V");
    }
}
