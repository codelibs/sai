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

/**
 * ES6 Array.from over array-like values.
 *
 * Like for-of and spread, it reads by index rather than through the iterator
 * protocol, which this engine does not have. An object that is only iterable is
 * therefore out of scope.
 *
 * @test
 * @run
 * @option --language=es6
 */

print(Array.from.length);
print(Array.isArray(Array.from({ length: 0 })));

// array-likes
print(JSON.stringify(Array.from({ 0: "foo", 1: "bar", length: 2 })));
print(JSON.stringify(Array.from({ length: 3 })));
print(JSON.stringify(Array.from({ 0: "a", 1: "b", 2: "c", length: 2 })));

// real arrays, holes included
print(JSON.stringify(Array.from([1, 2, 3])));
var withHole = Array.from([1, , 3]);
print(withHole.hasOwnProperty(1) + "," + JSON.stringify(withHole));

// the copy does not alias the source
var source = [1, 2, 3];
var copy = Array.from(source);
source[0] = 9;
print(JSON.stringify(copy));

// strings, read as UTF-16 code units
print(JSON.stringify(Array.from("abc")));
print(JSON.stringify(Array.from("")));

// the arguments object
(function () {
    print(JSON.stringify(Array.from(arguments)));
})(1, 2, 3);

// primitives have no length, so they produce an empty array
print(JSON.stringify(Array.from(5)));
print(JSON.stringify(Array.from(true)));

// length goes through ToLength: NaN, negatives and fractions
print(JSON.stringify(Array.from({ length: "nope" })));
print(JSON.stringify(Array.from({ length: -1 })));
print(JSON.stringify(Array.from({ 0: "a", 1: "b", 2: "c", length: 2.7 })));

// map function, called with (element, index)
print(JSON.stringify(Array.from({ 0: "foo", 1: "bar", length: 2 }, function (e, i) { return e + i; })));
print(JSON.stringify(Array.from([1, 2, 3], function (e) { return e * 2; })));
print(JSON.stringify(Array.from("abc", function (c, i) { return c + i; })));
print(JSON.stringify(Array.from([1, 2], function (e) { return e * this.k; }, { k: 10 })));
print(JSON.stringify(Array.from([], undefined)));

// Java arrays, Lists and Iterables
var ints = new (Java.type("int[]"))(3);
ints[0] = 1; ints[1] = 2; ints[2] = 3;
print(JSON.stringify(Array.from(ints)));

var longs = new (Java.type("long[]"))(2);
longs[0] = 7; longs[1] = 8;
print(JSON.stringify(Array.from(longs)));

var strings = new (Java.type("java.lang.String[]"))(2);
strings[0] = "x"; strings[1] = "y";
print(JSON.stringify(Array.from(strings)));

var list = new (Java.type("java.util.ArrayList"))();
list.add("p"); list.add("q");
print(JSON.stringify(Array.from(list)));
print(JSON.stringify(Array.from(list, function (e, i) { return e + i; })));

var set = new (Java.type("java.util.LinkedHashSet"))();
set.add("m"); set.add("n");
print(JSON.stringify(Array.from(set)));

// null and undefined are a TypeError
[null, undefined].forEach(function (bad) {
    try {
        Array.from(bad);
        print("no TypeError from Array.from");
    } catch (e) {
        print("from " + (e instanceof TypeError));
    }
});

// a map function that is present but not callable is a TypeError, even for an
// empty source
[1, null, {}, "x"].forEach(function (bad) {
    try {
        Array.from([], bad);
        print("no TypeError from Array.from");
    } catch (e) {
        print("mapfn " + (e instanceof TypeError));
    }
});
