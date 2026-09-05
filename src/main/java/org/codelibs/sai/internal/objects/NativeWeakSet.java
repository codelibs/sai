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
 * ECMAScript 2015 23.4, the WeakSet built-in.
 *
 * <p>A WeakSet is a WeakMap whose values carry nothing: it can say whether it
 * holds an object, and holding it does not keep it alive.
 */
@ScriptClass("WeakSet")
public final class NativeWeakSet extends ScriptObject {

    private final Map<Object, Boolean> map = new WeakHashMap<>();

    // initialized by saigen
    private static PropertyMap $saigenmap$;

    private NativeWeakSet(final ScriptObject proto, final PropertyMap propertyMap) {
        super(proto, propertyMap);
    }

    /**
     * ECMA 23.4.1.1 WeakSet ( [ iterable ] )
     *
     * @param newObj is this invoked with new
     * @param self   self reference
     * @param arg    an iterable of objects, or nothing
     * @return the new set
     */
    @Constructor(arity = 0)
    public static Object construct(final boolean newObj, final Object self, final Object arg) {
        if (!newObj) {
            throw typeError("constructor.requires.new", "WeakSet");
        }
        final Global global = Global.instance();
        final NativeWeakSet instance = new NativeWeakSet(global.getWeakSetPrototype(), $saigenmap$);
        CollectionSupport.addEntries(instance, arg, "add", false);
        return instance;
    }

    @Override
    public String getClassName() {
        return "WeakSet";
    }

    /**
     * ECMA 23.4.3.1 WeakSet.prototype.add ( value )
     *
     * @param self  self reference
     * @param value the value, which has to be an object
     * @return self, so that calls chain
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object add(final Object self, final Object value) {
        final NativeWeakSet instance = checkWeakSet(self);
        if (JSType.isPrimitive(value)) {
            throw typeError("invalid.weak.key", ScriptRuntime.safeToString(value));
        }
        instance.map.put(value, Boolean.TRUE);
        return self;
    }

    /**
     * ECMA 23.4.3.4 WeakSet.prototype.has ( value )
     *
     * @param self  self reference
     * @param value the value
     * @return true if the value is present
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object has(final Object self, final Object value) {
        final NativeWeakSet instance = checkWeakSet(self);
        return !JSType.isPrimitive(value) && instance.map.containsKey(value);
    }

    /**
     * ECMA 23.4.3.3 WeakSet.prototype.delete ( value )
     *
     * @param self  self reference
     * @param value the value
     * @return true if there was a value to remove
     */
    @Function(name = "delete", arity = 1, attributes = Attribute.NOT_ENUMERABLE)
    public static Object remove(final Object self, final Object value) {
        final NativeWeakSet instance = checkWeakSet(self);
        return !JSType.isPrimitive(value) && instance.map.remove(value) != null;
    }

    private static NativeWeakSet checkWeakSet(final Object self) {
        if (self instanceof NativeWeakSet) {
            return (NativeWeakSet) self;
        }
        throw typeError("not.a.weak.set", ScriptRuntime.safeToString(self));
    }
}
