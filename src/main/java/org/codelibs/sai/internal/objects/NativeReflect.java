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

import org.codelibs.sai.internal.objects.annotations.Attribute;
import org.codelibs.sai.internal.objects.annotations.Function;
import org.codelibs.sai.internal.objects.annotations.ScriptClass;
import org.codelibs.sai.internal.objects.annotations.Where;
import org.codelibs.sai.internal.runtime.ECMAException;
import org.codelibs.sai.internal.runtime.JSType;
import org.codelibs.sai.internal.runtime.PropertyDescriptor;
import org.codelibs.sai.internal.runtime.PropertyMap;
import org.codelibs.sai.internal.runtime.ScriptFunction;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;
import org.codelibs.sai.internal.runtime.linker.SaiCallSiteDescriptor;

/**
 * ECMAScript 2015 26.1, the Reflect object.
 *
 * <p>Reflect exposes the operations Object already offers as statics, but in the
 * form the specification defines them in: each one answers whether it succeeded
 * instead of throwing or handing the target back, so a caller can act on a
 * refusal without a try block.
 */
@ScriptClass("Reflect")
public final class NativeReflect extends ScriptObject {

    // initialized by saigen
    @SuppressWarnings("unused")
    private static PropertyMap $saigenmap$;

    private NativeReflect() {
        // don't create me
        throw new UnsupportedOperationException();
    }

    /**
     * ECMA 26.1.6 Reflect.get ( target, propertyKey [ , receiver ] )
     *
     * @param self     self reference
     * @param target   the object to read from
     * @param key      the property key
     * @param receiver the this an accessor runs against, the target by default
     * @return the value of the property
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object get(final Object self, final Object target, final Object key, final Object receiver) {
        final ScriptObject sobj = checkObject(target);
        final String name = JSType.toString(key);

        if (receiver == ScriptRuntime.UNDEFINED || receiver == target) {
            return sobj.get(name);
        }

        // 9.1.8 OrdinaryGet: the property is looked up along the target's chain, but
        // a getter found there is called with the receiver as its this.
        final ScriptObject desc = findDescriptor(sobj, name);
        if (desc == null) {
            return ScriptRuntime.UNDEFINED;
        }
        final Object getter = desc.get(PropertyDescriptor.GET);
        if (getter instanceof ScriptFunction) {
            return ScriptRuntime.apply((ScriptFunction) getter, receiver);
        }
        return desc.has(PropertyDescriptor.GET) ? ScriptRuntime.UNDEFINED : desc.get(PropertyDescriptor.VALUE);
    }

    /**
     * ECMA 26.1.13 Reflect.set ( target, propertyKey, V [ , receiver ] )
     *
     * @param self     self reference
     * @param target   the object to write to
     * @param key      the property key
     * @param value    the value
     * @param receiver the this a setter runs against, the target by default
     * @return true if the write was allowed
     */
    @Function(arity = 3, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object set(final Object self, final Object target, final Object key, final Object value,
            final Object receiver) {
        final ScriptObject sobj = checkObject(target);
        final String name = JSType.toString(key);

        if (receiver != ScriptRuntime.UNDEFINED && receiver != target) {
            // 9.1.9 OrdinarySet: a setter found on the target's chain is called with
            // the receiver as its this; anything else is written on the receiver.
            final ScriptObject desc = findDescriptor(sobj, name);
            if (desc != null && desc.has(PropertyDescriptor.SET)) {
                final Object setter = desc.get(PropertyDescriptor.SET);
                if (!(setter instanceof ScriptFunction)) {
                    return false;
                }
                ScriptRuntime.apply((ScriptFunction) setter, receiver, value);
                return true;
            }
            return set(self, receiver, key, value, ScriptRuntime.UNDEFINED);
        }

        // 9.1.9 again: an inherited data property that is not writable refuses the
        // write. ScriptObject.set stops looking once it sees the property is
        // inherited, so the check has to be made here.
        final ScriptObject inherited = findDescriptor(sobj, name);
        if (inherited != null && !inherited.has(PropertyDescriptor.SET)
                && !JSType.toBoolean(inherited.get(PropertyDescriptor.WRITABLE))) {
            return false;
        }

        // Reflect reports a refusal rather than throwing it, so the write is made
        // in the strict mode that raises one and the error is turned into a false.
        try {
            sobj.set(name, value, SaiCallSiteDescriptor.CALLSITE_STRICT);
        } catch (final ECMAException e) {
            return false;
        }
        return true;
    }

    /**
     * ECMA 26.1.9 Reflect.has ( target, propertyKey )
     *
     * @param self   self reference
     * @param target the object to ask
     * @param key    the property key
     * @return true if the target or its prototypes carry the key
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object has(final Object self, final Object target, final Object key) {
        return checkObject(target).has(JSType.toString(key));
    }

    /**
     * ECMA 26.1.4 Reflect.deleteProperty ( target, propertyKey )
     *
     * @param self   self reference
     * @param target the object to delete from
     * @param key    the property key
     * @return true if the property is gone afterwards
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object deleteProperty(final Object self, final Object target, final Object key) {
        return checkObject(target).delete(JSType.toString(key), false);
    }

    /**
     * ECMA 26.1.7 Reflect.getOwnPropertyDescriptor ( target, propertyKey )
     *
     * @param self   self reference
     * @param target the object to describe
     * @param key    the property key
     * @return the descriptor, or undefined
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object getOwnPropertyDescriptor(final Object self, final Object target, final Object key) {
        final Object desc = checkObject(target).getOwnPropertyDescriptor(JSType.toString(key));
        return desc == null ? ScriptRuntime.UNDEFINED : desc;
    }

    /**
     * ECMA 26.1.3 Reflect.defineProperty ( target, propertyKey, attributes )
     *
     * @param self       self reference
     * @param target     the object to define on
     * @param key        the property key
     * @param attributes the descriptor
     * @return true if the definition was allowed
     */
    @Function(arity = 3, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object defineProperty(final Object self, final Object target, final Object key,
            final Object attributes) {
        // reject = false, so a refusal comes back as false rather than as a TypeError.
        return checkObject(target).defineOwnProperty(JSType.toString(key), attributes, false);
    }

    /**
     * ECMA 26.1.8 Reflect.getPrototypeOf ( target )
     *
     * @param self   self reference
     * @param target the object to ask
     * @return the prototype, or null
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object getPrototypeOf(final Object self, final Object target) {
        return checkObject(target).getProto();
    }

    /**
     * ECMA 26.1.14 Reflect.setPrototypeOf ( target, proto )
     *
     * @param self   self reference
     * @param target the object to change
     * @param proto  the new prototype, an object or null
     * @return true if the change was allowed
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object setPrototypeOf(final Object self, final Object target, final Object proto) {
        final ScriptObject sobj = checkObject(target);
        try {
            sobj.setPrototypeOf(proto);
        } catch (final ECMAException e) {
            return false;
        }
        return true;
    }

    /**
     * ECMA 26.1.10 Reflect.isExtensible ( target )
     *
     * @param self   self reference
     * @param target the object to ask
     * @return true if properties can still be added
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object isExtensible(final Object self, final Object target) {
        return checkObject(target).isExtensible();
    }

    /**
     * ECMA 26.1.12 Reflect.preventExtensions ( target )
     *
     * @param self   self reference
     * @param target the object to seal off
     * @return true if the target is inextensible afterwards
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object preventExtensions(final Object self, final Object target) {
        final ScriptObject sobj = checkObject(target);
        try {
            sobj.preventExtensions();
        } catch (final ECMAException e) {
            return false;
        }
        return !sobj.isExtensible();
    }

    /**
     * ECMA 26.1.11 Reflect.ownKeys ( target )
     *
     * <p>Every own key, enumerable or not, which is what Object.getOwnPropertyNames
     * already answers.
     *
     * @param self   self reference
     * @param target the object to list
     * @return an array of the own keys
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object ownKeys(final Object self, final Object target) {
        return new NativeArray(checkObject(target).getOwnKeys(true));
    }

    /**
     * ECMA 26.1.1 Reflect.apply ( target, thisArgument, argumentsList )
     *
     * @param self      self reference
     * @param target    the function to call
     * @param thisArg   the this of the call
     * @param arguments an array-like of arguments
     * @return whatever the function returns
     */
    @Function(arity = 3, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object apply(final Object self, final Object target, final Object thisArg, final Object arguments) {
        return ScriptRuntime.apply(checkCallable(target), thisArg, toArgumentList(arguments));
    }

    /**
     * ECMA 26.1.2 Reflect.construct ( target [ , argumentsList [ , newTarget ] ] )
     *
     * <p>The third argument names the constructor whose prototype the new object
     * gets, which is how a subclass instance is built from a parent's constructor.
     *
     * @param self      self reference
     * @param target    the constructor to run
     * @param arguments an array-like of arguments
     * @param newTarget the constructor the result should be an instance of
     * @return the constructed object
     */
    @Function(arity = 2, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object construct(final Object self, final Object target, final Object arguments,
            final Object newTarget) {
        final ScriptFunction ctor = checkCallable(target);
        final Object result = ScriptRuntime.construct(ctor, toArgumentList(arguments));

        if (newTarget != ScriptRuntime.UNDEFINED && newTarget != target && result instanceof ScriptObject) {
            final ScriptFunction other = checkCallable(newTarget);
            final Object proto = other.get("prototype");
            if (proto instanceof ScriptObject) {
                ((ScriptObject) result).setPrototypeOf(proto);
            }
        }
        return result;
    }

    /**
     * Walks the prototype chain for the first own descriptor of the key, which is
     * where OrdinaryGet and OrdinarySet look before they decide what to do with the
     * receiver.
     */
    private static ScriptObject findDescriptor(final ScriptObject start, final String name) {
        for (ScriptObject holder = start; holder != null; holder = holder.getProto()) {
            final Object desc = holder.getOwnPropertyDescriptor(name);
            if (desc instanceof ScriptObject) {
                return (ScriptObject) desc;
            }
        }
        return null;
    }

    private static Object[] toArgumentList(final Object arguments) {
        if (arguments == ScriptRuntime.UNDEFINED || arguments == null) {
            return ScriptRuntime.EMPTY_ARRAY;
        }
        if (!(arguments instanceof ScriptObject)) {
            throw typeError("not.an.object", ScriptRuntime.safeToString(arguments));
        }
        final ScriptObject list = (ScriptObject) arguments;
        final int length = (int) JSType.toUint32(list.getLength());
        final Object[] args = new Object[length];
        for (int i = 0; i < length; i++) {
            args[i] = list.get(i);
        }
        return args;
    }

    private static ScriptFunction checkCallable(final Object target) {
        if (target instanceof ScriptFunction) {
            return (ScriptFunction) target;
        }
        throw typeError("not.a.function", ScriptRuntime.safeToString(target));
    }

    private static ScriptObject checkObject(final Object target) {
        if (target instanceof ScriptObject) {
            return (ScriptObject) target;
        }
        throw typeError("not.an.object", ScriptRuntime.safeToString(target));
    }
}
