// @rdforward:preserve - hand-tuned facade, do not regenerate
package net.neoforged.fml.common;

import net.neoforged.api.distmarker.Dist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NeoForge's {@code @Mod} annotation. Shape matches Forge's but lives in
 * the new package. Loader accepts either annotation when scanning jars.
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
