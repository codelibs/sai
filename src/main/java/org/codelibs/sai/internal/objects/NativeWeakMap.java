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

import java.util.Map;
import java.util.WeakHashMap;

import org.codelibs.sai.internal.objects.annotations.Attribute;
import org.codelibs.sai.internal.objects.annotations.Constructor;
import org.codelibs.sai.internal.objects.annotations.Function;
import org.codelibs.sai.internal.objects.annotations.ScriptClass;
import org.codelibs.sai.internal.runtime.JSType;
import org.codelibs.sai.internal.runtime.PropertyMap;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;

/**
 * ECMAScript 2015 23.3, the WeakMap built-in.
 *
 * <p>Only an object can be a key, and the map holds it weakly, so an entry stops
 * keeping its key alive once nothing else refers to it. There is no way to ask a
 * WeakMap what it contains, which is why it has neither size nor iterators nor
 * clear.
 */
@ScriptClass("WeakMap")
public final class NativeWeakMap extends ScriptObject {

    private final Map<Object, Object> map = new WeakHashMap<>();

    // initialized by saigen
    private static PropertyMap $saigenmap$;

    private NativeWeakMap(final ScriptObject proto, final PropertyMap propertyMap) {
        super(proto, propertyMap);
    }

    /**
     * ECMA 23.3.1.1 WeakMap ( [ iterable ] )
     *
     * @param newObj is this invoked with new
     * @param self   self reference
     * @param arg    an iterable of [key, value] pairs, or nothing
     * @return the new map
     */
    @Constructor(arity = 0)
    public static Object construct(final boolean newObj, final Object self, final Object arg) {
        if (!newObj) {
            throw typeError("constructor.requires.new", "WeakMap");
        }
        final Global global = Global.instance();
        final NativeWeakMap instance = new NativeWeakMap(global.getWeakMapPrototype(), $saigenmap$);
        CollectionSupport.addEntries(instance, arg, "set", true);
        return instance;
    }

    @Override
    public String getClassName() {
        return "WeakMap";
    }

    /**
     * ECMA 23.3.3.5 WeakMap.prototype.set ( key, value )
     *
     * @param self  self reference
     * @param key   the key, which has to be an object
     * @param value the value
     * @return self, so that calls chain
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE)
    public static Object set(final Object self, final Object key, final Object value) {
        final NativeWeakMap instance = checkWeakMap(self);
        if (JSType.isPrimitive(key)) {
            throw typeError("invalid.weak.key", ScriptRuntime.safeToString(key));
        }
        instance.map.put(key, value);
        return self;
    }

    /**
     * ECMA 23.3.3.3 WeakMap.prototype.get ( key )
     *
     * <p>A primitive can never have been stored, so looking one up is not an error,
     * it simply misses.
     *
     * @param self self reference
     * @param key  the key
     * @return the value stored under the key, or undefined
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object get(final Object self, final Object key) {
        final NativeWeakMap instance = checkWeakMap(self);
        if (JSType.isPrimitive(key)) {
            return ScriptRuntime.UNDEFINED;
        }
        // A stored null is a JavaScript null, so absence has to be asked for
        // separately rather than read off a null return.
        if (!instance.map.containsKey(key)) {
            return ScriptRuntime.UNDEFINED;
        }
        return instance.map.get(key);
    }

    /**
     * ECMA 23.3.3.4 WeakMap.prototype.has ( key )
     *
     * @param self self reference
     * @param key  the key
     * @return true if the key is present
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object has(final Object self, final Object key) {
        final NativeWeakMap instance = checkWeakMap(self);
        return !JSType.isPrimitive(key) && instance.map.containsKey(key);
    }

    /**
     * ECMA 23.3.3.2 WeakMap.prototype.delete ( key )
     *
     * @param self self reference
     * @param key  the key
     * @return true if there was an entry to remove
     */
    @Function(name = "delete", arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object remove(final Object self, final Object key) {
        final NativeWeakMap instance = checkWeakMap(self);
        if (JSType.isPrimitive(key) || !instance.map.containsKey(key)) {
            return false;
        }
        instance.map.remove(key);
        return true;
    }

    private static NativeWeakMap checkWeakMap(final Object self) {
        if (self instanceof NativeWeakMap) {
            return (NativeWeakMap) self;
        }
        throw typeError("not.a.weak.map", ScriptRuntime.safeToString(self));
    }
}
