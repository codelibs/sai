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
 * Four conformance gaps that have nothing in common but their size.
 *
 * ES2015 22.2.4.4: a typed array constructor reads anything carrying the iteration
 * protocol through it, rather than only an array or another typed array.
 *
 * ES2015 14.1.2: a parameter list carrying a default, a pattern or a rest element
 * binds each of its names once, whether or not the code is strict.
 *
 * Annex B.3.6: the initializer a for..in head may carry is allowed outside strict
 * mode only; ES5.1 had no grammar for it at all.
 *
 * ES2015 11.6: an identifier is read by the Unicode ID_Start and ID_Continue
 * properties, which UAX #31 takes the Pattern_Syntax characters out of. Java's own
 * identifier tests keep the one such character it calls a letter.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

function iterable(values) {
    var object = {};

    object[Symbol.iterator] = function () {
        var index = 0;
        return {
            next: function () {
                return index < values.length ? { value: values[index++], done: false } : { done: true };
            }
        };
    };

    return object;
}

// 22.2.4.4: every one of the nine reads an iterable.
[Int8Array, Uint8Array, Uint8ClampedArray, Int16Array, Uint16Array,
 Int32Array, Uint32Array, Float32Array, Float64Array].forEach(function (Type) {
    var array = new Type(iterable([1, 2, 3]));
    show(Type.name, array.length + "," + array[0] + "," + array[1] + "," + array[2]);
});

// A set is iterable, and so is a string.
show("from a set", new Int8Array(new Set([4, 5])).length);
show("from an empty iterable", new Int8Array(iterable([])).length);

// The older forms are unchanged.
show("from a length", new Int8Array(3).length);
show("from an array", new Int8Array([6, 7]).join(","));
show("from a typed array", new Int8Array(new Int8Array([8, 9])).join(","));
show("from a buffer", new Int8Array(new ArrayBuffer(4)).length);
// LIMITATION: 22.2.4.5 InitializeTypedArrayFromArrayLike is not here, so an object
// that is not iterable is still read as a length rather than copied.
show("a plain object is read as a length", new Int8Array({ length: 2, 0: 1, 1: 2 }).length);

// 14.1.2: a list with a pattern in it cannot repeat a name.
function refuses(source) {
    try {
        eval(source);
        return "accepted";
    } catch (e) {
        return e instanceof SyntaxError;
    }
}

show("a repeated name in a pattern", refuses("(function ([id, id]) { });"));
show("a name repeated beside a pattern", refuses("(function (a, [a]) { });"));
show("a name repeated beside a default", refuses("(function (a, a = 1) { });"));
show("a name repeated beside a rest", refuses("(function (a, ...a) { });"));
show("a name repeated in nested patterns", refuses("(function ({ a: [b], c: { b } }) { });"));

// A plain list still may, outside strict mode, and a pattern that binds a name
// only once is fine even when the function is named the same.
show("a plain list may repeat", refuses("(function (a, a) { });"));
var named = eval("(function d([d]) { return d; })");
show("a pattern binding the function name", named([true]));
show("two patterns binding different names", eval("(function ([a], [b]) { return a + b; })")([1], [2]));

// B.3.6: the initializer of a for..in head.
show("an initializer outside strict mode", refuses("for (var i = 0 in {}) {}"));
show("and inside it", refuses("'use strict'; for (var i = 0 in {}) {}"));

// 11.6: the vertical tilde is a letter to Java and not an identifier to ES2015.
show("a Pattern_Syntax character", refuses("var ⸯ;"));
show("an ordinary letter", refuses("var a2;"));
show("a letter above the basic plane", refuses("var 𐋀;"));
show("one in an escape", refuses("var \\u2E2F;"));
