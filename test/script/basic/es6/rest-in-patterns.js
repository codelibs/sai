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

// The rest target can itself be a pattern. It is the same "take the tail into an
// array" step, so what lands there is destructured like any other array.
var a, b, c;
[a, ...[b, c]] = [1, 2, 3];
print(a + ":" + b + ":" + c);

[...[a, b]] = [4, 5];
print(a + ":" + b);

[a, ...{ 0: b }] = [6, 7];
print(a + ":" + b);

[a, ...[b, ...c]] = [8, 9, 10, 11];
print(a + ":" + b + ":" + c.join(","));

[a, ...[[b], [c]]] = [12, [13], [14]];
print(a + ":" + b + ":" + c);

// The tail is a fresh array, so an element target that reads from the source still
// sees the source unchanged.
var src = [15, 16, 17], first, last;
[first, ...[src[2], last]] = src;
print(first + ":" + last + ":" + src.join(","));
