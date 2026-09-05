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
 * The walk behind {@code String.prototype[Symbol.iterator]}, ES6 21.1.5.
 *
 * <p>It steps by code point rather than by code unit, so a character above the
 * basic plane is one step, and it reads the string it was handed once: a string
 * is immutable, so there is nothing to re-read.
 */
final class StringIterator extends AbstractIterator {

    private final String string;
    private int index;

    private StringIterator(final String string) {
        super(Global.instance().getStringIteratorPrototype());
        this.string = string;
    }

    /**
     * Build the iterator String.prototype[Symbol.iterator] returns.
     *
     * @param string the string being walked
     * @return the iterator object
     */
    static ScriptObject newIterator(final String string) {
        return new StringIterator(string);
    }

    @Override
    ScriptObject step() {
        if (this.index >= this.string.length()) {
            return result(ScriptRuntime.UNDEFINED, true);
        }

        final int start = this.index;

        this.index += Character.charCount(this.string.codePointAt(start));

        return result(this.string.substring(start, this.index), false);
    }
}
