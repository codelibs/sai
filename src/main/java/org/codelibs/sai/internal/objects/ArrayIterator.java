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
import org.codelibs.sai.internal.runtime.JSType;
import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The walk behind the object Array.prototype.keys, values and entries hand back.
 *
 * ES6 makes these three iterators, but this engine has no iterator protocol:
 * for-of is desugared to an index loop in the parser, spread and destructuring
 * read by index too, and there is no Symbol.iterator to hang an iterable off. The
 * object built here therefore does NOT drive for-of, spread or destructuring -
 * <pre>
 *     for (var v of [1, 2].values()) { ... }   // does not iterate
 * </pre>
 * - and is only useful on its own, by calling next() until the result says done.
 * Wiring it into for-of needs a redesign of for-of itself, which is a separate
 * piece of work.
 */
final class ArrayIterator {
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
            ArrayIterator.class, Object.class);

    private final ScriptObject array;
    private final Kind kind;
    private long index;
    private boolean done;

    private ArrayIterator(final ScriptObject array, final Kind kind) {
        this.array = array;
        this.kind = kind;
    }

    /**
     * Build the object Array.prototype.keys, values and entries return: an ordinary
     * script object carrying a next method bound to a fresh walk of the array.
     *
     * @param array the array being walked
     * @param kind  what next() yields for each element
     * @return the iterator object
     */
    static ScriptObject newIterator(final ScriptObject array, final Kind kind) {
        final ScriptObject iterator = Global.instance().newObject();

        iterator.addOwnProperty("next", Attribute.NOT_ENUMERABLE,
                ScriptFunction.createBuiltin("next", NEXT.bindTo(new ArrayIterator(array, kind))));

        return iterator;
    }

    @SuppressWarnings("unused")
    private static Object next(final ArrayIterator walk, final Object self) {
        final ScriptObject result = Global.instance().newObject();
        final long length = JSType.toUint32(walk.array.getLength());

        if (walk.done || walk.index >= length) {
            // An exhausted iterator stays exhausted, even if the array grows later.
            walk.done = true;
            result.set("value", ScriptRuntime.UNDEFINED, 0);
            result.set("done", true, 0);
            return result;
        }

        final long at = walk.index++;
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
