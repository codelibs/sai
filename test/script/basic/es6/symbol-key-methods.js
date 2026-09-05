/*
 * Copyright (c) 2010, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
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
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * ES2015 7.1.14 ToPropertyKey in the methods that take a key by hand:
 * Object.prototype.hasOwnProperty and propertyIsEnumerable, and every Reflect
 * operation that names a property. Each of them converted its argument with
 * ToString, which throws on a symbol, so a symbol keyed property was reachable
 * through o[sym] and Object.defineProperty but through nothing else.
 *
 * 9.1.12 also has Reflect.ownKeys answer the symbols after the names.
 *
 * A symbol cannot reach any of these without the Symbol built-in, which does not
 * exist before it, so this is an addition rather than a change and the test
 * deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

var key = Symbol("key");
var object = {};
object[key] = 1;

// 19.1.3.2 and 19.1.3.4: both read the key the same way the property was written.
show("hasOwnProperty", object.hasOwnProperty(key));
show("hasOwnProperty misses another symbol", object.hasOwnProperty(Symbol("key")));
show("propertyIsEnumerable", object.propertyIsEnumerable(key));

var hidden = Symbol("hidden");
Object.defineProperty(object, hidden, { value: 2, enumerable: false });
show("a non enumerable one is still own", object.hasOwnProperty(hidden));
show("but is not enumerable", object.propertyIsEnumerable(hidden));

// An inherited symbol is not an own one.
var derived = Object.create(object);
show("inherited is not own", derived.hasOwnProperty(key));
show("inherited is not enumerable here", derived.propertyIsEnumerable(key));

// Names still work, and so does the string a number spells.
show("a name", ({ a: 1 }).hasOwnProperty("a"));
show("a number", ({ 1: true }).hasOwnProperty(1));
show("an array index", [1].hasOwnProperty(0));

// 26.1: every Reflect operation that names a property.
var target = {};
show("Reflect.defineProperty", Reflect.defineProperty(target, key, { value: 3, writable: true, configurable: true }));
show("Reflect.get", Reflect.get(target, key));
show("Reflect.has", Reflect.has(target, key));
show("Reflect.getOwnPropertyDescriptor", Reflect.getOwnPropertyDescriptor(target, key).value);
show("Reflect.set", Reflect.set(target, key, 4) + "," + target[key]);
show("Reflect.deleteProperty", Reflect.deleteProperty(target, key) + "," + (key in target));

// 9.1.9 through a receiver, the path that looks the key up along the chain.
var accessed = null;
var base = {};
Object.defineProperty(base, key, { get: function () { accessed = this; return 5; } });
var receiver = Object.create(base);
show("Reflect.get through a receiver", Reflect.get(base, key, receiver) + "," + (accessed === receiver));

// 26.1.11: names first, then symbols, each in the order they were added.
var first = Symbol("first");
var second = Symbol("second");
var third = Symbol("third");
var listed = { 1: true, A: true };
listed.B = true;
listed[first] = true;
listed[2] = true;
listed[second] = true;
Object.defineProperty(listed, "C", { value: true, enumerable: true });
Object.defineProperty(listed, third, { value: true, enumerable: true });

var keys = Reflect.ownKeys(listed);
show("ownKeys counts both", keys.length);
show("ownKeys lists the names first", keys.slice(0, keys.length - 3).join(","));
show("ownKeys lists the symbols in order",
     [keys[keys.length - 3] === first, keys[keys.length - 2] === second, keys[keys.length - 1] === third].join(","));
show("ownKeys of an object with no symbols", Reflect.ownKeys({ a: 1, b: 2 }).join(","));
show("ownKeys skips inherited symbols", Reflect.ownKeys(Object.create(listed)).length);
