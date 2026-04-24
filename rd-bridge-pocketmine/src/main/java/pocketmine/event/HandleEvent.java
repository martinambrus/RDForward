package pocketmine.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bridge-local replacement for PocketMine's PHPDoc {@code @priority NORMAL}
 * and {@code @ignoreCancelled true} tags. Java has no equivalent of
 * docblock-directed reflection, so we normalise those attributes as a
 * Java annotation. Port authors move the PHPDoc values directly onto the
 * corresponding Java method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HandleEvent {

    EventPriority priority() default EventPriority.NORMAL;

    boolean ignoreCancelled() default false;

    enum EventPriority {
        LOWEST, LOW, NORMAL, HIGH, HIGHEST, MONITOR
    }
}
