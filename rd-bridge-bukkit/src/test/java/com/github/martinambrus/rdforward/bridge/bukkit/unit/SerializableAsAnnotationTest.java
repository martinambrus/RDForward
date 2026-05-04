package com.github.martinambrus.rdforward.bridge.bukkit.unit;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies SerializableAs annotation has runtime retention and type
 * target. ConfigurationSerialization.getAlias() reflectively reads
 * this annotation — without @Retention(RUNTIME) it vanishes at runtime
 * and aliases silently fail.
 */
class SerializableAsAnnotationTest {

    @Test
    void annotationHasRuntimeRetention() {
        Retention retention = SerializableAs.class.getAnnotation(Retention.class);
        assertNotNull(retention, "SerializableAs must be annotated with @Retention");
        assertEquals(RetentionPolicy.RUNTIME, retention.value(),
                "@Retention must be RUNTIME so getAlias() can read it reflectively");
    }

    @Test
    void annotationTargetsType() {
        Target target = SerializableAs.class.getAnnotation(Target.class);
        assertNotNull(target, "SerializableAs must be annotated with @Target");
        assertArrayEquals(new ElementType[]{ElementType.TYPE}, target.value(),
                "@Target must be TYPE — serializable types are classes");
    }

    @Test
    void annotationValueAccessible() {
        @SerializableAs("myAlias")
        class Dummy implements ConfigurationSerializable {
            @Override public Map<String, Object> serialize() { return Map.of(); }
        }
        SerializableAs sa = Dummy.class.getAnnotation(SerializableAs.class);
        assertNotNull(sa);
        assertEquals("myAlias", sa.value());
    }
}
