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
 * ES2015 7.4, the iteration protocol: for..of, the spread operator, array
 * destructuring and Array.from all read a value through its Symbol.iterator when
 * it has one.
 *
 * These are additions -- a value without the symbol is read exactly as before,
 * by length -- so they are installed unconditionally. The syntax involved needs
 * --language=es6 to parse, which is why this test asks for it.
 *
 * The walk is eager: what asks for it is compiled into a loop over something
 * with a length, so the iterator is run to exhaustion first. Two things follow
 * from that, and are recorded at the end of this file: an iterator that never
 * ends never returns, and one that is abandoned part way through is not told so.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

// The shape of an iterable that is nothing but the protocol: no length, no
// indices, just a method under the symbol.
function iterableOf(values) {
    var remaining = values.slice();
    var iterator = {
        next: function () {
            return remaining.length === 0
                    ? { value: undefined, done: true }
                    : { value: remaining.shift(), done: false };
        }
    };
    var iterable = {};
    iterable[Symbol.iterator] = function () {
        return iterator;
    };
    return iterable;
}

// The built-ins that should carry one, and what each borrows.
show("Array.prototype", Array.prototype[Symbol.iterator] === Array.prototype.values);
show("Map.prototype", Map.prototype[Symbol.iterator] === Map.prototype.entries);
show("Set.prototype", Set.prototype[Symbol.iterator] === Set.prototype.values);
show("String.prototype", typeof String.prototype[Symbol.iterator]);

// for..of, over an object that is only iterable.
var collected = "";
for (var each of iterableOf([1, 2, 3])) {
    collected += each;
}
show("for..of", collected);

// And over one that merely inherits the symbol.
collected = "";
for (var inherited of Object.create(iterableOf([4, 5]))) {
    collected += inherited;
}
show("for..of an instance", collected);

// The spread operator, in an array and in a call.
show("spread in an array literal", ["a", ...iterableOf(["b", "c"]), "d"].join(","));
show("spread in a call", (function (a, b, c) {
    return a + b + c;
}(...iterableOf([1, 2, 3]))));

// Array destructuring, in a declaration, in an assignment and in a parameter.
var first;
var second;
var third;
var declared = iterableOf([1, 2]);
var [d1, d2, d3] = declared;
show("destructuring declaration", [d1, d2, d3 === undefined].join(","));
[first, second, third] = iterableOf([3, 4]);
show("destructuring assignment", [first, second, third === undefined].join(","));
show("destructuring a parameter", (function ([a, b, c]) {
    return [a, b, c === undefined].join(",");
}(iterableOf([5, 6]))));

// Array.from, with and without a map function.
show("Array.from", Array.from(iterableOf([1, 2, 3])).join(","));
show("Array.from an instance", Array.from(Object.create(iterableOf([1, 2]))).join(","));
show("Array.from mapping", Array.from(iterableOf(["foo", "bar"]), function (e, i) {
    return e + this.tag + i;
}, { tag: "-" }).join(","));

// The collections read their argument the same way.
show("new Set from an iterable", new Set(iterableOf([1, 2, 2])).size);
show("new Map from an iterable", new Map(iterableOf([["k", "v"]])).get("k"));

// Map and Set are themselves iterable now.
show("for..of a Map", (function () {
    var out = "";
    for (var entry of new Map([["a", 1], ["b", 2]])) {
        out += entry[0] + entry[1];
    }
    return out;
}()));
show("spread a Set", [...new Set([1, 2, 2, 3])].join(","));
show("spread a Map", [...new Map([["k", "v"]])][0].join(":"));
show("Array.from a Set", Array.from(new Set([1, 2])).join(","));

// A string walks by code point, whether through the syntax or through the
// symbol, so a character above the basic plane is one step.
show("spread a string", [..."a𠮷b"].length);
show("the string iterator", (function () {
    var it = "a𠮷b"[Symbol.iterator]();
    return [it.next().value, it.next().value.length, it.next().value, it.next().done].join(",");
}()));

// Nothing that was already read by length changes.
show("an array", (function () {
    var out = "";
    for (var x of [1, 2, 3]) {
        out += x;
    }
    return out;
}()));
show("an array spread", [...[1, 2], 3].join(","));
show("an array-like for Array.from", Array.from({ length: 2, 0: "a", 1: "b" }).join(","));
show("a plain object is not iterable", (function () {
    var n = 0;
    for (var x of { a: 1 }) {
        n++;
    }
    return n;
}()));

// The two consequences of reading eagerly. An iterator is drained before the
// loop starts, so it is already finished by the time a break is reached, and
// nothing is left to tell it so.
var drained = 0;
var counting = {};
counting[Symbol.iterator] = function () {
    var i = 0;
    return {
        next: function () {
            drained++;
            return i < 3 ? { value: i++, done: false } : { value: undefined, done: true };
        },
        "return": function () {
            drained = -1;
            return {};
        }
    };
};
for (var stop of counting) {
    break;
}
show("read ahead of the loop", drained);
