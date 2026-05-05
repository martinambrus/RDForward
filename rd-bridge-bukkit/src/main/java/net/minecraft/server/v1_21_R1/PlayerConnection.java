package net.minecraft.server.v1_21_R1;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;

/**
 * Stub for CS-CoreLib's {@code ReflectionUtils} which calls
 * {@code PlayerConnection.sendPacket(Object)}.
 */
public class PlayerConnection {
    public void sendPacket(Object packet) {
        StubCallLog.logOnce(null, "net.minecraft.server.v1_21_R1.PlayerConnection.sendPacket(Ljava/lang/Object;)V");
    }
}
