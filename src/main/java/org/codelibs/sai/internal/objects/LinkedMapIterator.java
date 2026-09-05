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

import org.codelibs.sai.internal.runtime.LinkedMap;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The iterator Map.prototype and Set.prototype hand out from keys, values and
 * entries. It walks a {@link LinkedMap} through a cursor, so entries deleted
 * before it arrives are skipped and ones appended while it runs are visited.
 *
 * <p>ES6 23.1.5 and 23.2.5 give the two their own prototypes, so that
 * Object.prototype.toString names a map's walk and a set's walk apart.
 */
final class LinkedMapIterator extends AbstractIterator {

    /** What next() yields for each entry. */
    enum Kind {
        /** the key */
        KEYS,
        /** the value */
        VALUES,
        /** a two element [key, value] array */
        ENTRIES
    }

    private final LinkedMap.Cursor cursor;
    private final Kind kind;
    private boolean done;

    private LinkedMapIterator(final LinkedMap map, final Kind kind, final ScriptObject proto) {
        super(proto);
        this.cursor = map.cursor();
        this.kind = kind;
    }

    /**
     * Build the iterator Map.prototype.keys, values and entries return.
     *
     * @param map  the map being walked
     * @param kind what next() yields for each entry
     * @return the iterator object
     */
    static ScriptObject newMapIterator(final LinkedMap map, final Kind kind) {
        return new LinkedMapIterator(map, kind, Global.instance().getMapIteratorPrototype());
    }

    /**
     * Build the iterator Set.prototype.keys, values and entries return.
     *
     * @param map  the map behind the set being walked
     * @param kind what next() yields for each entry
     * @return the iterator object
     */
    static ScriptObject newSetIterator(final LinkedMap map, final Kind kind) {
        return new LinkedMapIterator(map, kind, Global.instance().getSetIteratorPrototype());
    }

    @Override
    ScriptObject step() {
        final LinkedMap.Node node = this.done ? null : this.cursor.next();

        if (node == null) {
            // An exhausted iterator stays exhausted, even if the map grows later.
            this.done = true;
            return result(ScriptRuntime.UNDEFINED, true);
        }

        final Object key = LinkedMap.denormalizeKey(node.getKey());

        switch (this.kind) {
        case KEYS:
            return result(key, false);
        case VALUES:
            return result(node.getValue(), false);
        default:
            return result(new NativeArray(new Object[] { key, node.getValue() }), false);
        }
    }
}
