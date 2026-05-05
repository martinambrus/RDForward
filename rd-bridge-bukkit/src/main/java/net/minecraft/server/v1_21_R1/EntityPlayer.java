package net.minecraft.server.v1_21_R1;

import com.github.martinambrus.rdforward.api.stub.StubCallLog;

/**
 * Stub for CS-CoreLib's {@code ReflectionUtils} which reads
 * {@code EntityPlayer.playerConnection} field and calls
 * {@code openBook(ItemStack, EnumHand)} (NMS method name "a").
 */
public class EntityPlayer {
    public PlayerConnection playerConnection;

    /** NMS-mapped name for openBook. CS-CoreLib looks for "a" first, then "openBook". */
    public void a(ItemStack stack, EnumHand hand) {
        StubCallLog.logOnce(null, "net.minecraft.server.v1_21_R1.EntityPlayer.a(Lnet/minecraft/server/v1_21_R1/ItemStack;Lnet/minecraft/server/v1_21_R1/EnumHand;)V");
    }

    public void openBook(ItemStack stack, EnumHand hand) {
        a(stack, hand);
    }
}
