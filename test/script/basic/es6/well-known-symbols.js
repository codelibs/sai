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
 * ES2015 19.4.2: the well known symbols that name an operation an object can
 * take over, and 19.1.2.8 Object.getOwnPropertySymbols.
 *
 * These are additions rather than changes to existing operations -- an object
 * without the symbol behaves exactly as before -- so they are installed
 * unconditionally and this test deliberately runs without --language=es6.
 *
 * @test
 * @run
 */

function show(label, value) {
    print(label + ": " + value);
}

// 19.1.2.8: the only way to see a symbol keyed property, since every other
// listing shows an object's names.
var first = Symbol("first");
var second = Symbol("second");
var keyed = { name: 1 };
keyed[first] = "a";
keyed[second] = "b";
var symbols = Object.getOwnPropertySymbols(keyed);
show("getOwnPropertySymbols", symbols.length + "," + (symbols[0] === first) + "," + (symbols[1] === second));
show("names are unaffected", Object.getOwnPropertyNames(keyed).join(","));
show("none to find", Object.getOwnPropertySymbols({ a: 1 }).length);
show("a primitive has none", Object.getOwnPropertySymbols(5).length);

// 19.1.3.6 step 15: Symbol.toStringTag names what Object.prototype.toString says.
var tagged = {};
tagged[Symbol.toStringTag] = "Custom";
show("toStringTag", tagged + "");
tagged[Symbol.toStringTag] = 5;
show("only a string is a tag", tagged + "");
delete tagged[Symbol.toStringTag];
show("without one", tagged + "");

// The tag is found on the prototype too, and step 1 wraps a primitive first, so
// a tag on String.prototype is seen from a string.
var pairs = [[Array.prototype, []], [String.prototype, ""], [Function.prototype, function () {}],
        [Error.prototype, new Error()], [Boolean.prototype, true], [Number.prototype, 2],
        [Date.prototype, new Date()], [RegExp.prototype, /./]];
var tags = pairs.map(function (pair) {
    pair[0][Symbol.toStringTag] = "Tagged";
    var result = Object.prototype.toString.call(pair[1]);
    delete pair[0][Symbol.toStringTag];
    return result;
});
show("inherited tags", tags.join(" "));
show("removed again", Object.prototype.toString.call([]));

// 20.2.1.9 and 24.3.3 tag the two namespace objects.
show("namespace tags", Math[Symbol.toStringTag] + "," + JSON[Symbol.toStringTag]);

// Asking a Java package for a tag must not make one up: reading a name off a
// package is what creates it, so the question has to be asked without a get.
show("a Java package is undisturbed", Object.prototype.toString.call(java.util));

// 7.1.1 step 2: Symbol.toPrimitive takes over the conversion, and is told which
// hint was asked for.
var converted = {};
var hints = [];
converted[Symbol.toPrimitive] = function (hint) {
    hints.push(hint);
    return 7;
};
show("number hint", converted >= 0);
show("string hint", converted in {});
show("value used", converted * 2);
show("hints seen", hints.join(","));

// An object without one converts the old way.
show("valueOf still used", ({ valueOf: function () { return 42; } }) + 0);
show("toString still used", String({ toString: function () { return "ts"; } }));

// 22.1.3.1.1: Symbol.isConcatSpreadable decides whether concat opens a value up,
// and it decides both ways.
var unspreadable = [1, 2];
unspreadable[Symbol.isConcatSpreadable] = false;
show("an array told not to spread", [].concat(unspreadable)[0] === unspreadable);

var spreadable = { length: 2, 0: "x", 1: "y" };
spreadable[Symbol.isConcatSpreadable] = true;
show("an object told to spread", [].concat(spreadable).join(","));

// Reading the elements is part of spreading, so a getter among them runs.
var poisoned = { length: 1 };
poisoned[Symbol.isConcatSpreadable] = true;
Object.defineProperty(poisoned, 0, { get: function () { throw new RangeError("read"); } });
try {
    [].concat(poisoned);
    print("FAILED: the getter was not read");
} catch (e) {
    show("a getter is read while spreading", e instanceof RangeError);
}

// Without the symbol, concat behaves as it always has.
show("concat unchanged", [1, 2].concat([3, 4], 5).join(","));
show("an object is still one element", [].concat({ length: 2, 0: "x" }).length);
