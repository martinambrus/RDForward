package net.minecraftforge.fml.common;

import net.minecraftforge.api.distmarker.Dist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stub of Forge's {@code @Mod} annotation. Marks a class as the entrypoint
 * for a mod whose id matches {@link #value()}. {@link EventBusSubscriber}
 * nested annotation auto-registers a class's static handlers on either the
 * FORGE or MOD bus.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {

    String value();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface EventBusSubscriber {
        String modid() default "";
        Bus bus() default Bus.FORGE;
        Dist[] value() default { Dist.CLIENT, Dist.DEDICATED_SERVER };

        enum Bus { FORGE, MOD }
    }
}
