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
 * %TypedArray%.prototype search and mutation methods: indexOf, lastIndexOf,
 * slice, reverse, sort, copyWithin, fill, find and findIndex.
 *
 * @test
 * @run
 * @option --language=es6
 */

var types = [Int8Array, Uint8Array, Uint8ClampedArray, Int16Array, Uint16Array,
             Int32Array, Uint32Array, Float32Array, Float64Array];

var names = ["indexOf", "lastIndexOf", "slice", "reverse", "sort", "copyWithin",
             "fill", "find", "findIndex"];

types.forEach(function(type) {
    names.forEach(function(name) {
        if (typeof type.prototype[name] !== "function") {
            print("FAILED: " + type.name + ".prototype." + name + " is not a function");
        }
    });
});
print("all methods present on all 9 types");

// indexOf / lastIndexOf
print("---- indexOf ----");
var dup = new Int8Array([3, 1, 2, 1]);
print(dup.indexOf(1), dup.indexOf(1, 2), dup.indexOf(1, -1), dup.indexOf(9));
print(dup.lastIndexOf(1), dup.lastIndexOf(1, 2), dup.lastIndexOf(9));
print("NaN is never found:", new Float64Array([NaN]).indexOf(NaN));
print("Float64Array:", new Float64Array([0.5, 1.5, 0.5]).indexOf(0.5),
      new Float64Array([0.5, 1.5, 0.5]).lastIndexOf(0.5));

// slice copies, subarray aliases
print("---- slice ----");
var sliced = new Int8Array([1, 2, 3, 4, 5]).slice(1, 4);
print(sliced instanceof Int8Array, sliced.length, sliced.join(","));
print("negative:", new Int8Array([1, 2, 3, 4, 5]).slice(-2).join(","));
print("empty:", new Int8Array([1, 2, 3]).slice(2, 1).length);
print("whole:", new Float64Array([0.5, 1.5]).slice(0).join(","));
var source = new Int8Array([1, 2, 3]);
var copy = source.slice(0);
var view = source.subarray(0);
copy[0] = 9;
print("slice does not alias:", source[0]);
view[0] = 9;
print("subarray does alias:", source[0]);
print("Uint32Array keeps big values:", new Uint32Array([4294967295, 1]).slice(0).join(","));

// reverse in place
print("---- reverse ----");
var toReverse = new Int16Array([1, 2, 3, 4]);
print(toReverse.reverse().join(","), toReverse.join(","), toReverse.reverse() === toReverse);
print("odd length:", new Float64Array([0.5, 1.5, 2.5]).reverse().join(","));

// sort defaults to numeric order, unlike Array.prototype.sort
print("---- sort ----");
print("typed:", new Int8Array([10, 9, 1, 2]).sort().join(","));
print("plain array for contrast:", [10, 9, 1, 2].sort().join(","));
print("comparator:", new Int8Array([10, 9, 1, 2]).sort(function(x, y) { return y - x; }).join(","));
print("in place:", (function() { var t = new Uint8Array([3, 1, 2]); return t.sort() === t; })());
print("NaN sorts last:", new Float64Array([NaN, 1, -1]).sort().join(","));
print("-0 sorts before 0:", 1 / new Float64Array([0, -0]).sort()[0]);
print("negatives:", new Int16Array([5, -5, 0, -100]).sort().join(","));
try {
    new Int8Array([1, 2]).sort(42);
    print("FAILED: sort accepted a non-callable comparator");
} catch (e) {
    print("non-callable comparator:", e instanceof TypeError);
}

// copyWithin
print("---- copyWithin ----");
print(new Int8Array([1, 2, 3, 4, 5]).copyWithin(0, 3).join(","));
print(new Int8Array([1, 2, 3, 4, 5]).copyWithin(1, 0, 3).join(","));
print(new Int8Array([1, 2, 3, 4, 5]).copyWithin(-2, 0).join(","));
print("no-op:", new Int8Array([1, 2, 3]).copyWithin(0, 0).join(","));
print("Float32Array:", new Float32Array([0.5, 1.5, 2.5]).copyWithin(0, 1).join(","));

// fill
print("---- fill ----");
print(new Int8Array(5).fill(7).join(","));
print(new Int8Array(5).fill(7, 1, 3).join(","));
print(new Int8Array(5).fill(7, -2).join(","));
print("Int8Array wraps:", new Int8Array(2).fill(200).join(","));
print("Uint8ClampedArray saturates:", new Uint8ClampedArray(3).fill(1000).join(","));
print("Uint32Array keeps big values:", new Uint32Array(2).fill(4294967295).join(","));
print("Float64Array keeps the fraction:", new Float64Array(2).fill(0.25).join(","));

// find / findIndex
print("---- find ----");
print(new Int8Array([1, 2, 3, 4]).find(function(v) { return v > 2; }));
print(new Int8Array([1, 2]).find(function(v) { return v > 9; }));
print(new Int8Array([1, 2, 3, 4]).findIndex(function(v) { return v > 2; }));
print(new Int8Array([1, 2]).findIndex(function(v) { return v > 9; }));
print("args:", new Int8Array([7]).find(function(v, i, arr) {
    return v === 7 && i === 0 && arr instanceof Int8Array;
}));
print("thisArg:", new Int8Array([1]).findIndex(function() { return this.ok; }, { ok: true }));

// receiver validation
print("---- receiver validation ----");
names.forEach(function(name) {
    [null, undefined, [1, 2], "str", 42].forEach(function(bad) {
        try {
            Int8Array.prototype[name].call(bad, function() { return true; });
            print("FAILED: " + name + " accepted " + String(bad));
        } catch (e) {
            if (!(e instanceof TypeError)) {
                print("FAILED: " + name + " threw " + e);
            }
        }
    });
});
print("all methods reject bad receivers with TypeError");
