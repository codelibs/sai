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
 * ES2015 7.4.6 IteratorClose: an iterator that is left part way through is told so,
 * by calling its return method, and the five places that read a whole iterable and
 * can stop early now do it -- the Map, Set, WeakMap and WeakSet constructors, and
 * Array.from with a mapping function.
 *
 * Each of them read the iterable by draining it into an array first, so there was
 * nothing left to close by the time an adder or a mapping function raised. They
 * walk it one element at a time instead, which also means an entry is added before
 * the next one is read.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

var closed = 0;
var read = 0;

function walk(values) {
    var iterable = {};

    iterable[Symbol.iterator] = function () {
        var index = 0;
        return {
            next: function () {
                read++;
                return index < values.length ? { value: values[index++], done: false }
                                             : { value: undefined, done: true };
            },
            "return": function () {
                closed++;
                return {};
            }
        };
    };

    return iterable;
}

function count(label, run) {
    closed = 0;
    read = 0;
    try {
        run();
    } catch (e) {
        // The reason the walk ended is not what is being measured here.
    }
    show(label, "closed " + closed + ", read " + read);
}

// 23.1.1.2: an entry that is not a pair stops the Map constructor, and the walk is
// closed at the entry that stopped it rather than after the last one.
count("Map on a bad entry", function () { new Map(walk([1, 2, 3])); });
count("WeakMap on a bad entry", function () { new WeakMap(walk([1, 2, 3])); });
count("WeakSet on a bad member", function () { new WeakSet(walk([1, 2, 3])); });

// 23.2.1.1: or an adder that raises, which is the only way to stop a Set.
var add = Set.prototype.add;
Set.prototype.add = function () { throw new Error("no"); };
count("Set on an adder that raises", function () { new Set(walk([1, 2, 3])); });
Set.prototype.add = add;

// 22.1.2.1: and a mapping function that raises stops Array.from.
count("Array.from on a mapping that raises", function () {
    Array.from(walk([1, 2, 3]), function (value) { if (value === 2) { throw new Error("no"); } return value; });
});

// A walk that runs to the end is not closed: it ended of its own accord.
count("a Set that fills", function () { new Set(walk([1, 2, 3])); });
count("Array.from that maps every element", function () { Array.from(walk([1, 2, 3]), function (v) { return v; }); });

// An iterator with no return at all is simply not told.
var noReturn = {};
noReturn[Symbol.iterator] = function () {
    var index = 0;
    return { next: function () { return index < 3 ? { value: index++, done: false } : { done: true }; } };
};
try {
    new Map(noReturn);
    show("an iterator with no return", "no error");
} catch (e) {
    show("an iterator with no return", e instanceof TypeError);
}

// Anything a return raises is dropped: the reason the walk ended is the one worth
// reporting.
var noisy = {};
noisy[Symbol.iterator] = function () {
    return {
        next: function () { return { value: 1, done: false }; },
        "return": function () { throw new Error("from return"); }
    };
};
try {
    new Map(noisy);
    show("a return that raises", "no error");
} catch (e) {
    show("a return that raises", e.message);
}

// Everything these five did, they still do.
var map = new Map([[1, "a"], [2, "b"]]);
show("Map from pairs", map.get(1) + map.get(2) + "," + map.size);
show("Set from an array", Array.from(new Set([1, 2, 2, 3])).join(","));
show("Set from a string", Array.from(new Set("abca")).join(""));
show("WeakSet from an array", new WeakSet([map]).has(map));
show("Array.from an array", Array.from([1, 2, 3]).join(","));
show("Array.from with a mapping", Array.from([1, 2, 3], function (v) { return v * 2; }).join(","));
show("Array.from a string by code point", Array.from("a𠮷b").length);
show("Array.from an array-like", Array.from({ length: 2, 0: "a", 1: "b" }).join(","));
show("Array.from a set", Array.from(new Set([1, 2])).join(","));
show("Array.from arguments", (function () { return Array.from(arguments).join(","); }(1, 2, 3)));
show("Array.from a Java list", (function () {
    var list = new java.util.ArrayList();
    list.add(1);
    list.add(2);
    return Array.from(list).join(",");
}()));
