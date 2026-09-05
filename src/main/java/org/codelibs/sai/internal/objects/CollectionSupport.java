/*
 * Copyright (c) 2026, CodeLibs Project and/or its affiliates. All rights reserved.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */


package org.codelibs.sai.internal.objects;

import static org.codelibs.sai.internal.runtime.ECMAErrors.typeError;

import java.util.Iterator;

import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The part of the Map, Set, WeakMap and WeakSet constructors that fills a fresh
 * collection from the optional iterable argument.
 */
final class CollectionSupport {

    private CollectionSupport() {
    }

    /**
     * ECMA 23.1.1.2 AddEntriesFromIterable, and the equivalent step in the three
     * sibling constructors.
     *
     * <p>The adder is read off the instance rather than called directly, so that a
     * script that has replaced {@code Map.prototype.set} sees its function called,
     * as the specification requires.
     *
     * @param instance  the collection being filled
     * @param iterable  the constructor argument; null and undefined fill nothing
     * @param adderName the method to call for each element, "set" or "add"
     * @param pairs     true when each element is a [key, value] pair
     */
    static void addEntries(final ScriptObject instance, final Object iterable, final String adderName,
            final boolean pairs) {
        if (iterable == null || iterable == ScriptRuntime.UNDEFINED) {
            return;
        }

        final Object adder = instance.get(adderName);
        if (!(adder instanceof ScriptFunction)) {
            throw typeError("not.a.function", ScriptRuntime.safeToString(adder));
        }
        final ScriptFunction fn = (ScriptFunction) adder;

        final Iterator<?> elements = ScriptRuntime.toValueIterator(iterable);
        while (elements.hasNext()) {
            final Object element = elements.next();
            if (!pairs) {
                ScriptRuntime.apply(fn, instance, element);
            } else if (element instanceof ScriptObject) {
                final ScriptObject pair = (ScriptObject) element;
                ScriptRuntime.apply(fn, instance, pair.get(0), pair.get(1));
            } else {
                throw typeError("not.an.object", ScriptRuntime.safeToString(element));
            }
        }
    }
}
