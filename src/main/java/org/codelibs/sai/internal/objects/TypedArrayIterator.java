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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.codelibs.sai.internal.dynalink.support.Lookup;
import org.codelibs.sai.internal.objects.annotations.Attribute;
import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The walk behind the object {@code %TypedArray%.prototype.keys}, {@code values} and
 * {@code entries} hand back.
 *
 * ES6 makes these three iterators, but this engine has no iterator protocol: for-of is
 * desugared to an index loop in the parser, spread and destructuring read by index too,
 * and there is no {@code Symbol.iterator} to hang an iterable off. The object built here
 * therefore does NOT drive for-of, spread or destructuring -
 * <pre>
 *     for (var v of new Int8Array([1, 2]).values()) { ... }   // does not iterate
 * </pre>
 * - and is only useful on its own, by calling {@code next()} until the result says done.
 */
final class TypedArrayIterator {
    /** What next() yields for each element. */
    enum Kind {
        /** the index */
        KEYS,
        /** the element */
        VALUES,
        /** a two element [index, element] array */
        ENTRIES
    }

    private static final MethodHandle NEXT = Lookup.findOwnStatic(MethodHandles.lookup(), "next", Object.class,
            TypedArrayIterator.class, Object.class);

    private final ArrayBufferView array;
    private final Kind kind;
    private int index;
    private boolean done;

    private TypedArrayIterator(final ArrayBufferView array, final Kind kind) {
        this.array = array;
        this.kind = kind;
    }

    /**
     * Build the object keys, values and entries return: an ordinary script object carrying
     * a next method bound to a fresh walk of the typed array.
     *
     * @param array the typed array being walked
     * @param kind  what next() yields for each element
     * @return the iterator object
     */
    static ScriptObject newIterator(final ArrayBufferView array, final Kind kind) {
        final ScriptObject iterator = Global.instance().newObject();

        iterator.addOwnProperty("next", Attribute.NOT_ENUMERABLE,
                ScriptFunction.createBuiltin("next", NEXT.bindTo(new TypedArrayIterator(array, kind))));

        return iterator;
    }

    @SuppressWarnings("unused")
    private static Object next(final TypedArrayIterator walk, final Object self) {
        final ScriptObject result = Global.instance().newObject();

        if (walk.done || walk.index >= walk.array.elementLength()) {
            // An exhausted iterator stays exhausted.
            walk.done = true;
            result.set("value", ScriptRuntime.UNDEFINED, 0);
            result.set("done", true, 0);
            return result;
        }

        final int at = walk.index++;
        final Object value;

        switch (walk.kind) {
        case KEYS:
            value = (double) at;
            break;
        case VALUES:
            value = walk.array.get(at);
            break;
        default:
            value = new NativeArray(new Object[] { (double) at, walk.array.get(at) });
            break;
        }

        result.set("value", value, 0);
        result.set("done", false, 0);

        return result;
    }
}
