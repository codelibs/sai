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
 * ES2015 Symbol.species: the accessor a constructor carries so that a method
 * building a new object out of an old one asks the old one's constructor which
 * constructor to use, and 9.4.2.3 ArraySpeciesCreate, which is where the five
 * Array.prototype methods that build a new array read it.
 *
 * A species is an addition -- an array whose constructor was never touched builds
 * an ordinary array exactly as before -- so it is installed unconditionally and
 * this test deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

// 19.4.2.3: each of these is a getter answering the receiver, not a data property.
[["Array", Array], ["Map", Map], ["Set", Set], ["RegExp", RegExp], ["ArrayBuffer", ArrayBuffer],
 ["Int8Array", Int8Array], ["Uint8Array", Uint8Array], ["Uint8ClampedArray", Uint8ClampedArray],
 ["Int16Array", Int16Array], ["Uint16Array", Uint16Array], ["Int32Array", Int32Array],
 ["Uint32Array", Uint32Array], ["Float32Array", Float32Array],
 ["Float64Array", Float64Array]].forEach(function (pair) {
    var constructor = pair[1];
    var descriptor = Object.getOwnPropertyDescriptor(constructor, Symbol.species);

    show(pair[0], [typeof descriptor.get, descriptor.set === undefined, constructor[Symbol.species] === constructor,
                   descriptor.enumerable, descriptor.configurable].join(","));
});

// The getter answers whatever it is read from, which is what makes a subclass
// that says nothing build objects of its own kind.
var derived = Object.create(Array);
show("read from a derived constructor", derived[Symbol.species] === derived);

function speciesArray(species) {
    var array = [1, 2, 3];
    array.constructor = {};
    array.constructor[Symbol.species] = species;
    return array;
}

// 9.4.2.3: the five methods build what the species says, not an array.
var Tagged = function () { this.tagged = true; };

show("concat", Array.prototype.concat.call(speciesArray(Tagged), []).tagged);
show("filter", Array.prototype.filter.call(speciesArray(Tagged), Boolean).tagged);
show("map", Array.prototype.map.call(speciesArray(Tagged), Boolean).tagged);
show("slice", Array.prototype.slice.call(speciesArray(Tagged), 0).tagged);
show("splice", Array.prototype.splice.call(speciesArray(Tagged), 0).tagged);

// The elements reach it, and so does the length where the specification writes one.
var built = Array.prototype.map.call(speciesArray(Tagged), function (value) { return value * 2; });
show("the elements reach the species", [built[0], built[1], built[2]].join(","));
show("map writes no length", built.length === undefined);
show("slice writes one", Array.prototype.slice.call(speciesArray(Tagged), 1).length);

// The length the specification hands the constructor differs per method.
var lengths = [];
function Recorder(length) { lengths.push(length); }
Array.prototype.concat.call(speciesArray(Recorder), []);
Array.prototype.filter.call(speciesArray(Recorder), Boolean);
Array.prototype.map.call(speciesArray(Recorder), Boolean);
Array.prototype.slice.call(speciesArray(Recorder), 1);
Array.prototype.splice.call(speciesArray(Recorder), 1, 2);
show("lengths handed to the constructor", lengths.join(","));

// An ordinary array is untouched: no constructor of its own, so no species to read.
show("an ordinary concat", Array.isArray([1].concat([2])) + "," + [1].concat([2]).join(","));
show("an ordinary map", Array.isArray([1, 2].map(Boolean)));
show("an ordinary filter", [1, 2, 3].filter(function (v) { return v > 1; }).join(","));
show("an ordinary slice", [1, 2, 3].slice(1).join(","));
show("an ordinary splice", [1, 2, 3].splice(1, 1).join(","));

// Nor is an array-like: 9.4.2.3 asks for a species only of a real array.
var like = { length: 2, 0: "a", 1: "b", constructor: { } };
like.constructor[Symbol.species] = Tagged;
show("an array-like has no species", Array.prototype.map.call(like, function (v) { return v; }).join(","));

// A species of Array, or of null or undefined, all mean an ordinary array.
show("a species of Array", Array.isArray(speciesArray(Array).map(Boolean)));
show("a species of null", Array.isArray(speciesArray(null).map(Boolean)));
show("a species of undefined", Array.isArray(speciesArray(undefined).map(Boolean)));

// Anything else that is not a constructor is a TypeError.
try {
    speciesArray(5).map(Boolean);
    show("a species that cannot construct", "no error");
} catch (e) {
    show("a species that cannot construct", e instanceof TypeError);
}

// Holes are not handed over, the way they are not copied into an ordinary array.
var holes = [1, , 3];
holes.constructor = {};
holes.constructor[Symbol.species] = Tagged;
var sliced = Array.prototype.slice.call(holes, 0);
show("a hole stays a hole", [0 in sliced, 1 in sliced, 2 in sliced].join(","));
