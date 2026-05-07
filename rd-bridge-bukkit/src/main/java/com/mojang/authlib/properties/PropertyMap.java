// @rdforward:preserve - hand-tuned facade, do not regenerate
package com.mojang.authlib.properties;

import com.google.common.collect.ForwardingMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Mojang authlib stub. Real authlib's {@code PropertyMap} extends
 * {@code com.google.common.collect.ForwardingMultimap<String,Property>};
 * plugins routinely call {@code put}, {@code get}, {@code values}
 * and iterate the map. Mirroring the same supertype keeps the
 * Guava call surface intact so plugins compiled against authlib
 * link without rewrite.
 */
public class PropertyMap extends ForwardingMultimap<String, Property> {

    private final Multimap<String, Property> backing = LinkedHashMultimap.create();

    @Override
    protected Multimap<String, Property> delegate() { return backing; }
}
