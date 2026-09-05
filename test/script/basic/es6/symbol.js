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
 * ES2015 19.4, the Symbol built-in.
 *
 * A symbol is a primitive value that can be used as a property key, and the only
 * kind of key that is not a name. Symbol is a new global name rather than a
 * change to an existing operation, so it is installed unconditionally and this
 * test deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

// 19.4.1.1: Symbol is callable but not constructable, and every call makes a new
// symbol, description or no description.
show("typeof", typeof Symbol());
try {
    new Symbol();
    print("FAILED: new Symbol() was allowed");
} catch (e) {
    show("new Symbol throws", e instanceof TypeError);
}
show("two calls, two symbols", Symbol("a") === Symbol("a"));
show("a symbol is itself", (function (s) { return s === s; }(Symbol())));

// 19.4.3: what a symbol answers to. The value itself carries nothing, so the
// lookup runs against Symbol.prototype.
var described = Symbol("desc");
show("toString", described.toString());
show("description", described.description);
show("no description", Symbol().description);
show("valueOf", described.valueOf() === described);
show("class name", Object.prototype.toString.call(described));

var inherited = Symbol();
show("nothing of its own", inherited.foo);
Symbol.prototype.foo = 2;
show("reaches Symbol.prototype", inherited.foo);
delete Symbol.prototype.foo;

// 7.1.12 and 7.1.3: a symbol has neither a string nor a numeric form, so it
// cannot be concatenated or added to.
try {
    described + "";
    print("FAILED: a symbol was concatenated with a string");
} catch (e) {
    show("no string coercion", e instanceof TypeError);
}
try {
    described + 0;
    print("FAILED: a symbol was added to a number");
} catch (e) {
    show("no number coercion", e instanceof TypeError);
}

// 21.1.1.1: String() is the one place a symbol may be written out.
show("String()", String(Symbol("foo")));

// 19.4.2.1 and 19.4.2.5: the registry, which lets two pieces of code that never
// meet agree on one symbol.
show("Symbol.for", Symbol.for("shared") === Symbol.for("shared"));
show("Symbol.keyFor", Symbol.keyFor(Symbol.for("shared")));
show("keyFor an unregistered symbol", Symbol.keyFor(Symbol("private")));
show("for is not the same as calling", Symbol.for("shared") === Symbol("shared"));

// 19.4.2: the well known symbols exist and are symbols.
show("well known", ["hasInstance", "isConcatSpreadable", "iterator", "match", "replace",
        "search", "species", "split", "toPrimitive", "toStringTag", "unscopables"]
        .map(function (name) { return typeof Symbol[name]; }).join(","));
show("well known are constants", (function () {
    var d = Object.getOwnPropertyDescriptor(Symbol, "iterator");
    return [d.writable, d.enumerable, d.configurable].join(",");
}()));

// A symbol as a property key. Two symbols with the same description are two keys.
var key = Symbol("k");
var other = Symbol("k");
var value = {};
var holder = {};
holder[key] = value;
holder[other] = "other";
show("read back", holder[key] === value);
show("same description, different key", holder[other]);
show("in", key in holder);
show("delete", delete holder[other]);
show("gone", holder[other]);

// 9.1.12: a symbol key is not one of an object's names, so nothing that lists
// names lists it.
var mixed = { name: 1 };
mixed[key] = 2;
var seen = [];
for (var each in mixed) {
    seen.push(each);
}
show("for..in", seen.join(","));
show("Object.keys", Object.keys(mixed).join(","));
show("getOwnPropertyNames", Object.getOwnPropertyNames(mixed).join(","));
show("JSON.stringify", JSON.stringify(mixed));
show("Object.assign", (function () {
    var copy = Object.assign({}, mixed);
    return copy.name + "," + (copy[key] === undefined);
}()));

// A symbol as a value is dropped by JSON, as an unrepresentable value is.
show("a symbol value in JSON", JSON.stringify({ a: Symbol() }));

// defineProperty and getOwnPropertyDescriptor take one too.
var defined = {};
Object.defineProperty(defined, key, { value: 7, enumerable: true });
show("defineProperty", defined[key]);
show("getOwnPropertyDescriptor", Object.getOwnPropertyDescriptor(defined, key).value);

// A symbol keyed property is inherited like any other.
var parent = {};
parent[key] = "from the prototype";
show("inherited", Object.create(parent)[key]);

// 19.4.3.4: Object() wraps a symbol the way it wraps any primitive.
var wrapped = Object(described);
show("Object(symbol)", [typeof wrapped, wrapped instanceof Symbol,
        wrapped.valueOf() === described].join(","));
