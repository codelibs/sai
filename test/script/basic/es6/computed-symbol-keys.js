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
 * ES2015 12.2.6.7: a computed property key is whatever 7.1.14 ToPropertyKey makes
 * of the expression, and that is a symbol when the expression is one. The three
 * runtime operations an object literal with a computed key compiles to were
 * converting the key with ToString, so a symbol key raised a TypeError and only
 * the accessor form -- which takes a different path -- worked.
 *
 * 12.14.4 SetFunctionName goes with it: a function with no name of its own that is
 * stored under a computed key is named after the key, which only the runtime can
 * do. A symbol names it by its description in brackets, and a symbol with no
 * description leaves the name empty.
 *
 * A computed key without the Symbol built-in can only be a string, so this is an
 * addition and the test deliberately runs without --language=es6, except that the
 * syntax itself needs it.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

var key = Symbol("key");

// 12.2.6.7: each of the four forms a computed key can take.
var data = { [key]: 1 };
show("a data property", data[key]);

var method = { [key]() { return 2; } };
show("a shorthand method", method[key]());

var accessor = { get [key]() { return 3; } };
show("an accessor", accessor[key]);

var written = { [key]: 4, other: 5 };
show("a literal with both kinds of key", written[key] + "," + written.other);

// The property is an own, enumerable, writable, configurable one, the way any
// property of an object literal is.
var descriptor = Object.getOwnPropertyDescriptor(data, key);
show("the descriptor", [descriptor.value, descriptor.writable, descriptor.enumerable, descriptor.configurable].join(","));

// An object literal defines its properties rather than assigning them, which a
// computed __proto__ has to keep doing.
var protoKey = "__proto__";
var array = [];
var notProto = { [protoKey]: array };
show("a computed __proto__ defines", (Object.getPrototypeOf(notProto) === Object.prototype) + "," + (notProto.__proto__ === array));

// 12.14.4: the name of a function stored under a computed key.
var described = Symbol("described");
var bare = Symbol();
var named = { [described]: function () { }, [bare]: function () { }, ["a string"]: function () { } };
show("a symbol with a description", JSON.stringify(named[described].name));
show("a symbol without one", JSON.stringify(named[bare].name));
show("a string key", JSON.stringify(named["a string"].name));
show("a shorthand method", JSON.stringify(({ [described]() { } })[described].name));

// A function that has a name of its own keeps it.
show("a named function expression", ({ [described]: function keeps() { } })[described].name);

// So does one that was named where it was made.
var already = function () { };
show("a function named elsewhere", ({ [described]: already })[described].name);

// The name is not writable and not enumerable, as 19.2.4.2 asks.
var nameDescriptor = Object.getOwnPropertyDescriptor(named[described], "name");
show("the name descriptor",
     [nameDescriptor.value, nameDescriptor.writable, nameDescriptor.enumerable, nameDescriptor.configurable].join(","));

// The literal is built in source order, whatever the keys are.
var order = [];
function record(value) { order.push(value); return value; }
var sequenced = { a: record(1), [record("b")]: record(2), c: record(3) };
show("source order", order.join(",") + " " + Object.keys(sequenced).join(","));

// A symbol key is not a name, so it shows up only among the symbols.
var mixed = { [key]: 1, plain: 2 };
show("names", Object.keys(mixed).join(","));
show("symbols", Object.getOwnPropertySymbols(mixed).length + "," + (Object.getOwnPropertySymbols(mixed)[0] === key));

// A class body reads computed keys through the same operations.
class Holder {
    [key]() { return 6; }
    get [described]() { return 7; }
    static [bare]() { return 8; }
}
show("a class method", new Holder()[key]());
show("a class accessor", new Holder()[described]);
show("a static class method", Holder[bare]());
