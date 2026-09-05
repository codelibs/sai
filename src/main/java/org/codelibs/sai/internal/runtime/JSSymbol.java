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

package org.codelibs.sai.internal.runtime;

import java.io.Serializable;

/**
 * An ES2015 symbol: a property key that is not a name, and a primitive value in
 * its own right.
 *
 * <p>Two symbols are the same only when they are the same object, which is what
 * makes a symbol usable as a key nothing else can guess. The class deliberately
 * inherits equals and hashCode from Object, since {@link PropertyHashMap} reaches
 * a key through exactly those two.
 *
 * <p>The description is for reading, not for identity: two symbols made with the
 * same description are still two symbols.
 */
public final class JSSymbol implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String description;

    /**
     * Creates a symbol.
     *
     * @param description a description to show when the symbol is printed, or null
     */
    public JSSymbol(final String description) {
        this.description = description;
    }

    /**
     * Returns the description this symbol was made with.
     *
     * @return the description, or null
     */
    public String getDescription() {
        return description;
    }

    /**
     * ECMA 19.4.3.3 SymbolDescriptiveString, which is what String() and the
     * prototype's toString both answer with.
     *
     * @return the symbol written out, as in Symbol(foo)
     */
    @Override
    public String toString() {
        return "Symbol(" + (description == null ? "" : description) + ")";
    }
}
