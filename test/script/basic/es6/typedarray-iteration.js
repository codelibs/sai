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
 * %TypedArray%.prototype iteration methods: join, forEach, every, some, map,
 * filter, reduce and reduceRight.
 *
 * @test
 * @run
 * @option --language=es6
 */

var types = [Int8Array, Uint8Array, Uint8ClampedArray, Int16Array, Uint16Array,
             Int32Array, Uint32Array, Float32Array, Float64Array];

var names = ["join", "forEach", "every", "some", "map", "filter", "reduce", "reduceRight"];

// every type sees every method, and through one shared prototype object
var sharedProto = Object.getPrototypeOf(Int8Array.prototype);
types.forEach(function(type) {
    names.forEach(function(name) {
        if (typeof type.prototype[name] !== "function") {
            print("FAILED: " + type.name + ".prototype." + name + " is not a function");
        }
    });
    if (Object.getPrototypeOf(type.prototype) !== sharedProto) {
        print("FAILED: " + type.name + ".prototype does not share %TypedArray%.prototype");
    }
});
print("all methods present on all 9 types");
print("shared prototype is not Object.prototype:", sharedProto !== Object.prototype);

// the methods are not enumerable, and are not own properties of the concrete prototypes
print("own props of Int8Array.prototype:", Object.getOwnPropertyNames(Int8Array.prototype).sort());
var enumerated = [];
for (var k in new Int8Array(2)) {
    enumerated.push(k);
}
print("for-in over an Int8Array:", enumerated);

// join
print("---- join ----");
print(new Int8Array([1, 2, 3]).join());
print(new Int8Array([1, 2, 3]).join("-"));
print(new Float64Array([0.5, 1.25]).join(","));
print("empty:", "[" + new Int32Array(0).join() + "]");

// forEach
print("---- forEach ----");
var seen = [];
new Int16Array([10, 20, 30]).forEach(function(value, index, array) {
    seen.push(index + ":" + value + ":" + (array instanceof Int16Array));
});
print(seen);

// every / some
print("---- every/some ----");
var u8 = new Uint8Array([2, 4, 6]);
print(u8.every(function(v) { return v % 2 === 0; }));
print(u8.every(function(v) { return v > 2; }));
print(u8.some(function(v) { return v > 5; }));
print(u8.some(function(v) { return v > 100; }));
print("thisArg:", new Int8Array([1]).every(function() { return this.ok; }, { ok: true }));

// map keeps the receiver's type and its element conversion
print("---- map ----");
var i8 = new Int8Array([1, 2, 3]);
var doubled = i8.map(function(v) { return v * 2; });
print(doubled instanceof Int8Array, doubled.length, doubled.join(","));
print("source unchanged:", i8.join(","));
print("Int8Array wraps:", new Int8Array([1]).map(function() { return 200; })[0]);
print("Float64Array keeps the fraction:", new Float64Array([1]).map(function() { return 0.5; })[0]);
print("Float32Array rounds:", new Float32Array([1]).map(function() { return 0.1; })[0]);
print("Uint8ClampedArray saturates:", new Uint8ClampedArray([1, 2]).map(function(v) { return v * 1000; }).join(","));
print("Uint8ClampedArray clamps low:", new Uint8ClampedArray([1]).map(function() { return -7; })[0]);
print("Uint8ClampedArray rounds to even:", new Uint8ClampedArray([1, 2]).map(function(v) { return v + 0.5; }).join(","));

// filter allocates a shorter array of the same type
print("---- filter ----");
var kept = new Int32Array([1, 2, 3, 4, 5]).filter(function(v) { return v % 2 === 1; });
print(kept instanceof Int32Array, kept.length, kept.join(","));
print("none kept:", new Float64Array([1, 2]).filter(function() { return false; }).length);

// reduce / reduceRight
print("---- reduce ----");
print(new Int8Array([1, 2, 3, 4]).reduce(function(acc, v) { return acc + v; }));
print(new Int8Array([1, 2, 3, 4]).reduce(function(acc, v) { return acc + v; }, 100));
print(new Int8Array([1, 2, 3]).reduceRight(function(acc, v) { return acc + "|" + v; }));
print(new Float64Array([0.5, 0.25]).reduce(function(acc, v) { return acc + v; }, 0));

// null/undefined receivers, and receivers that are not typed arrays, are rejected
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
print(new Int8Array(1).join.call(new Float64Array([1.5, 2.5]), ";"));
