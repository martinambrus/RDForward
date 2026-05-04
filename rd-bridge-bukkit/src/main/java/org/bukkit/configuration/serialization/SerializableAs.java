package org.bukkit.configuration.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a {@link ConfigurationSerializable} class with the alias used in
 *  the serialized {@code "=="} key. Read at runtime by
 *  {@link ConfigurationSerialization#getAlias(Class)}. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializableAs {
    String value();
}
