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
 * ES6 rest elements in destructuring patterns.
 *
 * @test
 * @run
 * @option --language=es6
 */

var [a, ...r] = [1, 2, 3];
print(a + ":" + r.join(","));

// Nothing left over is an empty array, not undefined.
var [b, ...r2] = [1];
print(b + ":" + r2.length);

// A rest element may be the whole pattern.
var [...all] = [1, 2];
print(all.join(","));

// It is a real array.
print(Array.isArray(r));

// In an assignment as well as a declaration.
var x, y;
[x, ...y] = [4, 5, 6];
print(x + ":" + y.join(","));

// Nested inside another pattern.
var { p: [q, ...s] } = { p: [7, 8, 9] };
print(q + ":" + s.join(","));

// Array patterns read by index, so a string works here too.
var [c, ...cs] = "abc";
print(c + ":" + cs.join(","));

// As a function parameter.
function f([h, ...t]) {
    return h + ":" + t.length;
}
print(f([1, 2, 3]));

// An elision before the rest still counts.
var [, ...afterHole] = [1, 2, 3];
print(afterHole.join(","));

// Any assignment target works in an assignment pattern.
var o = {};
[o.head, ...o.tail] = [1, 2, 3];
print(o.head + ":" + o.tail.join(","));
