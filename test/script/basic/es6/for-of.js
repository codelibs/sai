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
 * ES6 for-of loops.
 *
 * @test
 * @run
 * @option --language=es6
 */

var out = [];
for (var v of [1, 2, 3]) {
    out.push(v);
}
print(out.join(","));

// Strings are read by index.
out = [];
for (var c of "abc") {
    out.push(c);
}
print(out.join(","));

// let gets a fresh binding on every iteration, var does not.
var fns = [];
for (let i of [1, 2, 3]) {
    fns.push(function () { return i; });
}
print(fns.map(function (f) { return f(); }).join(","));

var fns2 = [];
for (var j of [1, 2, 3]) {
    fns2.push(function () { return j; });
}
print(fns2.map(function (f) { return f(); }).join(","));

// break and continue behave as they do in any loop.
out = [];
for (var k of [1, 2, 3, 4]) {
    if (k === 2) {
        continue;
    }
    if (k === 4) {
        break;
    }
    out.push(k);
}
print(out.join(","));

// Labels reach the outer loop.
out = [];
outer: for (var a of [1, 2]) {
    for (var b of [1, 2]) {
        if (b === 2) {
            continue outer;
        }
        out.push(a + ":" + b);
    }
}
print(out.join(","));

// The source is evaluated once.
var calls = 0;
function src() {
    calls++;
    return [1, 2];
}
for (var s of src()) {
    // nothing
}
print(calls);

// The loop variable may be an existing binding rather than a declaration.
var e;
out = [];
for (e of [7, 8]) {
    out.push(e);
}
print(out.join(",") + ";" + e);

// Anything with a length and indices works.
out = [];
for (var q of { length: 2, 0: "x", 1: "y" }) {
    out.push(q);
}
print(out.join(","));

// An empty source runs the body no times.
out = [];
for (var z of []) {
    out.push(z);
}
print("[" + out.join(",") + "]");

// for-of nests.
out = [];
for (var m of [[1, 2], [3]]) {
    for (var n of m) {
        out.push(n);
    }
}
print(out.join(","));

// A single statement body needs no braces.
out = [];
for (var w of [1, 2]) out.push(w * 10);
print(out.join(","));

// The loop variable may be a destructuring pattern. The element is bound to a
// temporary once per iteration and the pattern is taken apart from there.
for (var [a, b] of [[1, 2], [3, 4]]) {
    print(a + ":" + b);
}

for (let [a, b] of [[5, 6]]) {
    print(a + ":" + b);
}

for (const [a, b] of [[7, 8]]) {
    print(a + ":" + b);
}

for (var { x, y } of [{ x: 9, y: 10 }]) {
    print(x + ":" + y);
}

for (const { k: renamed } of [{ k: 11 }]) {
    print(renamed);
}

// Rest, nesting and defaults inside the pattern.
for (const [head, ...rest] of [[12, 13, 14]]) {
    print(head + ":" + rest);
}

for (const [{ inner }] of [[{ inner: 15 }]]) {
    print(inner);
}

for (let [withDefault = 16] of [[]]) {
    print(withDefault);
}

// The common shape: iterating pairs.
var pairs = [["k", 17], ["j", 18]];
for (const [key, value] of pairs) {
    print(key + "=" + value);
}

// With no declaration the leaves are assignment targets, member expressions included.
var lhs1, lhs2;
for ([lhs1, lhs2] of [[19, 20]]) {
    print(lhs1 + ":" + lhs2);
}

var target = {};
for ([target.field] of [[21]]) {
    print(target.field);
}

// A let binding still gets a fresh copy per iteration, so closures made in the
// body capture that iteration's value.
var captured = [];
for (let [each] of [[22], [23]]) {
    captured.push(function () { return each; });
}
print(captured[0]() + "," + captured[1]());

// break and continue are unaffected.
for (const [n] of [[24], [25], [26]]) {
    if (n === 25) {
        continue;
    }
    if (n === 26) {
        break;
    }
    print(n);
}
