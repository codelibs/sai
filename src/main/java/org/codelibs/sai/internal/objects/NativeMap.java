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
 * ECMAScript 2015 23.1, the Map built-in.
 */
@ScriptClass("Map")
public final class NativeMap extends ScriptObject {

    private final LinkedMap map = new LinkedMap();

    // initialized by saigen
    private static PropertyMap $saigenmap$;

    private NativeMap(final ScriptObject proto, final PropertyMap propertyMap) {
        super(proto, propertyMap);
    }

    /**
     * ECMA 23.1.1.1 Map ( [ iterable ] )
     *
     * @param newObj is this invoked with new
     * @param self   self reference
     * @param arg    an iterable of [key, value] pairs, or nothing
     * @return the new map
     */
    @Constructor(arity = 0)
    public static Object construct(final boolean newObj, final Object self, final Object arg) {
        if (!newObj) {
            throw typeError("constructor.requires.new", "Map");
        }
        final Global global = Global.instance();
        final NativeMap instance = new NativeMap(global.getMapPrototype(), $saigenmap$);
        // The adder is read off the instance rather than called directly, so that a
        // script replacing Map.prototype.set is honoured.
        CollectionSupport.addEntries(instance, arg, "set", true);
        return instance;
    }

    @Override
    public String getClassName() {
        return "Map";
    }

    /**
     * ECMA 23.1.3.9 get Map.prototype.size
     *
     * <p>The accessor sits on the instance rather than on the prototype, as it does
     * for every other built-in here that exposes per-instance state: a built-in
     * accessor found on a prototype is invoked with the prototype as its self, so a
     * prototype-level size could never see the map it was asked about.
     *
     * @param self self reference
     * @return the number of entries
     */
    @Getter(name = "size", attributes = Attribute.NOT_ENUMERABLE | Attribute.NOT_WRITABLE)
    public static Object size(final Object self) {
        return (double) checkMap(self).map.size();
    }

    /**
     * ECMA 23.1.3.9 Map.prototype.set ( key, value )
     *
     * @param self  self reference
     * @param key   the key
     * @param value the value
     * @return self, so that calls chain
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE)
    public static Object set(final Object self, final Object key, final Object value) {
        checkMap(self).map.set(LinkedMap.normalizeKey(key), value);
        return self;
    }

    /**
     * ECMA 23.1.3.6 Map.prototype.get ( key )
     *
     * @param self self reference
     * @param key  the key
     * @return the value stored under the key, or undefined
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object get(final Object self, final Object key) {
        final LinkedMap.Node node = checkMap(self).map.get(LinkedMap.normalizeKey(key));
        return node == null ? ScriptRuntime.UNDEFINED : node.getValue();
    }

    /**
     * ECMA 23.1.3.7 Map.prototype.has ( key )
     *
     * @param self self reference
     * @param key  the key
     * @return true if the key is present
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object has(final Object self, final Object key) {
        return checkMap(self).map.get(LinkedMap.normalizeKey(key)) != null;
    }

    /**
     * ECMA 23.1.3.3 Map.prototype.delete ( key )
     *
     * @param self self reference
     * @param key  the key
     * @return true if there was an entry to remove
     */
    @Function(name = "delete", arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object remove(final Object self, final Object key) {
        return checkMap(self).map.delete(LinkedMap.normalizeKey(key));
    }

    /**
     * ECMA 23.1.3.1 Map.prototype.clear ( )
     *
     * @param self self reference
     * @return undefined
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object clear(final Object self) {
        checkMap(self).map.clear();
        return ScriptRuntime.UNDEFINED;
    }

    /**
     * ECMA 23.1.3.5 Map.prototype.forEach ( callbackfn [ , thisArg ] )
     *
     * @param self     self reference
     * @param callback called with the value, the key and the map
     * @param thisArg  the this of the call
     * @return undefined
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object forEach(final Object self, final Object callback, final Object thisArg) {
        final NativeMap instance = checkMap(self);
        if (!(callback instanceof ScriptFunction)) {
            throw typeError("not.a.function", ScriptRuntime.safeToString(callback));
        }
        final ScriptFunction fn = (ScriptFunction) callback;
        final LinkedMap.Cursor cursor = instance.map.cursor();
        for (LinkedMap.Node node = cursor.next(); node != null; node = cursor.next()) {
            ScriptRuntime.apply(fn, thisArg, node.getValue(), LinkedMap.denormalizeKey(node.getKey()), instance);
        }
        return ScriptRuntime.UNDEFINED;
    }

    /**
     * ECMA 23.1.3.8 Map.prototype.keys ( )
     *
     * @param self self reference
     * @return an iterator over the keys
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object keys(final Object self) {
        return LinkedMapIterator.newMapIterator(checkMap(self).map, LinkedMapIterator.Kind.KEYS);
    }

    /**
     * ECMA 23.1.3.11 Map.prototype.values ( )
     *
     * @param self self reference
     * @return an iterator over the values
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object values(final Object self) {
        return LinkedMapIterator.newMapIterator(checkMap(self).map, LinkedMapIterator.Kind.VALUES);
    }

    /**
     * ECMA 23.1.3.4 Map.prototype.entries ( )
     *
     * @param self self reference
     * @return an iterator over the [key, value] pairs
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object entries(final Object self) {
        return LinkedMapIterator.newMapIterator(checkMap(self).map, LinkedMapIterator.Kind.ENTRIES);
    }

    /**
     * Iterating a map yields its entries, which is what the value iterator behind
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
                return new NativeArray(new Object[] { LinkedMap.denormalizeKey(node.getKey()), node.getValue() });
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }
        };
    }

    private static NativeMap checkMap(final Object self) {
        if (self instanceof NativeMap) {
            return (NativeMap) self;
        }
        throw typeError("not.a.map", ScriptRuntime.safeToString(self));
    }
}
