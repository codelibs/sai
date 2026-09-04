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
 * %TypedArray%.prototype.keys, values and entries, and the %TypedArray% static
 * methods of and from.
 *
 * @test
 * @run
 * @option --language=es6
 */

var types = [Int8Array, Uint8Array, Uint8ClampedArray, Int16Array, Uint16Array,
             Int32Array, Uint32Array, Float32Array, Float64Array];

types.forEach(function(type) {
    ["keys", "values", "entries"].forEach(function(name) {
        if (typeof type.prototype[name] !== "function") {
            print("FAILED: " + type.name + ".prototype." + name + " is not a function");
        }
    });
    ["of", "from"].forEach(function(name) {
        if (typeof type[name] !== "function") {
            print("FAILED: " + type.name + "." + name + " is not a function");
        }
    });
});
print("all methods present on all 9 types");
print("own props of Int8Array:", Object.getOwnPropertyNames(Int8Array).sort());

function drain(iterator) {
    var out = [];
    var step;
    while (!(step = iterator.next()).done) {
        out.push(step.value);
    }
    return out;
}

// keys / values / entries
print("---- keys/values/entries ----");
var walked = new Int16Array([10, 20, 30]);
print("keys:", drain(walked.keys()));
print("values:", drain(walked.values()));
var pairs = drain(walked.entries());
print("entries:", pairs.map(function(p) { return "[" + p.join(",") + "]"; }).join(" "));
print("an entry is an Array:", pairs[0] instanceof Array, pairs[0].length);
print("Float64Array values:", drain(new Float64Array([0.5, 1.5]).values()));
print("empty:", drain(new Int8Array(0).keys()).length);

var exhausted = new Int8Array([1]).values();
print("first step:", exhausted.next().value, exhausted.next().done);
print("stays exhausted:", exhausted.next().done, exhausted.next().value);
print("iterators are independent:", drain(walked.keys()).join(",") === drain(walked.keys()).join(","));

// of
print("---- of ----");
var made = Int8Array.of(1, 2, 3);
print(made instanceof Int8Array, made.length, made.join(","));
print("no arguments:", Int8Array.of().length);
print("Int8Array wraps:", Int8Array.of(200).join(","));
print("Uint8ClampedArray saturates:", Uint8ClampedArray.of(1000, -5).join(","));
print("Uint32Array keeps big values:", Uint32Array.of(4294967295).join(","));
print("Float64Array keeps the fraction:", Float64Array.of(0.5, 1.5).join(","));
print("Float32Array rounds:", Float32Array.of(0.1).join(","));

// from
print("---- from ----");
print("array:", Int8Array.from([1, 2, 3]).join(","), Int8Array.from([1, 2, 3]) instanceof Int8Array);
print("array-like:", Int8Array.from({ length: 3, 0: 7, 1: 8, 2: 9 }).join(","));
print("string:", Int8Array.from("123").join(","));
print("arguments:", (function() { return Int8Array.from(arguments).join(","); })(4, 5, 6));
print("another typed array:", Float64Array.from(new Int8Array([1, 2])).join(","));
print("holes read as undefined:", Float64Array.from({ length: 2 }).join(","));
print("no length:", Int8Array.from({}).length);
print("map function:", Int8Array.from([1, 2, 3], function(v, i) { return v * 10 + i; }).join(","));
print("map thisArg:", Int8Array.from([1], function() { return this.k; }, { k: 5 }).join(","));
print("Uint8ClampedArray saturates:", Uint8ClampedArray.from([1000, -5]).join(","));
try {
    Int8Array.from([1], 42);
    print("FAILED: from accepted a non-callable map function");
} catch (e) {
    print("non-callable map function:", e instanceof TypeError);
}
try {
    Int8Array.from([], 42);
    print("FAILED: from accepted a non-callable map function over an empty source");
} catch (e) {
    print("checked before the source is read:", e instanceof TypeError);
}

// receiver validation for the prototype methods
print("---- receiver validation ----");
["keys", "values", "entries"].forEach(function(name) {
    [null, undefined, [1, 2], "str", 42].forEach(function(bad) {
        try {
            Int8Array.prototype[name].call(bad);
            print("FAILED: " + name + " accepted " + String(bad));
        } catch (e) {
            if (!(e instanceof TypeError)) {
                print("FAILED: " + name + " threw " + e);
            }
        }
    });
});
print("all methods reject bad receivers with TypeError");
