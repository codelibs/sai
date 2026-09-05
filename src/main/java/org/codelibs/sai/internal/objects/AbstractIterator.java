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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.codelibs.sai.internal.dynalink.support.Lookup;
import org.codelibs.sai.internal.objects.annotations.Attribute;
import org.codelibs.sai.internal.runtime.PropertyMap;
import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * The walk behind every built-in iterator: the array, string, map and set ones.
 *
 * <p>ES6 25.1.2 puts {@code next} on a prototype shared by all iterators of a kind,
 * and puts those prototypes over one further object, %IteratorPrototype%, whose only
 * member is a {@code Symbol.iterator} returning the receiver. That is what makes an
 * iterator itself iterable, so
 * <pre>
 *     for (var v of [1, 2].values()) { ... }
 * </pre>
 * works. The chain is built here and handed out by {@link Global}.
 */
abstract class AbstractIterator extends ScriptObject {

    /** %IteratorPrototype% [ @@iterator ], which hands the receiver straight back. */
    static final MethodHandle SELF = Lookup.findOwnStatic(MethodHandles.lookup(), "self", Object.class, Object.class);

    private static final MethodHandle NEXT = Lookup.findOwnStatic(MethodHandles.lookup(), "next", Object.class,
            Object.class);

    AbstractIterator(final ScriptObject proto) {
        super(proto, PropertyMap.newMap());
    }

    /**
     * Take one step, and describe it the way ES6 25.1.1.2 asks: an object carrying
     * the value reached and whether the walk is over.
     *
     * @return the IteratorResult object
     */
    abstract ScriptObject step();

    /**
     * Build the IteratorResult ES6 7.4.7 describes.
     *
     * @param value what the step reached
     * @param done  whether the walk is over
     * @return the result object
     */
    static ScriptObject result(final Object value, final boolean done) {
        final ScriptObject result = Global.instance().newObject();

        result.set("value", value, 0);
        result.set("done", done, 0);

        return result;
    }

    /**
     * Build one of the %XIteratorPrototype% objects: the shared {@code next}, and the
     * tag Object.prototype.toString reads, over %IteratorPrototype%.
     *
     * @param iteratorPrototype %IteratorPrototype%, the object it sits on
     * @param tag               what Object.prototype.toString should name it
     * @return the prototype
     */
    static ScriptObject newPrototype(final ScriptObject iteratorPrototype, final String tag) {
        final ScriptObject prototype = Global.instance().newObject();

        prototype.setProto(iteratorPrototype);
        prototype.addOwnProperty("next", Attribute.NOT_ENUMERABLE, ScriptFunction.createBuiltin("next", NEXT));
        prototype.addOwnProperty(NativeSymbol.toStringTag, Attribute.NOT_ENUMERABLE | Attribute.NOT_WRITABLE, tag);

        return prototype;
    }

    @SuppressWarnings("unused")
    private static Object self(final Object self) {
        return self;
    }

    @SuppressWarnings("unused")
    private static Object next(final Object self) {
        if (self instanceof AbstractIterator) {
            return ((AbstractIterator) self).step();
        }
        throw typeError("not.an.iterator", ScriptRuntime.safeToString(self));
    }
}
