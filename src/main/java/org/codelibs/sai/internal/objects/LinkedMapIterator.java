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
import org.codelibs.sai.internal.runtime.LinkedMap;
import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The iterator Map.prototype and Set.prototype hand out from keys, values and
 * entries. It walks a {@link LinkedMap} through a cursor, so entries deleted
 * before it arrives are skipped and ones appended while it runs are visited.
 */
final class LinkedMapIterator {

    /** What next() yields for each entry. */
    enum Kind {
        /** the key */
        KEYS,
        /** the value */
        VALUES,
        /** a two element [key, value] array */
        ENTRIES
    }

    private static final MethodHandle NEXT = Lookup.findOwnStatic(MethodHandles.lookup(), "next", Object.class,
            LinkedMapIterator.class, Object.class);

    private final LinkedMap.Cursor cursor;
    private final Kind kind;
    private boolean done;

    private LinkedMapIterator(final LinkedMap map, final Kind kind) {
        this.cursor = map.cursor();
        this.kind = kind;
    }

    /**
     * Build the object keys, values and entries return: an ordinary script object
     * carrying a next method bound to a fresh walk of the map.
     *
     * @param map  the map being walked
     * @param kind what next() yields for each entry
     * @return the iterator object
     */
    static ScriptObject newIterator(final LinkedMap map, final Kind kind) {
        final ScriptObject iterator = Global.instance().newObject();

        iterator.addOwnProperty("next", Attribute.NOT_ENUMERABLE,
                ScriptFunction.createBuiltin("next", NEXT.bindTo(new LinkedMapIterator(map, kind))));

        return iterator;
    }

    @SuppressWarnings("unused")
    private static Object next(final LinkedMapIterator walk, final Object self) {
        final ScriptObject result = Global.instance().newObject();
        final LinkedMap.Node node = walk.done ? null : walk.cursor.next();

        if (node == null) {
            // An exhausted iterator stays exhausted, even if the map grows later.
            walk.done = true;
            result.set("value", ScriptRuntime.UNDEFINED, 0);
            result.set("done", true, 0);
            return result;
        }

        final Object key = LinkedMap.denormalizeKey(node.getKey());
        final Object value;

        switch (walk.kind) {
        case KEYS:
            value = key;
            break;
        case VALUES:
            value = node.getValue();
            break;
        default:
            value = new NativeArray(new Object[] { key, node.getValue() });
            break;
        }

        result.set("value", value, 0);
        result.set("done", false, 0);
        return result;
    }
}
