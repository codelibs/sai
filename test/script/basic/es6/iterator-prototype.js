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
 * ES2015 25.1.2 %IteratorPrototype% and the four prototypes that sit on it.
 *
 * Every built-in iterator now inherits next() from a prototype shared by its kind,
 * and each of those inherits a Symbol.iterator that hands the receiver back -- which
 * is what makes an iterator usable as an iterable in its own right.
 *
 * The objects themselves are additions rather than changes to existing operations, so
 * they are installed unconditionally; the test asks for --language=es6 only because it
 * reaches the walks through for-of and spread as well as by hand.
 *
 * @test
 * @run
 * @option --language=es6
 */

function show(label, value) {
    print(label + ": " + value);
}

var iterators = {
    "array": [][Symbol.iterator](),
    "string": ""[Symbol.iterator](),
    "map": new Map()[Symbol.iterator](),
    "set": new Set()[Symbol.iterator](),
    "typed array": new Int8Array(0)[Symbol.iterator]()
};

// 25.1.2: the walk carries no members of its own; next comes from the prototype of
// its kind, and Symbol.iterator from the one object under all of them.
for (var kind in iterators) {
    var iterator = iterators[kind];
    var kindPrototype = Object.getPrototypeOf(iterator);
    var iteratorPrototype = Object.getPrototypeOf(kindPrototype);

    show(kind + " has its own next", iterator.hasOwnProperty("next"));
    show(kind + " reads next from its kind", kindPrototype.hasOwnProperty("next"));
    show(kind + " reads Symbol.iterator from %IteratorPrototype%",
         !kindPrototype.hasOwnProperty(Symbol.iterator) && iteratorPrototype.hasOwnProperty(Symbol.iterator));
    show(kind + " is its own iterable", iterator[Symbol.iterator]() === iterator);
}

// 22.1.5.2: an array and a typed array share one prototype; the other three do not.
show("array and typed array share a prototype",
     Object.getPrototypeOf([][Symbol.iterator]()) === Object.getPrototypeOf(new Int8Array(0)[Symbol.iterator]()));
show("map and set do not",
     Object.getPrototypeOf(new Map()[Symbol.iterator]()) !== Object.getPrototypeOf(new Set()[Symbol.iterator]()));

// 25.1.2: %IteratorPrototype% itself is an ordinary object.
var shared = Object.getPrototypeOf(Object.getPrototypeOf([][Symbol.iterator]()));
show("%IteratorPrototype% sits on Object.prototype", Object.getPrototypeOf(shared) === Object.prototype);
show("every kind shares it",
     [[], "", new Map(), new Set(), new Int8Array(0)].every(function (value) {
         return Object.getPrototypeOf(Object.getPrototypeOf(value[Symbol.iterator]())) === shared;
     }));

// 19.4.2.10: each kind names itself, so Object.prototype.toString tells them apart.
for (var kind in iterators) {
    show(kind + " names itself", Object.prototype.toString.call(iterators[kind]));
}

// next is shared, so it has to read the walk off its receiver rather than off itself.
var next = Object.getPrototypeOf([][Symbol.iterator]()).next;
var walk = [1, 2][Symbol.iterator]();
show("shared next steps the receiver", next.call(walk).value + "," + next.call(walk).value);
try {
    next.call({});
    show("a plain object is not a walk", "no error");
} catch (e) {
    show("a plain object is not a walk", e instanceof TypeError);
}

// The walks themselves keep working, including through for-of and spread.
var out = [];
for (var value of [1, 2, 3].values()) {
    out.push(value);
}
show("for-of over an array walk", out.join(","));
show("spread over a set walk", [...new Set([1, 2]).values()].join(","));
show("Array.from over a map walk", Array.from(new Map([[1, "a"]]).entries()).join(","));

// 21.1.5.1: the string walk steps by code point, so a character above the basic
// plane is one step.
var astral = "a𠮷b"[Symbol.iterator]();
show("string walk steps by code point",
     [astral.next().value, astral.next().value.length, astral.next().value, astral.next().done].join(","));
