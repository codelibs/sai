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
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import org.codelibs.sai.internal.objects.annotations.Attribute;
import org.codelibs.sai.internal.objects.annotations.Constructor;
import org.codelibs.sai.internal.objects.annotations.Function;
import org.codelibs.sai.internal.objects.annotations.Getter;
import org.codelibs.sai.internal.objects.annotations.ScriptClass;
import org.codelibs.sai.internal.objects.annotations.Where;
import org.codelibs.sai.internal.runtime.JSSymbol;
import org.codelibs.sai.internal.runtime.JSType;
import org.codelibs.sai.internal.runtime.PropertyMap;
import org.codelibs.sai.internal.runtime.ScriptObject;
import org.codelibs.sai.internal.runtime.ScriptRuntime;
import org.codelibs.sai.internal.runtime.linker.PrimitiveLookup;

import org.codelibs.sai.internal.dynalink.linker.GuardedInvocation;
import org.codelibs.sai.internal.dynalink.linker.LinkRequest;
import org.codelibs.sai.internal.lookup.MethodHandleFactory;
import org.codelibs.sai.internal.lookup.MethodHandleFunctionality;

/**
 * ECMAScript 2015 19.4, the Symbol built-in.
 *
 * <p>A symbol value is a {@link JSSymbol}, not a script object. This class is both
 * the Symbol function and the object a symbol is wrapped in when one is asked for
 * a property, the way NativeString stands behind a string.
 */
@ScriptClass("Symbol")
public final class NativeSymbol extends ScriptObject {

    /** ECMA 19.4.2.5, the symbol behind the iteration protocol. */
    public static final JSSymbol iterator = new JSSymbol("Symbol.iterator");

    /** ECMA 19.4.2.3, the symbol naming a constructor's derived constructor. */
    public static final JSSymbol species = new JSSymbol("Symbol.species");

    /** ECMA 19.4.2.10, the symbol naming the tag Object.prototype.toString reads. */
    public static final JSSymbol toStringTag = new JSSymbol("Symbol.toStringTag");

    /** ECMA 19.4.2.9, the symbol naming the conversion to a primitive. */
    public static final JSSymbol toPrimitive = new JSSymbol("Symbol.toPrimitive");

    /** ECMA 19.4.2.4, the symbol naming instanceof. */
    public static final JSSymbol hasInstance = new JSSymbol("Symbol.hasInstance");

    /** ECMA 19.4.2.2, the symbol naming whether concat spreads a value. */
    public static final JSSymbol isConcatSpreadable = new JSSymbol("Symbol.isConcatSpreadable");

    /** ECMA 19.4.2.11, the symbol naming the names a with statement does not see. */
    public static final JSSymbol unscopables = new JSSymbol("Symbol.unscopables");

    /** ECMA 19.4.2.6, the symbol naming a value's own String.prototype.match. */
    public static final JSSymbol match = new JSSymbol("Symbol.match");

    /** ECMA 19.4.2.7, the symbol naming a value's own String.prototype.replace. */
    public static final JSSymbol replace = new JSSymbol("Symbol.replace");

    /** ECMA 19.4.2.8, the symbol naming a value's own String.prototype.search. */
    public static final JSSymbol search = new JSSymbol("Symbol.search");

    /** ECMA 19.4.2.12, the symbol naming a value's own String.prototype.split. */
    public static final JSSymbol split = new JSSymbol("Symbol.split");

    /**
     * The well known symbols, in the order 19.4.2 lists them, paired with the name
     * each is reachable under on the Symbol object.
     *
     * <p>They are installed from here rather than declared as annotated properties
     * because saigen writes such a constant into the generated class as a constant
     * pool entry, and a symbol is not one.
     */
    private static final Object[][] WELL_KNOWN = {
        { "hasInstance", hasInstance }, { "isConcatSpreadable", isConcatSpreadable },
        { "iterator", iterator }, { "match", match }, { "replace", replace },
        { "search", search }, { "species", species }, { "split", split },
        { "toPrimitive", toPrimitive }, { "toStringTag", toStringTag },
        { "unscopables", unscopables },
    };

    /**
     * Adds the well known symbols to the Symbol object. Each is a constant: not
     * writable, not enumerable and not configurable, as 19.4.2 requires.
     *
     * @param symbolConstructor the Symbol object to add them to
     */
    static void installWellKnownSymbols(final ScriptObject symbolConstructor) {
        for (final Object[] entry : WELL_KNOWN) {
            symbolConstructor.addOwnProperty((String) entry[0], Attribute.NON_ENUMERABLE_CONSTANT, entry[1]);
        }
    }

    /** ECMA 19.4.2.1 GlobalSymbolRegistry, shared by every global in the process. */
    private static final Map<String, JSSymbol> REGISTRY = new HashMap<>();

    private static final MethodHandleFunctionality MH = MethodHandleFactory.getFunctionality();
    private static final MethodHandle WRAPFILTER = findOwnMH("wrapFilter", MH.type(NativeSymbol.class, Object.class));
    private static final MethodHandle PROTOFILTER = findOwnMH("protoFilter", MH.type(Object.class, Object.class));

    /** The handle Global installs under Symbol.toPrimitive on Symbol.prototype. */
    static final MethodHandle SYMBOL_TO_PRIMITIVE = findOwnMH("symbolToPrimitive", MH.type(Object.class, Object.class,
            Object.class));

    /**
     * ES6 19.4.3.4 Symbol.prototype [ @@toPrimitive ] ( hint ), which answers the symbol
     * a wrapper stands for whatever the hint is.
     *
     * <p>It is what keeps a wrapper usable where the symbol is: as a property key, and
     * on either side of ==. Without it the wrapper would reduce through toString, which
     * spells the symbol out as a string rather than answering it.
     *
     * @param self the wrapper, or the symbol itself
     * @param hint the hint, which a symbol has no use for
     * @return the symbol
     */
    public static Object symbolToPrimitive(final Object self, final Object hint) {
        return checkSymbol(self);
    }

    /**
     * The getter behind every {@code Symbol.species}: ES6 defines each of them as
     * {@code get [Symbol.species]() { return this; }}, so one handle serves them all.
     */
    static final MethodHandle SPECIES_GETTER = findOwnMH("species", MH.type(Object.class, Object.class));

    /**
     * ES6 24.1.3.3 and its like: a constructor's Symbol.species is itself, unless a
     * subclass says otherwise.
     *
     * @param self the constructor the accessor was read from
     * @return the constructor to build a derived object with
     */
    public static Object species(final Object self) {
        return self;
    }

    // initialized by saigen
    private static PropertyMap $saigenmap$;

    private final JSSymbol symbol;

    NativeSymbol(final JSSymbol symbol, final Global global) {
        this(symbol, global.getSymbolPrototype(), $saigenmap$);
    }

    private NativeSymbol(final JSSymbol symbol, final ScriptObject proto, final PropertyMap map) {
        super(proto, map);
        this.symbol = symbol;
    }

    /**
     * ECMA 19.4.1.1 Symbol ( [ description ] )
     *
     * <p>Symbol is callable but not constructable: there is no way to get a symbol
     * wrapper object other than by asking Object for one.
     *
     * @param newObj is this invoked with new
     * @param self   self reference
     * @param args   the description, or nothing
     * @return a fresh symbol
     */
    @Constructor(arity = 1)
    public static Object construct(final boolean newObj, final Object self, final Object... args) {
        if (newObj) {
            throw typeError("symbol.not.a.constructor");
        }
        final Object description = args.length > 0 ? args[0] : ScriptRuntime.UNDEFINED;
        return new JSSymbol(description == ScriptRuntime.UNDEFINED ? null : JSType.toString(description));
    }

    @Override
    public String getClassName() {
        return "Symbol";
    }

    /**
     * ECMA 19.4.2.1 Symbol.for ( key )
     *
     * <p>The registry hands back the same symbol for the same string, which is how
     * two pieces of code that never meet can agree on one.
     *
     * @param self self reference
     * @param key  the string to look the symbol up under
     * @return the registered symbol
     */
    @Function(name = "for", arity = 1, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object symbolFor(final Object self, final Object key) {
        final String name = JSType.toString(key);
        synchronized (REGISTRY) {
            return REGISTRY.computeIfAbsent(name, JSSymbol::new);
        }
    }

    /**
     * ECMA 19.4.2.5 Symbol.keyFor ( sym )
     *
     * @param self self reference
     * @param sym  a symbol
     * @return the string it is registered under, or undefined
     */
    @Function(arity = 1, attributes = Attribute.NOT_ENUMERABLE, where = Where.CONSTRUCTOR)
    public static Object keyFor(final Object self, final Object sym) {
        if (!(sym instanceof JSSymbol)) {
            throw typeError("not.a.symbol", ScriptRuntime.safeToString(sym));
        }
        synchronized (REGISTRY) {
            for (final Map.Entry<String, JSSymbol> entry : REGISTRY.entrySet()) {
                if (entry.getValue() == sym) {
                    return entry.getKey();
                }
            }
        }
        return ScriptRuntime.UNDEFINED;
    }

    /**
     * ECMA 19.4.3.2 Symbol.prototype.toString ( )
     *
     * @param self self reference
     * @return the symbol written out, as in Symbol(foo)
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object toString(final Object self) {
        return checkSymbol(self).toString();
    }

    /**
     * ECMA 19.4.3.3 Symbol.prototype.valueOf ( )
     *
     * @param self self reference
     * @return the symbol itself
     */
    @Function(arity = 0, attributes = Attribute.NOT_ENUMERABLE)
    public static Object valueOf(final Object self) {
        return checkSymbol(self);
    }

    /**
     * The description the symbol was made with.
     *
     * @param self self reference
     * @return the description, or undefined
     */
    // On the instance rather than the prototype, for the reason NativeMap.size is:
    // a built-in accessor found on a prototype is invoked with the prototype as its
    // self, which is not the symbol being asked about.
    @Getter(name = "description", attributes = Attribute.NOT_ENUMERABLE)
    public static Object description(final Object self) {
        final String description = checkSymbol(self).getDescription();
        return description == null ? ScriptRuntime.UNDEFINED : description;
    }

    /**
     * Returns the symbol this object wraps.
     *
     * @return the symbol
     */
    public JSSymbol getSymbol() {
        return symbol;
    }

    /**
     * Links a property read or a call on a symbol, by wrapping it the way a string
     * or a number is wrapped: the symbol itself carries no properties, so the lookup
     * runs against a NativeSymbol standing in front of Symbol.prototype.
     *
     * @param request  the link request
     * @param receiver the symbol the call site saw
     * @return the invocation to link
     */
    public static GuardedInvocation lookupPrimitive(final LinkRequest request, final Object receiver) {
        return PrimitiveLookup.lookupPrimitive(request, JSSymbol.class,
                new NativeSymbol((JSSymbol) receiver, Global.instance()), WRAPFILTER, PROTOFILTER);
    }

    @SuppressWarnings("unused")
    private static NativeSymbol wrapFilter(final Object receiver) {
        return new NativeSymbol((JSSymbol) receiver, Global.instance());
    }

    @SuppressWarnings("unused")
    private static Object protoFilter(final Object object) {
        return Global.instance().getSymbolPrototype();
    }

    private static MethodHandle findOwnMH(final String name, final MethodType type) {
        return MH.findStatic(MethodHandles.lookup(), NativeSymbol.class, name, type);
    }

    private static JSSymbol checkSymbol(final Object self) {
        if (self instanceof JSSymbol) {
            return (JSSymbol) self;
        }
        if (self instanceof NativeSymbol) {
            return ((NativeSymbol) self).symbol;
        }
        throw typeError("not.a.symbol", ScriptRuntime.safeToString(self));
    }
}
