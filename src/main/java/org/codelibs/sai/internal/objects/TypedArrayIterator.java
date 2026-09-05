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

import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The walk behind the object {@code %TypedArray%.prototype.keys}, {@code values} and
 * {@code entries} hand back. It reads the elements straight off the view rather than
 * through the generic array path, so a detached or resized buffer stops it.
 */
final class TypedArrayIterator extends AbstractIterator {
    /** What next() yields for each element. */
    enum Kind {
        /** the index */
        KEYS,
        /** the element */
        VALUES,
        /** a two element [index, element] array */
        ENTRIES
    }

    private final ArrayBufferView array;
    private final Kind kind;
    private int index;
    private boolean done;

    private TypedArrayIterator(final ArrayBufferView array, final Kind kind) {
        super(Global.instance().getArrayIteratorPrototype());
        this.array = array;
        this.kind = kind;
    }

    /**
     * Build the iterator keys, values and entries return.
     *
     * @param array the typed array being walked
     * @param kind  what next() yields for each element
     * @return the iterator object
     */
    static ScriptObject newIterator(final ArrayBufferView array, final Kind kind) {
        return new TypedArrayIterator(array, kind);
    }

    @Override
    ScriptObject step() {
        if (this.done || this.index >= this.array.elementLength()) {
            // An exhausted iterator stays exhausted.
            this.done = true;
            return result(ScriptRuntime.UNDEFINED, true);
        }

        final int at = this.index++;

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
