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
 * A let or const declared as the variable of a for-in loop is bound once per
 * iteration, so a closure made in the body captures that iteration's key, and
 * const is accepted even though the head carries no initializer.
 *
 * @test
 * @run
 * @option --language=es6
 */

function values(fns) {
    return fns.map(function (f) { return f(); }).join(",");
}

// let: one binding per iteration, so each closure sees its own key.
var letFns = [];
for (let k in { a: 1, b: 2, c: 3 }) {
    letFns.push(function () { return k; });
}
print(values(letFns));

// const: accepted with no initializer in the head, and equally per-iteration.
var constFns = [];
for (const c in { x: 1, y: 2 }) {
    constFns.push(function () { return c; });
}
print(values(constFns));

// var keeps its single, loop-wide binding.
var varFns = [];
for (var v in { a: 1, b: 2, c: 3 }) {
    varFns.push(function () { return v; });
}
print(values(varFns));

// continue and break leave the earlier bindings alone.
var skipped = [];
for (let k in { a: 1, b: 2, c: 3 }) {
    if (k === "b") {
        continue;
    }
    skipped.push(function () { return k; });
}
print(values(skipped));

var stopped = [];
for (let k in { a: 1, b: 2, c: 3 }) {
    if (k === "c") {
        break;
    }
    stopped.push(function () { return k; });
}
print(values(stopped));

// The binding does not escape the loop.
try {
    print(k);
} catch (e) {
    print(e.name);
}

// A nested function only called after the loop has finished still sees the key
// it was made with; this is the path that reparses the function from source.
var nested = [];
for (let k in { p: 1, q: 2 }) {
    nested.push(function () { return function () { return k; }; });
}
print(nested.map(function (f) { return f()(); }).join(","));

// The loop variable shadows a parameter of the same name.
function shadow(e) {
    var seen = [];
    for (let e in { inner: 1 }) {
        seen.push(e);
    }
    return seen.join(",") + "/" + e;
}
print(shadow("outer"));

// let is writable inside the body, const is not.
for (let k in { a: 1 }) {
    k = "written";
    print(k);
}

// eval, so that the assignment is compiled on its own and the rest of this file
// still runs whether the engine rejects it at compile time or at run time.
try {
    eval("for (const c in { a: 1 }) { c = 2; }");
} catch (e) {
    print(e.name + ": " + e.message);
}

// The for each extension keeps working with a per-iteration binding.
var each = [];
for each (let value in { a: 10, b: 20 }) {
    each.push(function () { return value; });
}
print(values(each));

// An empty object never runs the body.
var never = [];
for (let k in {}) {
    never.push(k);
}
print(never.length);

// A destructuring pattern is bound per iteration too, since a key is a string and
// an array pattern reads it out character by character.
var patternLet = [];
for (let [first, second] in { ab: 1, cd: 2 }) {
    patternLet.push(function () { return first + second; });
}
print(values(patternLet));

var patternConst = [];
for (const [first, second] in { ef: 1, gh: 2 }) {
    patternConst.push(function () { return first + second; });
}
print(values(patternConst));

var patternObject = [];
for (let { length: size } in { abc: 1, de: 2 }) {
    patternObject.push(function () { return size; });
}
print(values(patternObject));

// A var pattern keeps the single loop-wide binding a var always had.
var patternVar = [];
for (var [head] in { xy: 1, zw: 2 }) {
    patternVar.push(function () { return head; });
}
print(values(patternVar));

// A pattern with no declaration assigns to whatever its leaves name.
var outerFirst, outerSecond;
for ([outerFirst, outerSecond] in { pq: 1 }) {
    print(outerFirst + outerSecond);
}
