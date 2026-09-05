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

import org.codelibs.sai.internal.objects.annotations.Attribute;
import org.codelibs.sai.internal.objects.annotations.Constructor;
import org.codelibs.sai.internal.objects.annotations.Function;
import org.codelibs.sai.internal.objects.annotations.Getter;
import org.codelibs.sai.internal.objects.annotations.ScriptClass;
import org.codelibs.sai.internal.runtime.LinkedMap;
import org.codelibs.sai.internal.runtime.PropertyMap;
import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * ECMAScript 2015 23.2, the Set built-in.
 *
 * <p>A set is a map whose values are its keys, so it shares {@link LinkedMap} with
 * {@link NativeMap} and with it the insertion order and the SameValueZero key
 * equality.
 */
@ScriptClass("Set")
public final class NativeSet extends ScriptObject {

    private final LinkedMap map = new LinkedMap();

    // initialized by saigen
    private static PropertyMap $saigenmap$;

    private NativeSet(final ScriptObject proto, final PropertyMap propertyMap) {
        super(proto, propertyMap);
    }

    /**
     * ECMA 23.2.1.1 Set ( [ iterable ] )
     *
     * @param newObj is this invoked with new
     * @param self   self reference
     * @param arg    an iterable of values, or nothing
     * @return the new set
     */
    @Constructor(arity = 0)
    public static Object construct(final boolean newObj, final Object self, final Object arg) {
        if (!newObj) {
            throw typeError("constructor.requires.new", "Set");
        }
        final Global global = Global.instance();
        final NativeSet instance = new NativeSet(global.getSetPrototype(), $saigenmap$);
        CollectionSupport.addEntries(instance, arg, "add", false);
        return instance;
    }

    @Override
    public String getClassName() {
        return "Set";
    }

    /**
     * ECMA 23.2.3.9 get Set.prototype.size
     *
     * <p>The accessor sits on the instance rather than on the prototype, as it does
     * for every other built-in here that exposes per-instance state: a built-in
     * accessor found on a prototype is invoked with the prototype as its self, so a
     * prototype-level size could never see the map it was asked about.
     *
     * @param self self reference
     * @return the number of values
     */
    @Getter(name = "size", attributes = Attribute.NOT_ENUMERABLE | Attribute.NOT_WRITABLE)
    public static Object size(final Object self) {
        return (double) checkSet(self).map.size();
    }

    /**
     * ECMA 23.2.3.1 Set.prototype.add ( value )
     *
     * @param self  self reference
     * @param value the value
     * @return self, so that calls chain
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object add(final Object self, final Object value) {
        final Object key = LinkedMap.normalizeKey(value);
        checkSet(self).map.set(key, LinkedMap.denormalizeKey(key));
        return self;
    }

    /**
     * ECMA 23.2.3.7 Set.prototype.has ( value )
     *
     * @param self  self reference
     * @param value the value
     * @return true if the value is present
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object has(final Object self, final Object value) {
        return checkSet(self).map.get(LinkedMap.normalizeKey(value)) != null;
    }

    /**
     * ECMA 23.2.3.4 Set.prototype.delete ( value )
     *
     * @param self  self reference
     * @param value the value
     * @return true if there was a value to remove
     */
    @Function(name = "delete", arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object remove(final Object self, final Object value) {
        return checkSet(self).map.delete(LinkedMap.normalizeKey(value));
    }

    /**
     * ECMA 23.2.3.2 Set.prototype.clear ( )
     *
     * @param self self reference
     * @return undefined
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object clear(final Object self) {
        checkSet(self).map.clear();
        return ScriptRuntime.UNDEFINED;
    }

    /**
     * ECMA 23.2.3.6 Set.prototype.forEach ( callbackfn [ , thisArg ] )
     *
     * @param self     self reference
     * @param callback called with the value twice, then the set
     * @param thisArg  the this of the call
     * @return undefined
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object forEach(final Object self, final Object callback, final Object thisArg) {
        final NativeSet instance = checkSet(self);
        if (!(callback instanceof ScriptFunction)) {
            throw typeError("not.a.function", ScriptRuntime.safeToString(callback));
        }
        final ScriptFunction fn = (ScriptFunction) callback;
        final LinkedMap.Cursor cursor = instance.map.cursor();
        for (LinkedMap.Node node = cursor.next(); node != null; node = cursor.next()) {
            // A set entry is its own key, which is why the value is passed twice.
            ScriptRuntime.apply(fn, thisArg, node.getValue(), node.getValue(), instance);
        }
        return ScriptRuntime.UNDEFINED;
    }

    /**
     * ECMA 23.2.3.8 Set.prototype.values ( ), and under 23.2.3.5 also its keys.
     *
     * @param self self reference
     * @return an iterator over the values
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object values(final Object self) {
        return LinkedMapIterator.newSetIterator(checkSet(self).map, LinkedMapIterator.Kind.VALUES);
    }

    /**
     * ECMA 23.2.3.5 Set.prototype.keys ( ), an alias of values.
     *
     * @param self self reference
     * @return an iterator over the values
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object keys(final Object self) {
        return LinkedMapIterator.newSetIterator(checkSet(self).map, LinkedMapIterator.Kind.VALUES);
    }

    /**
     * ECMA 23.2.3.3 Set.prototype.entries ( ), which yields each value paired with
     * itself.
     *
     * @param self self reference
     * @return an iterator over the [value, value] pairs
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object entries(final Object self) {
        return LinkedMapIterator.newSetIterator(checkSet(self).map, LinkedMapIterator.Kind.ENTRIES);
    }

    /**
     * Iterating a set yields its values, which is what the value iterator behind
     * for..of and the spread operator asks for.
     */
    @Override
    public Iterator<Object> valueIterator() {
        final LinkedMap.Cursor cursor = map.cursor();
        return new Iterator<Object>() {
            private LinkedMap.Node next = cursor.next();

            @Override
            public boolean hasNext() {
                return next != null;
            }

            @Override
            public Object next() {
                final LinkedMap.Node node = next;
                next = cursor.next();
                return node.getValue();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    private static NativeSet checkSet(final Object self) {
        if (self instanceof NativeSet) {
            return (NativeSet) self;
        }
        throw typeError("not.a.set", ScriptRuntime.safeToString(self));
    }
}
