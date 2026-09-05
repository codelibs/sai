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
 * ES2015 well-known symbols that let an object take over an operation the
 * language performs on it: Symbol.hasInstance for instanceof, Symbol.unscopables
 * for the names a with statement passes over, and Symbol.toPrimitive when nothing
 * says which primitive is wanted. Two objects that carry one of them come with
 * it: Array.prototype names the methods added since with was written, and
 * Date.prototype is why a date reads as its string.
 *
 * An object without any of these behaves exactly as before, so this is an
 * addition and the test deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

// 12.9.4: instanceof asks the right hand side first, and asks it even when it is
// not a function.
var asked = null;
var Marker = function () { };
Object.defineProperty(Marker, Symbol.hasInstance, { value: function (value) { asked = value; return value === 1; } });
show("instanceof through hasInstance", (1 instanceof Marker) + "," + (2 instanceof Marker));
show("it is handed the left hand side", asked);

var notAFunction = {};
notAFunction[Symbol.hasInstance] = function () { return true; };
show("an object that is not a function", ({}) instanceof notAFunction);

var receiver = null;
var records = function () { };
Object.defineProperty(records, Symbol.hasInstance, { value: function () { receiver = this; return false; } });
1 instanceof records;
show("called on the right hand side", receiver === records);

// Ordinary instanceof is untouched.
function Ordinary() { }
show("an ordinary constructor", (new Ordinary()) instanceof Ordinary);
show("an ordinary miss", ({}) instanceof Ordinary);
try {
    ({}) instanceof {};
    show("a plain object is still refused", "no error");
} catch (e) {
    show("a plain object is still refused", e instanceof TypeError);
}

// 8.1.1.2.1: a with statement passes over the names Symbol.unscopables lists, so
// the binding outside is the one that is read.
var bar = "outer";
var scoped = { foo: 1, bar: 2 };
scoped[Symbol.unscopables] = { bar: true };
with (scoped) {
    show("an unlisted name is read from the object", foo);
    show("a listed one is read from outside", bar);
}

// Only a truthy entry hides a name, and only an object counts as the list.
var partly = { a: 1, b: 2, c: 3 };
partly[Symbol.unscopables] = { a: false, b: 0 };
var a = "outer a";
var b = "outer b";
with (partly) {
    show("a false entry hides nothing", a + "," + b + "," + c);
}

var notAnObject = { d: 4 };
notAnObject[Symbol.unscopables] = "not an object";
with (notAnObject) {
    show("a non object list is ignored", d);
}

// Writing through a hidden name writes outside too.
var e = "outer e";
var written = { e: "inner e" };
written[Symbol.unscopables] = { e: true };
with (written) {
    e = "assigned";
}
show("a write goes outside", e + "," + written.e);

// 22.1.3.32: Array.prototype lists the methods added after with was written.
var unscopables = Array.prototype[Symbol.unscopables];
show("Array.prototype lists", Object.getOwnPropertyNames(unscopables).sort().join(","));
show("the list has no prototype", Object.getPrototypeOf(unscopables));
var values = "outer values";
with ([]) {
    show("values is read from outside an array", values);
    show("but join is not", typeof join);
}

// 7.2.12: == reduces an object with no hint at all, which a Symbol.toPrimitive is
// told about as the "default" hint. The other two hints are unchanged.
var hints = [];
function hinted() {
    var object = {};
    object[Symbol.toPrimitive] = function (hint) { hints.push(hint); return 0; };
    return object;
}
hinted() >= 0;
hinted() in {};
hinted() == 0;
"" + hinted();
+hinted();
show("hints", hints.join(","));

// 20.3.4.45: Date.prototype's picks the order valueOf and toString are tried in.
var toPrimitive = Date.prototype[Symbol.toPrimitive];
show("Date.prototype[Symbol.toPrimitive]", typeof toPrimitive);
show("number", toPrimitive.call(Object(2), "number"));
show("string", toPrimitive.call(Object(2), "string"));
show("default", toPrimitive.call(Object(2), "default"));
try {
    toPrimitive.call(Object(2), "wrong");
    show("an unknown hint", "no error");
} catch (e) {
    show("an unknown hint", e instanceof TypeError);
}
var date = new Date(0);
show("a date still reads as its string", ("" + date) === date.toString());
show("and as its number when asked", (+date) === date.getTime());

// 9.2.12: an arguments object carries its own Symbol.iterator, and it is the very
// method Array.prototype.values is.
(function () {
    show("arguments has its own", Object.prototype.hasOwnProperty.call(arguments, Symbol.iterator));
    show("and it is Array.prototype.values", arguments[Symbol.iterator] === Array.prototype.values);
    var seen = [];
    for (var index = 0; index < arguments.length; index++) {
        seen.push(arguments[index]);
    }
    show("arguments still reads by index", seen.join(","));
}(1, 2, 3));

(function () {
    "use strict";
    show("a strict arguments object has one too", Object.prototype.hasOwnProperty.call(arguments, Symbol.iterator));
}(1));
