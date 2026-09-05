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

import org.codelibs.sai.internal.runtime.JSType;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The walk behind the object Array.prototype.keys, values and entries hand back,
 * and the one %TypedArray%.prototype hands back too - ES6 22.2.3.30 gives a typed
 * array the very same iterator an array has.
 */
final class ArrayIterator extends AbstractIterator {
    /** What next() yields for each element. */
    enum Kind {
        /** the index */
        KEYS,
        /** the element */
        VALUES,
        /** a two element [index, element] array */
        ENTRIES
    }

    private final ScriptObject array;
    private final Kind kind;
    private long index;
    private boolean done;

    private ArrayIterator(final ScriptObject array, final Kind kind) {
        super(Global.instance().getArrayIteratorPrototype());
        this.array = array;
        this.kind = kind;
    }

    /**
     * Build the iterator Array.prototype.keys, values and entries return.
     *
     * @param array the array being walked
     * @param kind  what next() yields for each element
     * @return the iterator object
     */
    static ScriptObject newIterator(final ScriptObject array, final Kind kind) {
        return new ArrayIterator(array, kind);
    }

    @Override
    ScriptObject step() {
        final long length = JSType.toUint32(this.array.getLength());

        if (this.done || this.index >= length) {
            // An exhausted iterator stays exhausted, even if the array grows later.
            this.done = true;
            return result(ScriptRuntime.UNDEFINED, true);
        }

        final long at = this.index++;

        switch (this.kind) {
        case KEYS:
            return result((double) at, false);
        case VALUES:
            return result(this.array.get(at), false);
        default:
            return result(new NativeArray(new Object[] { (double) at, this.array.get(at) }), false);
        }
    }
}
